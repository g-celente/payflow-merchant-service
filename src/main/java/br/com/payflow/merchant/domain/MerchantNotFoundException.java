package br.com.payflow.merchant.domain;

import java.util.UUID;

public final class MerchantNotFoundException extends RuntimeException {

    public MerchantNotFoundException(UUID id) {
        super("Merchant " + id + " not found");
    }
}
