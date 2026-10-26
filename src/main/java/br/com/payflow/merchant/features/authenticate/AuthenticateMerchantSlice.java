package br.com.payflow.merchant.features.authenticate;

import br.com.payflow.merchant.domain.ApiKeyHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Composition of the "authenticate merchant" slice.
@Configuration(proxyBeanMethods = false)
public class AuthenticateMerchantSlice {

    @Bean
    AuthenticateMerchantHandler authenticateMerchantHandler(MerchantLookup merchants, ApiKeyHasher hasher) {
        return new AuthenticateMerchantHandler(merchants, hasher);
    }
}
