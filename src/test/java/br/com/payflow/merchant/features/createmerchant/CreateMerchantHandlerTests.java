package br.com.payflow.merchant.features.createmerchant;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.MerchantStatus;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import br.com.payflow.merchant.support.TestMerchants.FixedCredentials;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CLOCK;
import static br.com.payflow.merchant.support.TestMerchants.HASHER;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateMerchantHandlerTests {

    private final InMemoryMerchantStore store = new InMemoryMerchantStore();
    private final CreateMerchantHandler handler =
            new CreateMerchantHandler(store, HASHER, new FixedCredentials(), CLOCK);

    @Test
    void createsActiveMerchantAndStoresOnlyTheKeyHash() {
        CreatedMerchant created = handler.execute(new CreateMerchantCommand("Loja", "contato@loja.com", "12345678000190"));

        var stored = store.stored(created.merchant().id()).orElseThrow();
        assertThat(created.apiKey()).startsWith("pf_test_key_");
        assertThat(stored.apiKeyHash()).isEqualTo(HASHER.hash(ApiKey.of(created.apiKey())));
        assertThat(stored.status()).isEqualTo(MerchantStatus.ACTIVE);
        assertThat(stored.hasWebhook()).isFalse();
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(created.toString()).doesNotContain(created.apiKey());
    }

    @Test
    void eachMerchantGetsItsOwnKey() {
        var first = handler.execute(new CreateMerchantCommand("Loja A", "a@loja.com", "11111111111"));
        var second = handler.execute(new CreateMerchantCommand("Loja B", "b@loja.com", "22222222222"));
        assertThat(first.apiKey()).isNotEqualTo(second.apiKey());
        assertThat(first.merchant().id()).isNotEqualTo(second.merchant().id());
    }

    @Test
    void duplicateDocumentIsRejected() {
        handler.execute(new CreateMerchantCommand("Loja", "a@loja.com", "12345678000190"));
        assertThatThrownBy(() -> handler.execute(new CreateMerchantCommand("Outra", "b@loja.com", "12345678000190")))
                .isInstanceOf(DocumentAlreadyRegisteredException.class);
        assertThat(store.size()).isEqualTo(1);
    }

    @Test
    void invalidDataNeverReachesTheStore() {
        assertThatThrownBy(() -> handler.execute(new CreateMerchantCommand("Loja", "sem-arroba", "12345678000190")))
                .isInstanceOf(DomainValidationException.class);
        assertThat(store.size()).isZero();
    }
}
