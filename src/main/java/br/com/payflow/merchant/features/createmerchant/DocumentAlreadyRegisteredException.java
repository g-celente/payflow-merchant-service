package br.com.payflow.merchant.features.createmerchant;

public final class DocumentAlreadyRegisteredException extends RuntimeException {

    public DocumentAlreadyRegisteredException() {
        super("A merchant with this document is already registered");
    }
}
