package br.com.payflow.merchant.features.provision;

import java.time.Clock;

import br.com.payflow.merchant.domain.ApiKeyHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Composition of the "provision demo merchant" slice: use case plus its startup trigger.
@Configuration(proxyBeanMethods = false)
public class ProvisionDemoMerchantSlice {

    private static final Logger log = LoggerFactory.getLogger(ProvisionDemoMerchantSlice.class);

    @Bean
    ProvisionDemoMerchantHandler provisionDemoMerchantHandler(MerchantProvisioning merchants, ApiKeyHasher hasher,
            Clock clock) {
        return new ProvisionDemoMerchantHandler(merchants, hasher, clock);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "payflow.demo-merchant.enabled", havingValue = "true")
    @EnableConfigurationProperties(DemoMerchantProperties.class)
    static class StartupTrigger {

        @Bean
        ApplicationRunner provisionDemoMerchant(ProvisionDemoMerchantHandler handler,
                DemoMerchantProperties properties) {
            return args -> {
                var merchant = handler.execute(properties.apiKey());
                log.info("Demo merchant provisioned: {}", merchant.id());
            };
        }
    }
}
