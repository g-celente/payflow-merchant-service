package br.com.payflow.merchant.features.updatemerchant;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;

public final class UpdateMerchantHandler {

    private final MerchantProfileStore merchants;
    private final Clock clock;

    public UpdateMerchantHandler(MerchantProfileStore merchants, Clock clock) {
        this.merchants = Objects.requireNonNull(merchants);
        this.clock = Objects.requireNonNull(clock);
    }

    public Merchant execute(UpdateMerchantCommand command) {
        Merchant updated = merchants.findById(command.id())
                .orElseThrow(() -> new MerchantNotFoundException(command.id()))
                .withProfile(command.name(), command.email(), command.status(), Instant.now(clock));
        merchants.replace(updated);
        return updated;
    }
}
