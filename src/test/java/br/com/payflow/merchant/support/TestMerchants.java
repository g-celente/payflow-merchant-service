package br.com.payflow.merchant.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.CredentialGenerator;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;
import br.com.payflow.merchant.infrastructure.crypto.Sha256ApiKeyHasher;

public final class TestMerchants {

    public static final Instant CREATED = Instant.parse("2026-10-01T12:00:00Z");
    public static final Instant NOW = Instant.parse("2026-10-26T15:00:00Z");
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    public static final Sha256ApiKeyHasher HASHER = new Sha256ApiKeyHasher();
    public static final String KEY = "a".repeat(32);
    public static final String KEY_HASH = "3ba3f5f43b92602683c19aee62a20342b084dd5971ddd33808d81a328879a547";

    private TestMerchants() {
    }

    public static Merchant merchant(String document) {
        return Merchant.register(UUID.randomUUID(), "Loja " + document, "contato@loja.com", document,
                HASHER.hash(ApiKey.of("key-for-" + document + "-".repeat(32))), CREATED);
    }

    public static Merchant merchantWithKey(String rawKey) {
        return Merchant.register(UUID.randomUUID(), "Loja Exemplo", "contato@loja.com", "12345678000190",
                HASHER.hash(ApiKey.of(rawKey)), CREATED);
    }

    public static WebhookSubscription webhook() {
        return new WebhookSubscription("https://loja.example/webhooks", "whsec_" + "s".repeat(32),
                EnumSet.of(WebhookEvent.PAYMENT_AUTHORIZED));
    }

    // Predictable credentials: pf_test_key_000...1, pf_test_key_000...2, ...
    public static final class FixedCredentials implements CredentialGenerator {

        private final AtomicInteger keys = new AtomicInteger();
        private final AtomicInteger secrets = new AtomicInteger();

        @Override
        public ApiKey newApiKey() {
            return ApiKey.of("pf_test_key_" + "%040d".formatted(keys.incrementAndGet()));
        }

        @Override
        public String newWebhookSecret() {
            return "whsec_test_" + "%032d".formatted(secrets.incrementAndGet());
        }
    }
}
