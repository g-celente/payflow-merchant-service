package br.com.payflow.merchant.infrastructure.web;

import br.com.payflow.merchant.api.ApiSecurity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InternalKeyProperties.class)
public class WebConfiguration {

    @Bean
    FilterRegistrationBean<InternalKeyFilter> internalKeyFilter(InternalKeyProperties properties) {
        var registration = new FilterRegistrationBean<>(new InternalKeyFilter(properties.internalKey()));
        registration.addUrlPatterns("/v1/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    // Swagger at /swagger-ui.html: internal key by default, merchant key only on /v1/merchants/me.
    @Bean
    OpenAPI merchantServiceOpenApi() {
        return new OpenAPI()
                .info(new Info().title("PayFlow merchant-service").version("v1")
                        .description("Domínio de lojistas do PayFlow: cadastro, chave de acesso e webhook."))
                .components(new Components()
                        .addSecuritySchemes(ApiSecurity.INTERNAL_KEY_SCHEME, headerKey(ApiSecurity.INTERNAL_KEY_HEADER))
                        .addSecuritySchemes(ApiSecurity.MERCHANT_KEY_SCHEME, headerKey(ApiSecurity.MERCHANT_KEY_HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(ApiSecurity.INTERNAL_KEY_SCHEME));
    }

    private static SecurityScheme headerKey(String header) {
        return new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name(header);
    }
}
