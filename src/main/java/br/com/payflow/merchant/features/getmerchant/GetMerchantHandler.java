package br.com.payflow.merchant.features.getmerchant;

import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;

public final class GetMerchantHandler {

    private final MerchantReader merchants;

    public GetMerchantHandler(MerchantReader merchants) {
        this.merchants = Objects.requireNonNull(merchants);
    }

    public Merchant execute(UUID id) {
        return merchants.findById(id).orElseThrow(() -> new MerchantNotFoundException(id));
    }
}
