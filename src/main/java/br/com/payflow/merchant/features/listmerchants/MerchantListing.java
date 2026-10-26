package br.com.payflow.merchant.features.listmerchants;

// Output port used only by the list slice. Newest merchants first.
public interface MerchantListing {

    MerchantPage list(ListMerchantsQuery query);
}
