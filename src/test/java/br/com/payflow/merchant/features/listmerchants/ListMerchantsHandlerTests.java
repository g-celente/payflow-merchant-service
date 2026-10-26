package br.com.payflow.merchant.features.listmerchants;

import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantStatus;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CREATED;
import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListMerchantsHandlerTests {

    private final Merchant oldest = merchant("11111111111");
    private final Merchant middle = merchant("22222222222")
            .withProfile("Loja B", "b@loja.com", MerchantStatus.SUSPENDED, CREATED);
    private final Merchant newest = merchant("33333333333");
    private final ListMerchantsHandler handler = new ListMerchantsHandler(new InMemoryMerchantStore().with(
            oldest,
            new Merchant(middle.id(), middle.name(), middle.email(), middle.document(), middle.apiKeyHash(),
                    middle.status(), null, CREATED.plusSeconds(60), CREATED.plusSeconds(60)),
            new Merchant(newest.id(), newest.name(), newest.email(), newest.document(), newest.apiKeyHash(),
                    newest.status(), null, CREATED.plusSeconds(120), CREATED.plusSeconds(120))));

    @Test
    void pagesNewestFirst() {
        MerchantPage first = handler.execute(new ListMerchantsQuery(null, 0, 2));
        assertThat(first.items()).extracting(Merchant::id).containsExactly(newest.id(), middle.id());
        assertThat(first.totalItems()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(handler.execute(new ListMerchantsQuery(null, 1, 2)).items()).extracting(Merchant::id)
                .containsExactly(oldest.id());
    }

    @Test
    void filtersByStatus() {
        MerchantPage suspended = handler.execute(new ListMerchantsQuery(MerchantStatus.SUSPENDED, 0, 20));
        assertThat(suspended.items()).extracting(Merchant::id).containsExactly(middle.id());
        assertThat(suspended.totalPages()).isEqualTo(1);
    }

    @Test
    void pageAndSizeAreBounded() {
        assertThatThrownBy(() -> new ListMerchantsQuery(null, -1, 20)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new ListMerchantsQuery(null, 0, 0)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new ListMerchantsQuery(null, 0, ListMerchantsQuery.MAX_SIZE + 1))
                .isInstanceOf(DomainValidationException.class);
    }
}
