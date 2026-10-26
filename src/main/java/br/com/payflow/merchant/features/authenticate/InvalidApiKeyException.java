package br.com.payflow.merchant.features.authenticate;

// Same failure for missing, malformed, unknown and suspended keys, so callers cannot probe which one happened.
public final class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException() {
        super("Missing or invalid API key");
    }
}
