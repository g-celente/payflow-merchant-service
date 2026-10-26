package br.com.payflow.merchant.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantStatus;
import br.com.payflow.merchant.domain.WebhookEvent;

// Public view of a merchant shared by the endpoints: never carries the API key hash or the webhook secret.
public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String document,
        MerchantStatus status,
        boolean webhookConfigured,
        Webhook webhook,
        Instant createdAt,
        Instant updatedAt) {

    public record Webhook(String url, List<String> events) {
    }

    public static MerchantResponse from(Merchant merchant) {
        Webhook webhook = merchant.hasWebhook()
                ? new Webhook(merchant.webhook().url(), eventNames(merchant))
                : null;
        return new MerchantResponse(merchant.id(), merchant.name(), merchant.email(), merchant.document(),
                merchant.status(), merchant.hasWebhook(), webhook, merchant.createdAt(), merchant.updatedAt());
    }

    private static List<String> eventNames(Merchant merchant) {
        return merchant.webhook().events().stream().map(WebhookEvent::wireName).toList();
    }
}
