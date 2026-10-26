package br.com.payflow.merchant.features.provision;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantStatus;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CLOCK;
import static br.com.payflow.merchant.support.TestMerchants.HASHER;
import static br.com.payflow.merchant.support.TestMerchants.KEY;
import static br.com.payflow.merchant.support.TestMerchants.KEY_HASH;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProvisionDemoMerchantHandlerTests {

    private final AtomicReference<Merchant> stored = new AtomicReference<>();
    private final ProvisionDemoMerchantHandler handler = new ProvisionDemoMerchantHandler(
            candidate -> {
                stored.compareAndSet(null, candidate);
                return stored.get();
            }, HASHER, CLOCK);

    @Test
    void createsActiveDemoMerchantWithHashAndFixedTime() {
        Merchant merchant = handler.execute(KEY);
        assertThat(merchant.id()).isEqualTo(ProvisionDemoMerchantHandler.MERCHANT_ID);
        assertThat(merchant.name()).isEqualTo(ProvisionDemoMerchantHandler.NAME);
        assertThat(merchant.document()).isEqualTo(ProvisionDemoMerchantHandler.DOCUMENT);
        assertThat(merchant.status()).isEqualTo(MerchantStatus.ACTIVE);
        assertThat(merchant.createdAt()).isEqualTo(NOW);
        assertThat(merchant.apiKeyHash()).isEqualTo(KEY_HASH);
    }

    @Test
    void repetitionKeepsOriginalMerchantAndTimestamp() {
        Merchant original = handler.execute(KEY);
        var later = new ProvisionDemoMerchantHandler(candidate -> stored.get(), HASHER,
                Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC));
        assertThat(later.execute(KEY)).isSameAs(original);
    }

    @Test
    void changedKeyFailsWithoutOverwritingExistingMerchant() {
        Merchant original = handler.execute(KEY);
        assertThatThrownBy(() -> handler.execute("b".repeat(32)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("different API key");
        assertThat(stored.get()).isSameAs(original);
    }

    @Test
    void invalidKeyNeverReachesPersistence() {
        for (String key : new String[]{null, "", " ".repeat(32), "short", "a".repeat(257)}) {
            assertThatThrownBy(() -> handler.execute(key)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(stored.get()).isNull();
    }

    @Test
    void persistenceFailureIsNotHidden() {
        var failing = new ProvisionDemoMerchantHandler(candidate -> {
            throw new IllegalStateException("Storage unavailable");
        }, HASHER, CLOCK);
        assertThatThrownBy(() -> failing.execute(KEY)).hasMessage("Storage unavailable");
    }

    @Test
    void hashingIsDelegatedToThePort() {
        var custom = new ProvisionDemoMerchantHandler(candidate -> candidate, apiKey -> "f".repeat(64), CLOCK);
        assertThat(custom.execute(KEY).apiKeyHash()).isEqualTo("f".repeat(64));
    }
}
