package br.com.payflow.merchant.features.rotateapikey;

import java.util.UUID;

// Use case output. The new raw key is shown once; the old one stops working immediately.
public record RotatedApiKey(UUID merchantId, String apiKey) {

    @Override
    public String toString() {
        return "RotatedApiKey[merchantId=" + merchantId + ", apiKey=****]";
    }
}
