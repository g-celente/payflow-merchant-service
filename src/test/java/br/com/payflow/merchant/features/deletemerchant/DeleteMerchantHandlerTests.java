package br.com.payflow.merchant.features.deletemerchant;

import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteMerchantHandlerTests {

    private final Merchant existing = merchant("12345678000190");
    private final InMemoryMerchantStore store = new InMemoryMerchantStore().with(existing);
    private final DeleteMerchantHandler handler = new DeleteMerchantHandler(store);

    @Test
    void removesTheMerchant() {
        handler.execute(existing.id());
        assertThat(store.stored(existing.id())).isEmpty();
    }

    @Test
    void secondDeleteIsNotFound() {
        handler.execute(existing.id());
        assertThatThrownBy(() -> handler.execute(existing.id())).isInstanceOf(MerchantNotFoundException.class);
        assertThatThrownBy(() -> handler.execute(UUID.randomUUID())).isInstanceOf(MerchantNotFoundException.class);
    }
}
