package br.com.payflow.merchant.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

// Where and what the deliver-webhook Function sends for a merchant. The secret signs each delivery (HMAC).
public record WebhookSubscription(String url, String secret, Set<WebhookEvent> events) {

    public static final int MIN_SECRET_LENGTH = 16;
    public static final int MAX_SECRET_LENGTH = 256;

    public WebhookSubscription {
        if (!isHttpUrl(url)) {
            throw new DomainValidationException("Webhook URL must be an absolute http or https URL");
        }
        if (secret == null || secret.length() < MIN_SECRET_LENGTH || secret.length() > MAX_SECRET_LENGTH) {
            throw new DomainValidationException("Webhook secret must contain between "
                    + MIN_SECRET_LENGTH + " and " + MAX_SECRET_LENGTH + " characters");
        }
        if (events == null || events.isEmpty()) {
            throw new DomainValidationException("Subscribe to at least one webhook event");
        }
        events = Collections.unmodifiableSet(EnumSet.copyOf(events));
    }

    @Override
    public String toString() {
        return "WebhookSubscription[url=" + url + ", secret=****, events=" + events + "]";
    }

    private static boolean isHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            return ("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) && uri.getHost() != null;
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
