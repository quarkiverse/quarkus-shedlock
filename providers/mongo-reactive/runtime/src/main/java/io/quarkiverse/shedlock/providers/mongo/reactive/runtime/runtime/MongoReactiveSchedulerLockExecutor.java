package io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

import io.quarkus.mongodb.runtime.MongoClientBeanUtil;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoReactiveSchedulerLockExecutor {
    String mongoClientName() default MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME;
}
