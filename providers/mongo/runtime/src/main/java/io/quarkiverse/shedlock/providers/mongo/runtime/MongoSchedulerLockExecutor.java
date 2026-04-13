package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSchedulerLockExecutor {
    String mongoClientName() default io.quarkus.mongodb.runtime.MongoConfig.DEFAULT_CLIENT_NAME;
}
