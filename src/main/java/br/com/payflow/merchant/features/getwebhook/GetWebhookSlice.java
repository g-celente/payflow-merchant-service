package br.com.payflow.merchant.features.getwebhook;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GetWebhookSlice {

    @Bean
    GetWebhookHandler getWebhookHandler(WebhookReader merchants) {
        return new GetWebhookHandler(merchants);
    }
}
