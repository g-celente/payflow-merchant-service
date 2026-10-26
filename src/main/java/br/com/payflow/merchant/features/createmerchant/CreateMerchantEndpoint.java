package br.com.payflow.merchant.features.createmerchant;

import java.net.URI;

import br.com.payflow.merchant.api.MerchantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Lojistas")
class CreateMerchantEndpoint {

    private final CreateMerchantHandler handler;

    CreateMerchantEndpoint(CreateMerchantHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/v1/merchants")
    @Operation(summary = "Cadastra um lojista e devolve a API key uma única vez")
    ResponseEntity<CreateMerchantResponse> create(@RequestBody CreateMerchantRequest request) {
        CreatedMerchant created = handler.execute(
                new CreateMerchantCommand(request.name(), request.email(), request.document()));
        return ResponseEntity.created(URI.create("/v1/merchants/" + created.merchant().id()))
                .body(new CreateMerchantResponse(MerchantResponse.from(created.merchant()), created.apiKey()));
    }

    @ExceptionHandler(DocumentAlreadyRegisteredException.class)
    ProblemDetail documentTaken(DocumentAlreadyRegisteredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Conflict");
        return problem;
    }

    record CreateMerchantRequest(String name, String email, String document) {
    }

    record CreateMerchantResponse(MerchantResponse merchant, String apiKey) {
    }
}
