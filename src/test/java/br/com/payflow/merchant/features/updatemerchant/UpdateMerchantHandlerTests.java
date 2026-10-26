package br.com.payflow.merchant.features.updatemerchant;

import java.util.UUID;

import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.domain.MerchantStatus;
import br.com.payflow.merchant.support.InMemoryMerchantStore;
import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CLOCK;
import static br.com.payflow.merchant.support.TestMerchants.CREATED;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static br.com.payflow.merchant.support.TestMerchants.merchant;
import static br.com.payflow.merchant.support.TestMerchants.webhook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateMerchantHandlerTests {

    private final Merchant existing = merchant("12345678000190").withWebhook(webhook(), CREATED);
    private final InMemoryMerchantStore store = new InMemoryMerchantStore().with(existing);
    private final UpdateMerchantHandler handler = new UpdateMerchantHandler(store, CLOCK);

    @Test
    void replacesTheEditableProfileOnly() {
        Merchant updated = handler.execute(
                new UpdateMerchantCommand(existing.id(), "Loja Nova", "novo@loja.com", MerchantStatus.SUSPENDED));

        assertThat(store.stored(existing.id())).contains(updated);
        assertThat(updated.name()).isEqualTo("Loja Nova");
        assertThat(updated.email()).isEqualTo("novo@loja.com");
        assertThat(updated.status()).isEqualTo(MerchantStatus.SUSPENDED);
        assertThat(updated.document()).isEqualTo(existing.document());
        assertThat(updated.apiKeyHash()).isEqualTo(existing.apiKeyHash());
        assertThat(updated.webhook()).isEqualTo(existing.webhook());
        assertThat(updated.createdAt()).isEqualTo(CREATED);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void invalidProfileLeavesTheMerchantUnchanged() {
        assertThatThrownBy(() -> handler.execute(
                new UpdateMerchantCommand(existing.id(), "Loja", "invalido", MerchantStatus.ACTIVE)))
                .isInstanceOf(DomainValidationException.class);
        assertThat(store.stored(existing.id())).contains(existing);
    }

    @Test
    void unknownIdIsNotFound() {
        assertThatThrownBy(() -> handler.execute(
                new UpdateMerchantCommand(UUID.randomUUID(), "Loja", "a@b.co", MerchantStatus.ACTIVE)))
                .isInstanceOf(MerchantNotFoundException.class);
    }
}
