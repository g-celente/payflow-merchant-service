package br.com.payflow.merchant.features.provision;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.Merchant;

public final class ProvisionDemoMerchantHandler {

    public static final UUID MERCHANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String NAME = "Demo Merchant";
    public static final String EMAIL = "demo@payflow.local";
    public static final String DOCUMENT = "11222333000181";

    private final MerchantProvisioning merchants;
    private final ApiKeyHasher hasher;
    private final Clock clock;

    public ProvisionDemoMerchantHandler(MerchantProvisioning merchants, ApiKeyHasher hasher, Clock clock) {
        this.merchants = Objects.requireNonNull(merchants);
        this.hasher = Objects.requireNonNull(hasher);
        this.clock = Objects.requireNonNull(clock);
    }

    public Merchant execute(String apiKey) {
        String hash = hasher.hash(ApiKey.of(apiKey));
        Merchant stored = Objects.requireNonNull(merchants.insertIfAbsent(
                Merchant.register(MERCHANT_ID, NAME, EMAIL, DOCUMENT, hash, Instant.now(clock))),
                "Merchant store returned no merchant");
        if (!MERCHANT_ID.equals(stored.id()) || !hash.equals(stored.apiKeyHash())) {
            throw new IllegalStateException(
                    "Demo merchant already exists with a different API key. "
                    + "Use the original key; changing the environment does not rotate credentials.");
        }
        return stored;
    }
}
