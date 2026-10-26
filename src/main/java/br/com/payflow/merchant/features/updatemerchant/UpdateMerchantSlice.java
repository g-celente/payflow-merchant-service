package br.com.payflow.merchant.features.updatemerchant;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UpdateMerchantSlice {

    @Bean
    UpdateMerchantHandler updateMerchantHandler(MerchantProfileStore merchants, Clock clock) {
        return new UpdateMerchantHandler(merchants, clock);
    }
}
