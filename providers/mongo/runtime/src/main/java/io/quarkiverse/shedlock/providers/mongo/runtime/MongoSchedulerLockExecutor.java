package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

import io.quarkus.mongodb.runtime.MongoClientBeanUtil;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSchedulerLockExecutor {
    String mongoClientName() default MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME;

    String lockAtMostFor() default "";

    String lockAtLeastFor() default "";
}
