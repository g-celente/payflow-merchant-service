package br.com.payflow.merchant.features.createmerchant;

public record CreateMerchantCommand(String name, String email, String document) {
}
