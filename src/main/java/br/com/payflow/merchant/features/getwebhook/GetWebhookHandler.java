package br.com.payflow.merchant.features.getwebhook;

import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.domain.WebhookSubscription;

public final class GetWebhookHandler {

    private final WebhookReader merchants;

    public GetWebhookHandler(WebhookReader merchants) {
        this.merchants = Objects.requireNonNull(merchants);
    }

    public WebhookSubscription execute(UUID merchantId) {
        Merchant merchant = merchants.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        if (!merchant.hasWebhook()) {
            throw new WebhookNotConfiguredException(merchantId);
        }
        return merchant.webhook();
    }
}
