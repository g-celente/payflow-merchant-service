package br.com.payflow.merchant;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import br.com.payflow.merchant.features.provision.ProvisionDemoMerchantHandler;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Real HTTP against a real MongoDB 8 (Testcontainers); never touches Atlas.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MerchantServiceIntegrationTests {

    private static final String DEMO_KEY = "demo-" + UUID.randomUUID();
    private static final String INTERNAL_KEY = "internal-" + UUID.randomUUID();
    private static final ObjectMapper JSON = new ObjectMapper();

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.mongodb.database", () -> "payflow_test");
        registry.add("payflow.internal-key", () -> INTERNAL_KEY);
        registry.add("payflow.demo-merchant.enabled", () -> true);
        registry.add("payflow.demo-merchant.api-key", () -> DEMO_KEY);
    }

    @Autowired
    MongoTemplate mongo;

    @Autowired
    ApplicationRunner provisionDemoMerchant;

    @Autowired
    ProvisionDemoMerchantHandler provisionHandler;

    @Autowired
    Environment environment;

    // ---- demo merchant and authentication ------------------------------------------------------------------

    @Test
    void bootstrapStoresOnlyHashAndIsRepeatable() throws Exception {
        provisionDemoMerchant.run(new DefaultApplicationArguments(new String[0]));

        assertThat(mongo.count(byId(ProvisionDemoMerchantHandler.MERCHANT_ID), "merchants")).isEqualTo(1);
        Document stored = mongo.findOne(byId(ProvisionDemoMerchantHandler.MERCHANT_ID), Document.class, "merchants");
        assertThat(stored).isNotNull();
        assertThat(stored.getString("apiKeyHash")).isEqualTo(sha256(DEMO_KEY));
        assertThat(stored.getString("status")).isEqualTo("ACTIVE");
        assertThat(stored.toJson()).doesNotContain(DEMO_KEY);
    }

    @Test
    void duplicateApiKeyHashIsRejectedByTheIndex() {
        var duplicate = new Document("_id", "another-merchant").append("apiKeyHash", sha256(DEMO_KEY));
        assertThatThrownBy(() -> mongo.insert(duplicate, "merchants")).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void changedDemoKeyDoesNotOverwriteTheStoredMerchant() {
        String before = mongo.findOne(byId(ProvisionDemoMerchantHandler.MERCHANT_ID), Document.class, "merchants")
                .getString("apiKeyHash");
        assertThatThrownBy(() -> provisionHandler.execute("different-key-for-integration-test"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("different API key");
        assertThat(mongo.findOne(byId(ProvisionDemoMerchantHandler.MERCHANT_ID), Document.class, "merchants")
                .getString("apiKeyHash")).isEqualTo(before);
    }

    @Test
    void concurrentProvisioningPreservesSingleMerchant() throws Exception {
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(4)) {
            var tasks = new java.util.ArrayList<java.util.concurrent.Callable<UUID>>();
            for (int index = 0; index < 12; index++) {
                tasks.add(() -> provisionHandler.execute(DEMO_KEY).id());
            }
            for (var result : executor.invokeAll(tasks)) {
                assertThat(result.get()).isEqualTo(ProvisionDemoMerchantHandler.MERCHANT_ID);
            }
        }
        assertThat(mongo.count(byId(ProvisionDemoMerchantHandler.MERCHANT_ID), "merchants")).isEqualTo(1);
    }

    @Test
    void currentMerchantIsReturnedForValidMerchantKey() throws Exception {
        var response = call("GET", "/v1/merchants/me", null, null, DEMO_KEY);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode body = JSON.readTree(response.body());
        assertThat(body.get("id").asText()).isEqualTo(ProvisionDemoMerchantHandler.MERCHANT_ID.toString());
        assertThat(body.get("name").asText()).isEqualTo("Demo Merchant");
        assertThat(body.get("webhookConfigured").asBoolean()).isFalse();
        assertThat(response.body()).doesNotContain(DEMO_KEY).doesNotContain("apiKeyHash");
    }

    @Test
    void missingOrUnknownMerchantKeyIsUnauthorizedWithProblemDetails() throws Exception {
        for (String key : new String[]{null, "short", "x".repeat(40)}) {
            var response = call("GET", "/v1/merchants/me", null, null, key);
            assertThat(response.statusCode()).isEqualTo(401);
            assertThat(response.headers().firstValue("WWW-Authenticate")).hasValue("ApiKey header=\"X-Merchant-Key\"");
            JsonNode problem = JSON.readTree(response.body());
            assertThat(problem.get("status").asInt()).isEqualTo(401);
            assertThat(problem.get("detail").asText()).isEqualTo("Missing or invalid API key");
        }
    }

    // ---- internal key --------------------------------------------------------------------------------------

    @Test
    void internalRoutesRequireTheInternalKey() throws Exception {
        for (String key : new String[]{null, "wrong-internal-key"}) {
            var response = call("GET", "/v1/merchants", null, key, null);
            assertThat(response.statusCode()).isEqualTo(401);
            assertThat(response.headers().firstValue("WWW-Authenticate")).hasValue("ApiKey header=\"X-Internal-Key\"");
            assertThat(response.headers().firstValue("Content-Type")).hasValue("application/problem+json");
        }
        // The merchant key is not a platform credential.
        assertThat(call("GET", "/v1/merchants", null, null, DEMO_KEY).statusCode()).isEqualTo(401);
    }

    // ---- CRUD ----------------------------------------------------------------------------------------------

    @Test
    void merchantLifecycle() throws Exception {
        String document = randomCnpj();
        var created = internal("POST", "/v1/merchants",
                "{\"name\":\"Loja Integração\",\"email\":\"contato@integra.com\",\"document\":\"" + document + "\"}");
        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode body = JSON.readTree(created.body());
        String id = body.at("/merchant/id").asText();
        String apiKey = body.get("apiKey").asText();
        assertThat(created.headers().firstValue("Location")).hasValue("/v1/merchants/" + id);
        assertThat(body.at("/merchant/status").asText()).isEqualTo("ACTIVE");
        assertThat(apiKey).startsWith("pf_");

        // The key returned once authenticates the new merchant.
        var me = call("GET", "/v1/merchants/me", null, null, apiKey);
        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(me.body()).get("id").asText()).isEqualTo(id);

        var fetched = internal("GET", "/v1/merchants/" + id, null);
        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(fetched.body()).doesNotContain(apiKey).doesNotContain("apiKeyHash");

        var updated = internal("PUT", "/v1/merchants/" + id,
                "{\"name\":\"Loja Renomeada\",\"email\":\"novo@integra.com\",\"status\":\"SUSPENDED\"}");
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(updated.body()).get("name").asText()).isEqualTo("Loja Renomeada");
        assertThat(JSON.readTree(updated.body()).get("document").asText()).isEqualTo(document);

        // A suspended merchant can no longer authenticate.
        assertThat(call("GET", "/v1/merchants/me", null, null, apiKey).statusCode()).isEqualTo(401);

        var suspended = JSON.readTree(internal("GET", "/v1/merchants?status=SUSPENDED&size=100", null).body());
        assertThat(suspended.get("items").valueStream().map(item -> item.get("id").asText())).contains(id);
        var active = JSON.readTree(internal("GET", "/v1/merchants?status=ACTIVE&size=100", null).body());
        assertThat(active.get("items").valueStream().map(item -> item.get("id").asText())).doesNotContain(id);

        assertThat(internal("DELETE", "/v1/merchants/" + id, null).statusCode()).isEqualTo(204);
        assertThat(internal("GET", "/v1/merchants/" + id, null).statusCode()).isEqualTo(404);
        assertThat(internal("DELETE", "/v1/merchants/" + id, null).statusCode()).isEqualTo(404);
    }

    @Test
    void duplicateDocumentIsConflict() throws Exception {
        String payload = "{\"name\":\"Loja\",\"email\":\"a@loja.com\",\"document\":\"" + randomCnpj() + "\"}";
        assertThat(internal("POST", "/v1/merchants", payload).statusCode()).isEqualTo(201);
        var conflict = internal("POST", "/v1/merchants", payload);
        assertThat(conflict.statusCode()).isEqualTo(409);
        assertThat(JSON.readTree(conflict.body()).get("status").asInt()).isEqualTo(409);
    }

    @Test
    void invalidInputIsBadRequestWithTheReason() throws Exception {
        var invalidEmail = internal("POST", "/v1/merchants", "{\"name\":\"Loja\",\"email\":\"sem-arroba\",\"document\":\"12345678901\"}");
        assertThat(invalidEmail.statusCode()).isEqualTo(400);
        assertThat(JSON.readTree(invalidEmail.body()).get("detail").asText()).contains("e-mail");

        assertThat(internal("POST", "/v1/merchants", "{not json").statusCode()).isEqualTo(400);
        assertThat(internal("GET", "/v1/merchants/not-a-uuid", null).statusCode()).isEqualTo(400);
        assertThat(internal("GET", "/v1/merchants?size=1000", null).statusCode()).isEqualTo(400);
        assertThat(internal("GET", "/v1/merchants?status=UNKNOWN", null).statusCode()).isEqualTo(400);
        assertThat(internal("GET", "/v1/merchants/" + UUID.randomUUID(), null).statusCode()).isEqualTo(404);
    }

    // ---- webhook and key rotation --------------------------------------------------------------------------

    @Test
    void webhookIsConfiguredForTheMerchantAndReadWithSecretOnlyOnTheInternalRoute() throws Exception {
        String id = createMerchant();
        assertThat(internal("GET", "/v1/merchants/" + id + "/webhook", null).statusCode()).isEqualTo(404);

        var configured = internal("PUT", "/v1/merchants/" + id + "/webhook",
                "{\"url\":\"https://loja.example/hooks\",\"events\":[\"payment.authorized\",\"payment.refunded\"]}");
        assertThat(configured.statusCode()).isEqualTo(200);
        String secret = JSON.readTree(configured.body()).get("secret").asText();
        assertThat(secret).startsWith("whsec_");

        JsonNode webhook = JSON.readTree(internal("GET", "/v1/merchants/" + id + "/webhook", null).body());
        assertThat(webhook.get("url").asText()).isEqualTo("https://loja.example/hooks");
        assertThat(webhook.get("secret").asText()).isEqualTo(secret);
        assertThat(webhook.get("events").valueStream().map(JsonNode::asText))
                .containsExactly("payment.authorized", "payment.refunded");

        var merchant = internal("GET", "/v1/merchants/" + id, null);
        assertThat(JSON.readTree(merchant.body()).get("webhookConfigured").asBoolean()).isTrue();
        assertThat(merchant.body()).doesNotContain(secret);

        var invalid = internal("PUT", "/v1/merchants/" + id + "/webhook",
                "{\"url\":\"https://loja.example\",\"events\":[\"payment.unknown\"]}");
        assertThat(invalid.statusCode()).isEqualTo(400);
    }

    @Test
    void rotatedKeyReplacesTheOldOneImmediately() throws Exception {
        String document = randomCnpj();
        JsonNode created = JSON.readTree(internal("POST", "/v1/merchants",
                "{\"name\":\"Loja\",\"email\":\"a@loja.com\",\"document\":\"" + document + "\"}").body());
        String id = created.at("/merchant/id").asText();
        String oldKey = created.get("apiKey").asText();

        var rotated = internal("POST", "/v1/merchants/" + id + "/api-key", null);
        assertThat(rotated.statusCode()).isEqualTo(200);
        String newKey = JSON.readTree(rotated.body()).get("apiKey").asText();

        assertThat(newKey).isNotEqualTo(oldKey);
        assertThat(call("GET", "/v1/merchants/me", null, null, oldKey).statusCode()).isEqualTo(401);
        assertThat(call("GET", "/v1/merchants/me", null, null, newKey).statusCode()).isEqualTo(200);
    }

    // ---- operations ----------------------------------------------------------------------------------------

    @Test
    void swaggerDocumentsEveryRoute() throws Exception {
        var docs = call("GET", "/v3/api-docs", null, null, null);
        assertThat(docs.statusCode()).isEqualTo(200);
        assertThat(docs.body()).contains("/v1/merchants/{id}/webhook", "/v1/merchants/{id}/api-key",
                "/v1/merchants/me", "X-Internal-Key", "X-Merchant-Key");
        assertThat(call("GET", "/swagger-ui/index.html", null, null, null).statusCode()).isEqualTo(200);
    }

    @Test
    void healthCheckIsUpWithoutExposingDetails() throws Exception {
        var response = call("GET", "/actuator/health", null, null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode health = JSON.readTree(response.body());
        assertThat(health.get("status").asText()).isEqualTo("UP");
        assertThat(health.has("components")).isFalse();
        assertThat(health.has("details")).isFalse();
    }

    // ---- helpers -------------------------------------------------------------------------------------------

    private String createMerchant() throws Exception {
        var created = internal("POST", "/v1/merchants",
                "{\"name\":\"Loja\",\"email\":\"a@loja.com\",\"document\":\"" + randomCnpj() + "\"}");
        assertThat(created.statusCode()).isEqualTo(201);
        return JSON.readTree(created.body()).at("/merchant/id").asText();
    }

    private HttpResponse<String> internal(String method, String path, String json) throws Exception {
        return call(method, path, json, INTERNAL_KEY, null);
    }

    private HttpResponse<String> call(String method, String path, String json, String internalKey, String merchantKey)
            throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create(
                    "http://localhost:" + environment.getRequiredProperty("local.server.port") + path));
            request.method(method, json == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json));
            if (json != null) {
                request.header("Content-Type", "application/json");
            }
            if (internalKey != null) {
                request.header("X-Internal-Key", internalKey);
            }
            if (merchantKey != null) {
                request.header("X-Merchant-Key", merchantKey);
            }
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private static Query byId(UUID id) {
        return Query.query(Criteria.where("_id").is(id));
    }

    private static String randomCnpj() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(10_000_000_000_000L, 99_999_999_999_999L));
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
