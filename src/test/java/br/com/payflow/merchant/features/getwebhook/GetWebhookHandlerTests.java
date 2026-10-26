package br.com.payflow.merchant.features.getwebhook;

import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CREATED;
import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static br.com.payflow.merchant.support.TestMerchants.webhook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetWebhookHandlerTests {

    private final Merchant configured = merchant("11111111111").withWebhook(webhook(), CREATED);
    private final Merchant withoutWebhook = merchant("22222222222");
    private final GetWebhookHandler handler =
            new GetWebhookHandler(new InMemoryMerchantStore().with(configured, withoutWebhook));

    @Test
    void returnsTheFullSubscriptionWithSecret() {
        assertThat(handler.execute(configured.id())).isEqualTo(webhook());
    }

    @Test
    void merchantWithoutWebhookIsReportedApartFromUnknownMerchant() {
        assertThatThrownBy(() -> handler.execute(withoutWebhook.id())).isInstanceOf(WebhookNotConfiguredException.class);
        assertThatThrownBy(() -> handler.execute(UUID.randomUUID())).isInstanceOf(MerchantNotFoundException.class);
    }
}
