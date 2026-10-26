package br.com.payflow.merchant.infrastructure.crypto;

import java.security.SecureRandom;
import java.util.HexFormat;

import br.com.payflow.merchant.domain.ApiKey;
import br.com.payflow.merchant.domain.CredentialGenerator;

// 32 random bytes (256 bits) per credential, hex encoded behind a readable prefix.
public final class SecureRandomCredentialGenerator implements CredentialGenerator {

    static final String API_KEY_PREFIX = "pf_";
    static final String WEBHOOK_SECRET_PREFIX = "whsec_";
    private static final int RANDOM_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    @Override
    public ApiKey newApiKey() {
        return ApiKey.of(API_KEY_PREFIX + randomHex());
    }

    @Override
    public String newWebhookSecret() {
        return WEBHOOK_SECRET_PREFIX + randomHex();
    }

    private String randomHex() {
        byte[] bytes = new byte[RANDOM_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
