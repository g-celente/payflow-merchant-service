package br.com.payflow.merchant.features.getwebhook;

import java.util.Optional;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the get-webhook slice.
public interface WebhookReader {

    Optional<Merchant> findById(UUID id);
}
