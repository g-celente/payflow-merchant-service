package br.com.payflow.merchant.features.deletemerchant;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class DeleteMerchantEndpoint {

    private final DeleteMerchantHandler handler;

    DeleteMerchantEndpoint(DeleteMerchantHandler handler) {
        this.handler = handler;
    }

    @DeleteMapping("/v1/merchants/{id}")
    @Operation(summary = "Remove um lojista")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        handler.execute(id);
        return ResponseEntity.noContent().build();
    }
}
