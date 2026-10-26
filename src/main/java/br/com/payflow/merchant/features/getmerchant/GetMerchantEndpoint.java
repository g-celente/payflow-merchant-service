package br.com.payflow.merchant.features.getmerchant;

import java.util.UUID;

import br.com.payflow.merchant.api.MerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class GetMerchantEndpoint {

    private final GetMerchantHandler handler;

    GetMerchantEndpoint(GetMerchantHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/v1/merchants/{id}")
    @Operation(summary = "Detalha um lojista")
    MerchantResponse get(@PathVariable UUID id) {
        return MerchantResponse.from(handler.execute(id));
    }
}
