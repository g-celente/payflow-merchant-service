package br.com.payflow.merchant.features.getwebhook;

import java.util.UUID;

public final class WebhookNotConfiguredException extends RuntimeException {

    public WebhookNotConfiguredException(UUID merchantId) {
        super("Merchant " + merchantId + " has no webhook configured");
    }
}
