package br.com.payflow.merchant.infrastructure.mongodb;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantStatus;
import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;

final class MerchantMapper {

    private MerchantMapper() {
    }

    static Merchant toDomain(MerchantDocument document) {
        Objects.requireNonNull(document, "MongoDB returned no merchant");
        // Documents written before status/updatedAt existed are read as active and never updated.
        MerchantStatus status = document.status() == null ? MerchantStatus.ACTIVE : MerchantStatus.valueOf(document.status());
        return new Merchant(document.id(), document.name(), document.email(), document.document(),
                document.apiKeyHash(), status, toDomain(document.webhook()), document.createdAt(),
                document.updatedAt() == null ? document.createdAt() : document.updatedAt());
    }

    static MerchantDocument toDocument(Merchant merchant) {
        return new MerchantDocument(merchant.id(), merchant.name(), merchant.email(), merchant.document(),
                merchant.apiKeyHash(), merchant.status().name(), toDocument(merchant.webhook()),
                merchant.createdAt(), merchant.updatedAt());
    }

    private static WebhookSubscription toDomain(MerchantDocument.WebhookDocument webhook) {
        if (webhook == null) {
            return null;
        }
        Set<WebhookEvent> events = EnumSet.noneOf(WebhookEvent.class);
        webhook.events().forEach(name -> events.add(WebhookEvent.fromWireName(name)));
        return new WebhookSubscription(webhook.url(), webhook.secret(), events);
    }

    private static MerchantDocument.WebhookDocument toDocument(WebhookSubscription webhook) {
        if (webhook == null) {
            return null;
        }
        return new MerchantDocument.WebhookDocument(webhook.url(), webhook.secret(),
                webhook.events().stream().map(WebhookEvent::wireName).toList());
    }
}
