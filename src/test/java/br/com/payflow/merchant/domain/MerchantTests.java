package br.com.payflow.merchant.domain;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static br.com.payflow.merchant.support.TestMerchants.CREATED;
import static br.com.payflow.merchant.support.TestMerchants.KEY_HASH;
import static br.com.payflow.merchant.support.TestMerchants.NOW;
import static br.com.payflow.merchant.support.TestMerchants.webhook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantTests {

    private static Merchant register(String name, String email, String document) {
        return Merchant.register(UUID.randomUUID(), name, email, document, KEY_HASH, CREATED);
    }

    @Test
    void registeredMerchantIsActiveWithoutWebhook() {
        Merchant merchant = register("  Loja Exemplo ", "contato@loja.com", "12345678000190");
        assertThat(merchant.status()).isEqualTo(MerchantStatus.ACTIVE);
        assertThat(merchant.isActive()).isTrue();
        assertThat(merchant.hasWebhook()).isFalse();
        assertThat(merchant.name()).isEqualTo("Loja Exemplo");
        assertThat(merchant.createdAt()).isEqualTo(merchant.updatedAt()).isEqualTo(CREATED);
    }

    @Test
    void acceptsCpfAndCnpjDigitsOnly() {
        assertThat(register("Loja", "a@b.co", "12345678901").document()).hasSize(11);
        for (String document : new String[]{null, "", "1234567890", "123456789012", "12.345.678/0001-90", "1234567800019a"}) {
            assertThatThrownBy(() -> register("Loja", "a@b.co", document))
                    .isInstanceOf(DomainValidationException.class).hasMessageContaining("CPF");
        }
    }

    @Test
    void rejectsInvalidNameAndEmail() {
        for (String name : new String[]{null, "", "   ", "x".repeat(Merchant.MAX_NAME_LENGTH + 1)}) {
            assertThatThrownBy(() -> register(name, "a@b.co", "12345678901")).isInstanceOf(DomainValidationException.class);
        }
        for (String email : new String[]{null, "", "loja", "loja@", "@loja.com", "loja @x.com"}) {
            assertThatThrownBy(() -> register("Loja", email, "12345678901")).isInstanceOf(DomainValidationException.class);
        }
    }

    @Test
    void statusIsRequired() {
        Merchant merchant = register("Loja", "a@b.co", "12345678901");
        assertThatThrownBy(() -> merchant.withProfile("Loja", "a@b.co", null, NOW))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("status");
    }

    @Test
    void changesKeepIdentityDocumentAndCreationTime() {
        Merchant original = register("Loja", "a@b.co", "12345678901").withWebhook(webhook(), CREATED);
        Merchant updated = original.withProfile("Loja Nova", "novo@b.co", MerchantStatus.SUSPENDED, NOW)
                .withApiKeyHash("f".repeat(64), NOW);
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.document()).isEqualTo(original.document());
        assertThat(updated.webhook()).isEqualTo(original.webhook());
        assertThat(updated.createdAt()).isEqualTo(CREATED);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    void textNeverShowsCredentials() {
        Merchant merchant = register("Loja", "a@b.co", "12345678901").withWebhook(webhook(), NOW);
        assertThat(merchant.toString()).doesNotContain(KEY_HASH).doesNotContain(merchant.webhook().secret());
    }
}
