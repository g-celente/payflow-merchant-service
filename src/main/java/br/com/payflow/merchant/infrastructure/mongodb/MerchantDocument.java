package br.com.payflow.merchant.infrastructure.mongodb;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("merchants")
public record MerchantDocument(
        @Id UUID id,
        String name,
        String email,
        String document,
        String apiKeyHash,
        String status,
        WebhookDocument webhook,
        Instant createdAt,
        Instant updatedAt) {

    public record WebhookDocument(String url, String secret, List<String> events) {
    }
}
