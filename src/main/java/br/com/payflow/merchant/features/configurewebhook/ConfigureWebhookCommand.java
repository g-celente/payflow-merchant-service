package br.com.payflow.merchant.features.configurewebhook;

import java.util.List;
import java.util.UUID;

// secret null or blank = generate one. events uses the wire names (payment.authorized, ...).
public record ConfigureWebhookCommand(UUID merchantId, String url, String secret, List<String> events) {

    @Override
    public String toString() {
        return "ConfigureWebhookCommand[merchantId=" + merchantId + ", url=" + url + ", secret=****, events=" + events + "]";
    }
}
