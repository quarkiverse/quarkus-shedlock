package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

public class MongoLockProviderProducer {
    @ApplicationScoped
    @Produces
    public MongoDatabase mongoDatabaseProducer(
            @ConfigProperty(name = "quarkus.mongodb.database") final Optional<String> quarkusMongodbDatabase,
            final MongoClient mongoClient) {
        final String database = quarkusMongodbDatabase.orElse("shedLock");
        return mongoClient.getDatabase(database);
    }

    @ApplicationScoped
    @Produces
    public LockProvider lockProvider(final MongoDatabase mongoDatabase) {
        return new MongoLockProvider(mongoDatabase);
    }
}
