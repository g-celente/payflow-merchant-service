package br.com.payflow.merchant.features.deletemerchant;

import java.util.UUID;

// Output port used only by the delete slice.
public interface MerchantRemoval {

    // Returns false when no merchant has this id.
    boolean deleteById(UUID id);
}
