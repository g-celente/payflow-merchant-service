package br.com.payflow.merchant.features.getmerchant;

import java.util.Optional;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the get slice.
public interface MerchantReader {

    Optional<Merchant> findById(UUID id);
}
