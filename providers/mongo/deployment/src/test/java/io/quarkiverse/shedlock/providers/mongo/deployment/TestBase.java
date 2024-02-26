package io.quarkiverse.shedlock.providers.mongo.deployment;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public abstract class TestBase {
    @Inject
    MongoClient mongoClient;
    @Inject
    MongoDatabase mongoDatabase;

    @AfterEach
    public void tearDown() {
        mongoClient.getDatabase(mongoDatabase.getName()).drop();
    }
}
