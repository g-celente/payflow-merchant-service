package br.com.payflow.merchant.features.updatemerchant;

import java.util.UUID;

import br.com.payflow.merchant.api.MerchantResponse;
import br.com.payflow.merchant.domain.MerchantStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class UpdateMerchantEndpoint {

    private final UpdateMerchantHandler handler;

    UpdateMerchantEndpoint(UpdateMerchantHandler handler) {
        this.handler = handler;
    }

    @PutMapping("/v1/merchants/{id}")
    @Operation(summary = "Altera nome, e-mail e status de um lojista")
    MerchantResponse update(@PathVariable UUID id, @RequestBody UpdateMerchantRequest request) {
        return MerchantResponse.from(handler.execute(
                new UpdateMerchantCommand(id, request.name(), request.email(), request.status())));
    }

    record UpdateMerchantRequest(String name, String email, MerchantStatus status) {
    }
}
