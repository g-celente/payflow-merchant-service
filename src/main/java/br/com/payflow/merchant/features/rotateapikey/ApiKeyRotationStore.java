package br.com.payflow.merchant.features.rotateapikey;

import java.util.Optional;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the rotate-key slice.
public interface ApiKeyRotationStore {

    Optional<Merchant> findById(UUID id);

    // Throws MerchantNotFoundException if the merchant was removed meanwhile.
    void replace(Merchant merchant);
}
