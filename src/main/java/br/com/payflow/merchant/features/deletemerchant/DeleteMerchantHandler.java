package br.com.payflow.merchant.features.deletemerchant;

import java.util.Objects;
import java.util.UUID;

import br.com.payflow.merchant.domain.MerchantNotFoundException;

public final class DeleteMerchantHandler {

    private final MerchantRemoval merchants;

    public DeleteMerchantHandler(MerchantRemoval merchants) {
        this.merchants = Objects.requireNonNull(merchants);
    }

    public void execute(UUID id) {
        if (!merchants.deleteById(id)) {
            throw new MerchantNotFoundException(id);
        }
    }
}
