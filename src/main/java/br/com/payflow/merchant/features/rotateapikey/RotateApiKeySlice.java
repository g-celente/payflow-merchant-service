package br.com.payflow.merchant.features.rotateapikey;

import java.time.Clock;

import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.CredentialGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RotateApiKeySlice {

    @Bean
    RotateApiKeyHandler rotateApiKeyHandler(ApiKeyRotationStore merchants, ApiKeyHasher hasher,
            CredentialGenerator credentials, Clock clock) {
        return new RotateApiKeyHandler(merchants, hasher, credentials, clock);
    }
}
