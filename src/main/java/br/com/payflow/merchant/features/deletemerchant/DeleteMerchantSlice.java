package br.com.payflow.merchant.features.deletemerchant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DeleteMerchantSlice {

    @Bean
    DeleteMerchantHandler deleteMerchantHandler(MerchantRemoval merchants) {
        return new DeleteMerchantHandler(merchants);
    }
}
