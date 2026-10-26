package br.com.payflow.merchant.features.createmerchant;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.CredentialGenerator;
import br.com.payflow.merchant.domain.Merchant;

public final class CreateMerchantHandler {

    private final NewMerchantStore merchants;
    private final ApiKeyHasher hasher;
    private final CredentialGenerator credentials;
    private final Clock clock;

    public CreateMerchantHandler(NewMerchantStore merchants, ApiKeyHasher hasher, CredentialGenerator credentials,
            Clock clock) {
        this.merchants = Objects.requireNonNull(merchants);
        this.hasher = Objects.requireNonNull(hasher);
        this.credentials = Objects.requireNonNull(credentials);
        this.clock = Objects.requireNonNull(clock);
    }

    public CreatedMerchant execute(CreateMerchantCommand command) {
        ApiKey apiKey = credentials.newApiKey();
        Merchant merchant = Merchant.register(UUID.randomUUID(), command.name(), command.email(), command.document(),
                hasher.hash(apiKey), Instant.now(clock));
        merchants.add(merchant);
        return new CreatedMerchant(merchant, apiKey.value());
    }
}
