package br.com.payflow.merchant.features.listmerchants;

import java.util.List;

import br.com.payflow.merchant.domain.Merchant;

public record MerchantPage(List<Merchant> items, int page, int size, long totalItems) {

    public MerchantPage {
        items = List.copyOf(items);
    }

    public long totalPages() {
        return (totalItems + size - 1) / size;
    }
}
