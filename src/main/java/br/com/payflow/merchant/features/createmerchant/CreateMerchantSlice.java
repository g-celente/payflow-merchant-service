package br.com.payflow.merchant.features.createmerchant;

import java.time.Clock;

import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.CredentialGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CreateMerchantSlice {

    @Bean
    CreateMerchantHandler createMerchantHandler(NewMerchantStore merchants, ApiKeyHasher hasher,
            CredentialGenerator credentials, Clock clock) {
        return new CreateMerchantHandler(merchants, hasher, credentials, clock);
    }
}
