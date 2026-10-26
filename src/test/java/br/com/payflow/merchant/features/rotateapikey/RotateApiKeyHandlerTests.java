package br.com.payflow.merchant.features.rotateapikey;

import java.util.UUID;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import br.com.payflow.merchant.support.TestMerchants.FixedCredentials;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CLOCK;
import static br.com.payflow.merchant.support.TestMerchants.HASHER;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RotateApiKeyHandlerTests {

    private final Merchant existing = merchant("12345678000190");
    private final InMemoryMerchantStore store = new InMemoryMerchantStore().with(existing);
    private final RotateApiKeyHandler handler = new RotateApiKeyHandler(store, HASHER, new FixedCredentials(), CLOCK);

    @Test
    void replacesTheStoredHashWithTheNewKey() {
        RotatedApiKey rotated = handler.execute(existing.id());

        Merchant stored = store.stored(existing.id()).orElseThrow();
        assertThat(stored.apiKeyHash()).isEqualTo(HASHER.hash(ApiKey.of(rotated.apiKey())))
                .isNotEqualTo(existing.apiKeyHash());
        assertThat(stored.updatedAt()).isEqualTo(NOW);
        assertThat(rotated.toString()).doesNotContain(rotated.apiKey());
    }

    @Test
    void everyRotationIssuesADifferentKey() {
        assertThat(handler.execute(existing.id()).apiKey()).isNotEqualTo(handler.execute(existing.id()).apiKey());
    }

    @Test
    void unknownMerchantIsNotFound() {
        assertThatThrownBy(() -> handler.execute(UUID.randomUUID())).isInstanceOf(MerchantNotFoundException.class);
    }
}
