package br.com.payflow.merchant.features.authenticate;

import java.util.Objects;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.Merchant;

public final class AuthenticateMerchantHandler {

    private final MerchantLookup merchants;
    private final ApiKeyHasher hasher;

    public AuthenticateMerchantHandler(MerchantLookup merchants, ApiKeyHasher hasher) {
        this.merchants = Objects.requireNonNull(merchants);
        this.hasher = Objects.requireNonNull(hasher);
    }

    public Merchant execute(String apiKey) {
        if (!ApiKey.isWellFormed(apiKey)) {
            throw new InvalidApiKeyException();
        }
        return merchants.findByApiKeyHash(hasher.hash(ApiKey.of(apiKey)))
                .filter(Merchant::isActive)
                .orElseThrow(InvalidApiKeyException::new);
    }
}
