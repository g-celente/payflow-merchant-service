package br.com.payflow.merchant.features.authenticate;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantStatus;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.HASHER;
import static br.com.payflow.merchant.support.TestMerchants.KEY;
import static br.com.payflow.merchant.support.TestMerchants.KEY_HASH;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static br.com.payflow.merchant.support.TestMerchants.merchantWithKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticateMerchantHandlerTests {

    private static final Merchant MERCHANT = merchantWithKey(KEY);

    private final AtomicInteger lookups = new AtomicInteger();
    private Merchant stored = MERCHANT;
    private final AuthenticateMerchantHandler handler = new AuthenticateMerchantHandler(hash -> {
        lookups.incrementAndGet();
        return KEY_HASH.equals(hash) ? Optional.of(stored) : Optional.empty();
    }, HASHER);

    @Test
    void knownKeyReturnsTheMerchantWithoutPrintingCredentials() {
        Merchant merchant = handler.execute(KEY);
        assertThat(merchant.id()).isEqualTo(MERCHANT.id());
        assertThat(merchant.toString()).doesNotContain(KEY).doesNotContain(KEY_HASH);
    }

    @Test
    void unknownKeyIsRejected() {
        assertThatThrownBy(() -> handler.execute("b".repeat(32))).isInstanceOf(InvalidApiKeyException.class);
        assertThat(lookups).hasValue(1);
    }

    @Test
    void suspendedMerchantIsRejectedLikeAnUnknownKey() {
        stored = MERCHANT.withProfile(MERCHANT.name(), MERCHANT.email(), MerchantStatus.SUSPENDED, NOW);
        assertThatThrownBy(() -> handler.execute(KEY))
                .isInstanceOf(InvalidApiKeyException.class).hasMessage("Missing or invalid API key");
    }

    @Test
    void malformedKeyIsRejectedWithoutQueryingTheStore() {
        for (String key : new String[]{null, "", " ".repeat(32), "short", "a".repeat(257)}) {
            assertThatThrownBy(() -> handler.execute(key)).isInstanceOf(InvalidApiKeyException.class);
        }
        assertThat(lookups).hasValue(0);
    }

    @Test
    void storeFailureIsNotReportedAsInvalidKey() {
        var failing = new AuthenticateMerchantHandler(hash -> {
            throw new IllegalStateException("Storage unavailable");
        }, HASHER);
        assertThatThrownBy(() -> failing.execute(KEY))
                .isInstanceOf(IllegalStateException.class).hasMessage("Storage unavailable");
    }
}
