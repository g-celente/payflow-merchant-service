package br.com.payflow.merchant.features.listmerchants;

import java.util.List;

import br.com.payflow.merchant.api.MerchantResponse;
import br.com.payflow.merchant.domain.MerchantStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class ListMerchantsEndpoint {

    private final ListMerchantsHandler handler;

    ListMerchantsEndpoint(ListMerchantsHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/v1/merchants")
    @Operation(summary = "Lista lojistas, mais recentes primeiro, com filtro opcional por status")
    MerchantPageResponse list(
            @RequestParam(required = false) MerchantStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MerchantPage result = handler.execute(new ListMerchantsQuery(status, page, size));
        return new MerchantPageResponse(result.items().stream().map(MerchantResponse::from).toList(),
                result.page(), result.size(), result.totalItems(), result.totalPages());
    }

    record MerchantPageResponse(List<MerchantResponse> items, int page, int size, long totalItems, long totalPages) {
    }
}
