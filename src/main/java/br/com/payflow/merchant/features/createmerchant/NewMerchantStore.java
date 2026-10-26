package br.com.payflow.merchant.features.createmerchant;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the create slice.
public interface NewMerchantStore {

    // Throws DocumentAlreadyRegisteredException when the document is taken.
    void add(Merchant merchant);
}
