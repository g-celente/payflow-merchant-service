package br.com.payflow.merchant.features.authenticate;

import br.com.payflow.merchant.api.ApiSecurity;
import br.com.payflow.merchant.api.MerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// Driving adapter of the authentication slice: GET /v1/merchants/me.
@RestController
@Tag(name = "Lojistas")
class CurrentMerchantEndpoint {

    private final AuthenticateMerchantHandler handler;

    CurrentMerchantEndpoint(AuthenticateMerchantHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/v1/merchants/me")
    @Operation(summary = "Lojista dono da chave X-Merchant-Key (usado pelo BFF para autenticar)")
    @SecurityRequirement(name = ApiSecurity.MERCHANT_KEY_SCHEME)
    MerchantResponse currentMerchant(
            @RequestHeader(name = ApiSecurity.MERCHANT_KEY_HEADER, required = false) String apiKey) {
        return MerchantResponse.from(handler.execute(apiKey));
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    ResponseEntity<ProblemDetail> invalidApiKey(InvalidApiKeyException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problem.setTitle("Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "ApiKey header=\"" + ApiSecurity.MERCHANT_KEY_HEADER + "\"")
                .body(problem);
    }
}
