package br.com.payflow.merchant.infrastructure.mongodb;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

// Created at startup; a failure stops the application instead of running without uniqueness guarantees.
public final class MerchantIndexes {

    private MerchantIndexes() {
    }

    public static void ensure(MongoTemplate mongo) {
        IndexOperations indexes = mongo.indexOps(MerchantDocument.class);
        indexes.createIndex(new Index().on("apiKeyHash", Sort.Direction.ASC).unique()
                .named(MongoMerchantStore.API_KEY_INDEX));
        // Sparse: a demo document from older deployments has no document until it is provisioned again.
        indexes.createIndex(new Index().on("document", Sort.Direction.ASC).unique().sparse()
                .named(MongoMerchantStore.DOCUMENT_INDEX));
        indexes.createIndex(new Index().on("status", Sort.Direction.ASC).on("createdAt", Sort.Direction.DESC)
                .named(MongoMerchantStore.STATUS_INDEX));
    }
}
