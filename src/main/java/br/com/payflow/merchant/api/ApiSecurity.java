package br.com.payflow.merchant.api;

// Header names and OpenAPI security scheme names shared by the endpoints, the filter and the Swagger setup.
public final class ApiSecurity {

    // Platform callers (BFF, deliver-webhook Function) on every /v1 route except /v1/merchants/me.
    public static final String INTERNAL_KEY_HEADER = "X-Internal-Key";
    // The merchant's own key, only on /v1/merchants/me.
    public static final String MERCHANT_KEY_HEADER = "X-Merchant-Key";

    public static final String INTERNAL_KEY_SCHEME = "internalKey";
    public static final String MERCHANT_KEY_SCHEME = "merchantKey";

    private ApiSecurity() {
    }
}
