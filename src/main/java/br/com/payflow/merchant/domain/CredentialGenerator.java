package br.com.payflow.merchant.domain;

// Port for random credentials, so use cases stay deterministic in tests.
public interface CredentialGenerator {

    ApiKey newApiKey();

    String newWebhookSecret();
}
