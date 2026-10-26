package br.com.payflow.merchant.domain;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookSubscriptionTests {

    private static final String SECRET = "s".repeat(WebhookSubscription.MIN_SECRET_LENGTH);
    private static final Set<WebhookEvent> EVENTS = EnumSet.of(WebhookEvent.PAYMENT_CONFIRMED);

    @Test
    void acceptsAbsoluteHttpAndHttpsUrls() {
        assertThat(new WebhookSubscription("https://loja.example/hooks", SECRET, EVENTS).url()).startsWith("https");
        // http stays allowed so a local receiver (host.docker.internal, localhost) works during development.
        assertThat(new WebhookSubscription("http://localhost:4000/hooks", SECRET, EVENTS).url()).startsWith("http");
    }

    @Test
    void rejectsOtherUrls() {
        for (String url : new String[]{null, "", "loja.example/hooks", "/hooks", "ftp://loja.example", "https://", "not a url"}) {
            assertThatThrownBy(() -> new WebhookSubscription(url, SECRET, EVENTS))
                    .isInstanceOf(DomainValidationException.class).hasMessageContaining("URL");
        }
    }

    @Test
    void secretLengthIsBounded() {
        for (String secret : new String[]{null, "short", "x".repeat(WebhookSubscription.MAX_SECRET_LENGTH + 1)}) {
            assertThatThrownBy(() -> new WebhookSubscription("https://loja.example", secret, EVENTS))
                    .isInstanceOf(DomainValidationException.class).hasMessageContaining("secret");
        }
    }

    @Test
    void needsAtLeastOneEventAndKeepsItsOwnCopy() {
        assertThatThrownBy(() -> new WebhookSubscription("https://loja.example", SECRET, Set.of()))
                .isInstanceOf(DomainValidationException.class);
        Set<WebhookEvent> events = EnumSet.of(WebhookEvent.PAYMENT_AUTHORIZED);
        WebhookSubscription webhook = new WebhookSubscription("https://loja.example", SECRET, events);
        events.add(WebhookEvent.PAYMENT_REFUNDED);
        assertThat(webhook.events()).containsExactly(WebhookEvent.PAYMENT_AUTHORIZED);
        assertThatThrownBy(() -> webhook.events().add(WebhookEvent.PAYMENT_DECLINED))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void eventsUseThePaymentServiceNames() {
        assertThat(WebhookEvent.fromWireName("payment.refunded")).isEqualTo(WebhookEvent.PAYMENT_REFUNDED);
        assertThatThrownBy(() -> WebhookEvent.fromWireName("payment.unknown"))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("payment.unknown");
    }

    @Test
    void textNeverShowsTheSecret() {
        assertThat(new WebhookSubscription("https://loja.example", SECRET, EVENTS).toString()).doesNotContain(SECRET);
    }
}
