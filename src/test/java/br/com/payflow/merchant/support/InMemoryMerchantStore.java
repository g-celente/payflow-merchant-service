package br.com.payflow.merchant.support;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.payflow.merchant.domain.Merchant;
import br.com.payflow.merchant.domain.MerchantNotFoundException;
import br.com.payflow.merchant.features.authenticate.MerchantLookup;
import br.com.payflow.merchant.features.configurewebhook.WebhookConfigurationStore;
import br.com.payflow.merchant.features.createmerchant.DocumentAlreadyRegisteredException;
import br.com.payflow.merchant.features.createmerchant.NewMerchantStore;
import br.com.payflow.merchant.features.deletemerchant.MerchantRemoval;
import br.com.payflow.merchant.features.getmerchant.MerchantReader;
import br.com.payflow.merchant.features.getwebhook.WebhookReader;
import br.com.payflow.merchant.features.listmerchants.ListMerchantsQuery;
import br.com.payflow.merchant.features.listmerchants.MerchantListing;
import br.com.payflow.merchant.features.listmerchants.MerchantPage;
import br.com.payflow.merchant.features.rotateapikey.ApiKeyRotationStore;
import br.com.payflow.merchant.features.updatemerchant.MerchantProfileStore;

// Test double for every slice port, with the same contracts as the MongoDB adapter.
public final class InMemoryMerchantStore implements MerchantLookup, NewMerchantStore, MerchantListing, MerchantReader,
        MerchantProfileStore, MerchantRemoval, WebhookConfigurationStore, WebhookReader, ApiKeyRotationStore {

    private final Map<UUID, Merchant> merchants = new LinkedHashMap<>();

    public InMemoryMerchantStore with(Merchant... initial) {
        for (Merchant merchant : initial) {
            merchants.put(merchant.id(), merchant);
        }
        return this;
    }

    public Optional<Merchant> stored(UUID id) {
        return Optional.ofNullable(merchants.get(id));
    }

    public int size() {
        return merchants.size();
    }

    @Override
    public Optional<Merchant> findByApiKeyHash(String apiKeyHash) {
        return merchants.values().stream().filter(m -> m.apiKeyHash().equals(apiKeyHash)).findFirst();
    }

    @Override
    public void add(Merchant merchant) {
        if (merchants.values().stream().anyMatch(m -> m.document().equals(merchant.document()))) {
            throw new DocumentAlreadyRegisteredException();
        }
        merchants.put(merchant.id(), merchant);
    }

    @Override
    public MerchantPage list(ListMerchantsQuery query) {
        var matching = merchants.values().stream()
                .filter(m -> query.status() == null || m.status() == query.status())
                .sorted(Comparator.comparing(Merchant::createdAt).reversed())
                .toList();
        var page = matching.stream().skip((long) query.page() * query.size()).limit(query.size()).toList();
        return new MerchantPage(page, query.page(), query.size(), matching.size());
    }

    @Override
    public Optional<Merchant> findById(UUID id) {
        return stored(id);
    }

    @Override
    public void replace(Merchant merchant) {
        if (!merchants.containsKey(merchant.id())) {
            throw new MerchantNotFoundException(merchant.id());
        }
        merchants.put(merchant.id(), merchant);
    }

    @Override
    public boolean deleteById(UUID id) {
        return merchants.remove(id) != null;
    }
}
