package br.com.payflow.merchant.infrastructure.mongodb;

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
import br.com.payflow.merchant.features.provision.MerchantProvisioning;
import br.com.payflow.merchant.features.rotateapikey.ApiKeyRotationStore;
import br.com.payflow.merchant.features.updatemerchant.MerchantProfileStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

// One MongoDB adapter for the merchants collection, exposed to each slice through its own port.
public final class MongoMerchantStore implements MerchantProvisioning, MerchantLookup, NewMerchantStore,
        MerchantListing, MerchantReader, MerchantProfileStore, MerchantRemoval, WebhookConfigurationStore,
        WebhookReader, ApiKeyRotationStore {

    static final String API_KEY_INDEX = "merchant_api_key_unique";
    static final String DOCUMENT_INDEX = "merchant_document_unique";
    static final String STATUS_INDEX = "merchant_status_created";

    private final MongoTemplate mongo;

    public MongoMerchantStore(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Merchant insertIfAbsent(Merchant candidate) {
        mongo.findAndModify(byId(candidate.id()),
                new Update().setOnInsert("name", candidate.name())
                        .setOnInsert("email", candidate.email())
                        .setOnInsert("document", candidate.document())
                        .setOnInsert("apiKeyHash", candidate.apiKeyHash())
                        .setOnInsert("status", candidate.status().name())
                        .setOnInsert("createdAt", candidate.createdAt())
                        .setOnInsert("updatedAt", candidate.updatedAt()),
                FindAndModifyOptions.options().upsert(true).returnNew(true), MerchantDocument.class);
        // A demo merchant created before e-mail, document and status existed gets them once.
        mongo.updateFirst(byId(candidate.id()).addCriteria(Criteria.where("document").exists(false)),
                new Update().set("email", candidate.email())
                        .set("document", candidate.document())
                        .set("status", candidate.status().name())
                        .set("updatedAt", candidate.updatedAt()),
                MerchantDocument.class);
        return findById(candidate.id()).orElseThrow(() -> new MerchantNotFoundException(candidate.id()));
    }

    @Override
    public Optional<Merchant> findByApiKeyHash(String apiKeyHash) {
        return Optional.ofNullable(mongo.findOne(
                Query.query(Criteria.where("apiKeyHash").is(apiKeyHash)), MerchantDocument.class))
                .map(MerchantMapper::toDomain);
    }

    @Override
    public void add(Merchant merchant) {
        try {
            mongo.insert(MerchantMapper.toDocument(merchant));
        } catch (DuplicateKeyException exception) {
            if (String.valueOf(exception.getMessage()).contains(DOCUMENT_INDEX)) {
                throw new DocumentAlreadyRegisteredException();
            }
            throw exception;
        }
    }

    @Override
    public MerchantPage list(ListMerchantsQuery query) {
        Query filter = query.status() == null
                ? new Query()
                : Query.query(Criteria.where("status").is(query.status().name()));
        long total = mongo.count(filter, MerchantDocument.class);
        filter.with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .skip((long) query.page() * query.size())
                .limit(query.size());
        var items = mongo.find(filter, MerchantDocument.class).stream().map(MerchantMapper::toDomain).toList();
        return new MerchantPage(items, query.page(), query.size(), total);
    }

    @Override
    public Optional<Merchant> findById(UUID id) {
        return Optional.ofNullable(mongo.findById(id, MerchantDocument.class)).map(MerchantMapper::toDomain);
    }

    @Override
    public void replace(Merchant merchant) {
        if (mongo.replace(byId(merchant.id()), MerchantMapper.toDocument(merchant)).getMatchedCount() == 0) {
            throw new MerchantNotFoundException(merchant.id());
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        return mongo.remove(byId(id), MerchantDocument.class).getDeletedCount() > 0;
    }

    private static Query byId(UUID id) {
        return Query.query(Criteria.where("_id").is(id));
    }
}
