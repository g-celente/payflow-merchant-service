package br.com.payflow.merchant.features.authenticate;

import java.util.Optional;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the authentication slice.
public interface MerchantLookup {

    Optional<Merchant> findByApiKeyHash(String apiKeyHash);
}
