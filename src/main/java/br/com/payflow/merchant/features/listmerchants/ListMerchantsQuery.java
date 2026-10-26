package br.com.payflow.merchant.features.listmerchants;

import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.MerchantStatus;

// status null = every status. page starts at 0.
public record ListMerchantsQuery(MerchantStatus status, int page, int size) {

    public static final int MAX_SIZE = 100;

    public ListMerchantsQuery {
        if (page < 0) {
            throw new DomainValidationException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new DomainValidationException("size must be between 1 and " + MAX_SIZE);
        }
    }
}
