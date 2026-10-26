package br.com.payflow.merchant.features.getmerchant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GetMerchantSlice {

    @Bean
    GetMerchantHandler getMerchantHandler(MerchantReader merchants) {
        return new GetMerchantHandler(merchants);
    }
}
