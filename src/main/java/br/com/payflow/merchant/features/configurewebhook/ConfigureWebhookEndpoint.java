package br.com.payflow.merchant.features.configurewebhook;

import java.util.List;
import java.util.UUID;

import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Webhook")
class ConfigureWebhookEndpoint {

    private final ConfigureWebhookHandler handler;

    ConfigureWebhookEndpoint(ConfigureWebhookHandler handler) {
        this.handler = handler;
    }

    @PutMapping("/v1/merchants/{id}/webhook")
    @Operation(summary = "Define URL, segredo e eventos do webhook (sem segredo, um é gerado e devolvido)")
    WebhookResponse configure(@PathVariable UUID id, @RequestBody ConfigureWebhookRequest request) {
        WebhookSubscription webhook = handler.execute(
                new ConfigureWebhookCommand(id, request.url(), request.secret(), request.events()));
        return new WebhookResponse(id, webhook.url(), webhook.secret(),
                webhook.events().stream().map(WebhookEvent::wireName).toList());
    }

    record ConfigureWebhookRequest(String url, String secret, List<String> events) {
    }

    record WebhookResponse(UUID merchantId, String url, String secret, List<String> events) {
    }
}
