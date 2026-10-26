package br.com.payflow.merchant.features.configurewebhook;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import br.com.payflow.merchant.domain.CredentialGenerator;
import br.com.payflow.merchant.domain.DomainValidationException;
import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.domain.WebhookEvent;
import br.com.payflow.merchant.domain.WebhookSubscription;

public final class ConfigureWebhookHandler {

    private final WebhookConfigurationStore merchants;
    private final CredentialGenerator credentials;
    private final Clock clock;

    public ConfigureWebhookHandler(WebhookConfigurationStore merchants, CredentialGenerator credentials, Clock clock) {
        this.merchants = Objects.requireNonNull(merchants);
        this.credentials = Objects.requireNonNull(credentials);
        this.clock = Objects.requireNonNull(clock);
    }

    public WebhookSubscription execute(ConfigureWebhookCommand command) {
        Merchant merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> new MerchantNotFoundException(command.merchantId()));
        String secret = command.secret() == null || command.secret().isBlank()
                ? credentials.newWebhookSecret()
                : command.secret();
        WebhookSubscription webhook = new WebhookSubscription(command.url(), secret, events(command));
        merchants.replace(merchant.withWebhook(webhook, Instant.now(clock)));
        return webhook;
    }

    private static Set<WebhookEvent> events(ConfigureWebhookCommand command) {
        if (command.events() == null || command.events().isEmpty()) {
            throw new DomainValidationException("Subscribe to at least one webhook event");
        }
        Set<WebhookEvent> events = EnumSet.noneOf(WebhookEvent.class);
        command.events().forEach(name -> events.add(WebhookEvent.fromWireName(name)));
        return events;
    }
}
