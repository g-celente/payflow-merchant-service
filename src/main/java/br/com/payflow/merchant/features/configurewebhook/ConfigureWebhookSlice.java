package br.com.payflow.merchant.features.configurewebhook;

import java.time.Clock;

import br.com.payflow.merchant.domain.CredentialGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ConfigureWebhookSlice {

    @Bean
    ConfigureWebhookHandler configureWebhookHandler(WebhookConfigurationStore merchants,
            CredentialGenerator credentials, Clock clock) {
        return new ConfigureWebhookHandler(merchants, credentials, clock);
    }
}
