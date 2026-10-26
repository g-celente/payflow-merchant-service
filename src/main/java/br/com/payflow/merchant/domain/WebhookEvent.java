package br.com.payflow.merchant.domain;

import java.util.Arrays;

// Payment events a merchant can subscribe to, named as published by the payment-service.
public enum WebhookEvent {
    PAYMENT_AUTHORIZED("payment.authorized"),
    PAYMENT_DECLINED("payment.declined"),
    PAYMENT_PENDING("payment.pending"),
    PAYMENT_CONFIRMED("payment.confirmed"),
    PAYMENT_REFUNDED("payment.refunded"),
    PAYMENT_CANCELED("payment.canceled");

    private final String wireName;

    WebhookEvent(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static WebhookEvent fromWireName(String name) {
        return Arrays.stream(values())
                .filter(event -> event.wireName.equals(name))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("Unknown webhook event: " + name));
    }
}
