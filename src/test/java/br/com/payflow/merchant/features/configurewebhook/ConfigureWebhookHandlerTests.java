package br.com.payflow.merchant.features.configurewebhook;

import java.util.List;
import java.util.UUID;

import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import br.com.payflow.merchant.support.TestMerchants.FixedCredentials;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CLOCK;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigureWebhookHandlerTests {

    private static final String URL = "https://loja.example/webhooks";

    private final Merchant existing = merchant("12345678000190");
    private final InMemoryMerchantStore store = new InMemoryMerchantStore().with(existing);
    private final ConfigureWebhookHandler handler = new ConfigureWebhookHandler(store, new FixedCredentials(), CLOCK);

    @Test
    void storesTheGivenSecretAndEvents() {
        String secret = "meu-segredo-com-16+";
        WebhookSubscription webhook = handler.execute(new ConfigureWebhookCommand(existing.id(), URL, secret,
                List.of("payment.authorized", "payment.refunded", "payment.authorized")));

        assertThat(webhook.secret()).isEqualTo(secret);
        assertThat(webhook.events()).containsExactly(WebhookEvent.PAYMENT_AUTHORIZED, WebhookEvent.PAYMENT_REFUNDED);
        Merchant stored = store.stored(existing.id()).orElseThrow();
        assertThat(stored.webhook()).isEqualTo(webhook);
        assertThat(stored.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void generatesASecretWhenNoneIsGiven() {
        for (String secret : new String[]{null, "", "   "}) {
            WebhookSubscription webhook = handler.execute(
                    new ConfigureWebhookCommand(existing.id(), URL, secret, List.of("payment.confirmed")));
            assertThat(webhook.secret()).startsWith("whsec_test_");
        }
    }

    @Test
    void rejectsUnknownOrMissingEventsAndBadUrls() {
        assertThatThrownBy(() -> handler.execute(
                new ConfigureWebhookCommand(existing.id(), URL, null, List.of("payment.unknown"))))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> handler.execute(new ConfigureWebhookCommand(existing.id(), URL, null, null)))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> handler.execute(
                new ConfigureWebhookCommand(existing.id(), "ftp://loja", null, List.of("payment.confirmed"))))
                .isInstanceOf(DomainValidationException.class);
        assertThat(store.stored(existing.id()).orElseThrow().hasWebhook()).isFalse();
    }

    @Test
    void unknownMerchantIsNotFound() {
        assertThatThrownBy(() -> handler.execute(
                new ConfigureWebhookCommand(UUID.randomUUID(), URL, null, List.of("payment.confirmed"))))
                .isInstanceOf(MerchantNotFoundException.class);
    }

    @Test
    void commandTextNeverShowsTheSecret() {
        assertThat(new ConfigureWebhookCommand(existing.id(), URL, "segredo-visivel-123", List.of()).toString())
                .doesNotContain("segredo-visivel-123");
    }
}
