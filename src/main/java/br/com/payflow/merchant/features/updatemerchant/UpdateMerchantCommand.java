package br.com.payflow.merchant.features.updatemerchant;

import java.util.UUID;

import br.com.payflow.merchant.domain.MerchantStatus;

// Replaces the editable profile; the document, key and webhook are untouched.
public record UpdateMerchantCommand(UUID id, String name, String email, MerchantStatus status) {
}
