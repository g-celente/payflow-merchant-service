package br.com.payflow.merchant.infrastructure.web;

import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Domain errors shared by several slices, as RFC 9457 Problem Details. Slice-specific errors stay in their endpoint.
@RestControllerAdvice
class ProblemDetailsAdvice {

    @ExceptionHandler(MerchantNotFoundException.class)
    ProblemDetail notFound(MerchantNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    ProblemDetail invalid(DomainValidationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
