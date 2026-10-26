package br.com.payflow.merchant.features.createmerchant;

import br.com.payflow.merchant.domain.Merchant;

// Use case output. The raw API key exists only here, to be shown to the merchant once.
public record CreatedMerchant(Merchant merchant, String apiKey) {

    @Override
    public String toString() {
        return "CreatedMerchant[merchant=" + merchant + ", apiKey=****]";
    }
}
