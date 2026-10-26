package br.com.payflow.merchant.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

// Aggregate of the merchants collection. Only the SHA-256 of the API key is kept; webhook may be null.
public record Merchant(
        UUID id,
        String name,
        String email,
        String document,
        String apiKeyHash,
        MerchantStatus status,
        WebhookSubscription webhook,
        Instant createdAt,
        Instant updatedAt) {

    public static final int MAX_NAME_LENGTH = 120;
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    // CPF (11 digits) or CNPJ (14 digits), digits only.
    private static final Pattern DOCUMENT = Pattern.compile("\\d{11}|\\d{14}");
    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    public Merchant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("Merchant name must contain between 1 and " + MAX_NAME_LENGTH + " characters");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new DomainValidationException("Merchant e-mail is invalid");
        }
        if (document == null || !DOCUMENT.matcher(document).matches()) {
            throw new DomainValidationException("Merchant document must be a CPF (11 digits) or CNPJ (14 digits)");
        }
        if (status == null) {
            throw new DomainValidationException("Merchant status is required");
        }
        if (apiKeyHash == null || !SHA256_HEX.matcher(apiKeyHash).matches()) {
            throw new IllegalArgumentException("API key hash must be a SHA-256 hexadecimal digest");
        }
        name = name.strip();
    }

    public static Merchant register(UUID id, String name, String email, String document, String apiKeyHash, Instant now) {
        return new Merchant(id, name, email, document, apiKeyHash, MerchantStatus.ACTIVE, null, now, now);
    }

    public Merchant withProfile(String newName, String newEmail, MerchantStatus newStatus, Instant now) {
        return new Merchant(id, newName, newEmail, document, apiKeyHash, newStatus, webhook, createdAt, now);
    }

    public Merchant withWebhook(WebhookSubscription newWebhook, Instant now) {
        return new Merchant(id, name, email, document, apiKeyHash, status, Objects.requireNonNull(newWebhook), createdAt, now);
    }

    public Merchant withApiKeyHash(String newApiKeyHash, Instant now) {
        return new Merchant(id, name, email, document, newApiKeyHash, status, webhook, createdAt, now);
    }

    public boolean isActive() {
        return status == MerchantStatus.ACTIVE;
    }

    public boolean hasWebhook() {
        return webhook != null;
    }

    // Credentials stay out of logs and test output.
    @Override
    public String toString() {
        return "Merchant[id=" + id + ", name=" + name + ", status=" + status
                + ", webhookConfigured=" + hasWebhook() + "]";
    }
}
