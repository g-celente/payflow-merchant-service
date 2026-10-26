package br.com.payflow.merchant.features.listmerchants;

import java.util.Objects;

public final class ListMerchantsHandler {

    private final MerchantListing merchants;

    public ListMerchantsHandler(MerchantListing merchants) {
        this.merchants = Objects.requireNonNull(merchants);
    }

    public MerchantPage execute(ListMerchantsQuery query) {
        return merchants.list(query);
    }
}
