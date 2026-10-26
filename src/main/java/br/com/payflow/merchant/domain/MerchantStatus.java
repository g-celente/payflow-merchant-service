package br.com.payflow.merchant.domain;

// Only ACTIVE merchants can authenticate with their API key.
public enum MerchantStatus {
    ACTIVE,
    SUSPENDED
}
