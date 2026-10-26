package br.com.payflow.merchant.infrastructure.crypto;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.WebhookSubscription;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomCredentialGeneratorTests {

    private final SecureRandomCredentialGenerator generator = new SecureRandomCredentialGenerator();

    @Test
    void apiKeyIsPrefixedHexOf256Bits() {
        String key = generator.newApiKey().value();
        assertThat(key).matches("pf_[0-9a-f]{64}");
        assertThat(ApiKey.isWellFormed(key)).isTrue();
    }

    @Test
    void webhookSecretFitsTheSubscriptionRules() {
        String secret = generator.newWebhookSecret();
        assertThat(secret).matches("whsec_[0-9a-f]{64}");
        assertThat(secret.length()).isBetween(WebhookSubscription.MIN_SECRET_LENGTH, WebhookSubscription.MAX_SECRET_LENGTH);
    }

    @Test
    void valuesDoNotRepeat() {
        assertThat(generator.newApiKey()).isNotEqualTo(generator.newApiKey());
        assertThat(generator.newWebhookSecret()).isNotEqualTo(generator.newWebhookSecret());
    }
}
