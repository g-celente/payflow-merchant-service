package br.com.payflow.merchant.features.rotateapikey;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.CredentialGenerator;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;

public final class RotateApiKeyHandler {

    private final ApiKeyRotationStore merchants;
    private final ApiKeyHasher hasher;
    private final CredentialGenerator credentials;
    private final Clock clock;

    public RotateApiKeyHandler(ApiKeyRotationStore merchants, ApiKeyHasher hasher, CredentialGenerator credentials,
            Clock clock) {
        this.merchants = Objects.requireNonNull(merchants);
        this.hasher = Objects.requireNonNull(hasher);
        this.credentials = Objects.requireNonNull(credentials);
        this.clock = Objects.requireNonNull(clock);
    }

    public RotatedApiKey execute(UUID merchantId) {
        Merchant merchant = merchants.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        ApiKey apiKey = credentials.newApiKey();
        merchants.replace(merchant.withApiKeyHash(hasher.hash(apiKey), Instant.now(clock)));
        return new RotatedApiKey(merchantId, apiKey.value());
    }
}
