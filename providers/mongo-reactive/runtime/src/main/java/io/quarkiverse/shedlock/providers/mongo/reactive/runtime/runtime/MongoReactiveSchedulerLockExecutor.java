package io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoReactiveSchedulerLockExecutor {
    String mongoClientName() default io.quarkus.mongodb.runtime.MongoConfig.DEFAULT_REACTIVE_CLIENT_NAME;
}
