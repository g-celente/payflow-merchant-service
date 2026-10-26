package br.com.payflow.merchant.features.configurewebhook;

import java.util.Optional;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the configure-webhook slice.
public interface WebhookConfigurationStore {

    Optional<Merchant> findById(UUID id);

    // Throws MerchantNotFoundException if the merchant was removed meanwhile.
    void replace(Merchant merchant);
}
