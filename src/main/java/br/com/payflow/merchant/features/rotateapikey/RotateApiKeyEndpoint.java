package br.com.payflow.merchant.features.rotateapikey;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class RotateApiKeyEndpoint {

    private final RotateApiKeyHandler handler;

    RotateApiKeyEndpoint(RotateApiKeyHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/v1/merchants/{id}/api-key")
    @Operation(summary = "Gera uma nova API key; a anterior deixa de funcionar na hora")
    RotateApiKeyResponse rotate(@PathVariable UUID id) {
        RotatedApiKey rotated = handler.execute(id);
        return new RotateApiKeyResponse(rotated.merchantId(), rotated.apiKey());
    }

    record RotateApiKeyResponse(UUID merchantId, String apiKey) {
    }
}
