package br.com.payflow.merchant.features.getwebhook;

import java.util.List;
import java.util.UUID;

import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// Internal route for the deliver-webhook Function: returns the secret it needs to sign deliveries.
@RestController
@Tag(name = "Webhook")
class GetWebhookEndpoint {

    private final GetWebhookHandler handler;

    GetWebhookEndpoint(GetWebhookHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/v1/merchants/{id}/webhook")
    @Operation(summary = "Configuração completa do webhook, com o segredo (uso interno da Function)")
    WebhookResponse get(@PathVariable UUID id) {
        WebhookSubscription webhook = handler.execute(id);
        return new WebhookResponse(id, webhook.url(), webhook.secret(),
                webhook.events().stream().map(WebhookEvent::wireName).toList());
    }

    @ExceptionHandler(WebhookNotConfiguredException.class)
    ProblemDetail notConfigured(WebhookNotConfiguredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Not Found");
        return problem;
    }

    record WebhookResponse(UUID merchantId, String url, String secret, List<String> events) {
    }
}
