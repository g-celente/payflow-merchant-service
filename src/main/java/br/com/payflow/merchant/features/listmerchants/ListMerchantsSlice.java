package br.com.payflow.merchant.features.listmerchants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ListMerchantsSlice {

    @Bean
    ListMerchantsHandler listMerchantsHandler(MerchantListing merchants) {
        return new ListMerchantsHandler(merchants);
    }
}
