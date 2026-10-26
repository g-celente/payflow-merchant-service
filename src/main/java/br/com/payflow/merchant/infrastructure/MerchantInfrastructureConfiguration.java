package br.com.payflow.merchant.infrastructure;

import java.time.Clock;

import br.com.payflow.merchant.domain.ApiKeyHasher;
import br.com.payflow.merchant.domain.CredentialGenerator;
import br.com.payflow.merchant.infrastructure.crypto.SecureRandomCredentialGenerator;
import br.com.payflow.merchant.infrastructure.crypto.Sha256ApiKeyHasher;
import br.com.payflow.merchant.infrastructure.mongodb.MerchantIndexes;
import br.com.payflow.merchant.infrastructure.mongodb.MongoMerchantStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

// Binds the slice ports, ApiKeyHasher and CredentialGenerator to their adapters.
@Configuration(proxyBeanMethods = false)
public class MerchantInfrastructureConfiguration {

    @Bean
    MongoMerchantStore merchantStore(MongoTemplate mongo) {
        return new MongoMerchantStore(mongo);
    }

    @Bean
    ApiKeyHasher apiKeyHasher() {
        return new Sha256ApiKeyHasher();
    }

    @Bean
    CredentialGenerator credentialGenerator() {
        return new SecureRandomCredentialGenerator();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    InitializingBean merchantIndexes(MongoTemplate mongo) {
        return () -> MerchantIndexes.ensure(mongo);
    }
}
