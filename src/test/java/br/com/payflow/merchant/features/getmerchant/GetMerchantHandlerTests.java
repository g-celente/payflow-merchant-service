package br.com.payflow.merchant.features.getmerchant;

import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetMerchantHandlerTests {

    private final Merchant existing = merchant("12345678000190");
    private final GetMerchantHandler handler = new GetMerchantHandler(new InMemoryMerchantStore().with(existing));

    @Test
    void returnsTheMerchant() {
        assertThat(handler.execute(existing.id())).isEqualTo(existing);
    }

    @Test
    void unknownIdIsNotFound() {
        UUID unknown = UUID.randomUUID();
        assertThatThrownBy(() -> handler.execute(unknown))
                .isInstanceOf(MerchantNotFoundException.class).hasMessageContaining(unknown.toString());
    }
}
