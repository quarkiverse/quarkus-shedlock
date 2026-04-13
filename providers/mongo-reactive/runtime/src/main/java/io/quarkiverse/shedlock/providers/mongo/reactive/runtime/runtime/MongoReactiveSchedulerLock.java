package io.quarkiverse.shedlock.providers.mongo.reactive.runtime.runtime;

import java.lang.annotation.*;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.shedlock.common.runtime.LockDuration;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface MongoReactiveSchedulerLock {
    @Nonbinding
    String mongoClientName() default io.quarkus.mongodb.runtime.MongoConfig.DEFAULT_REACTIVE_CLIENT_NAME;

    @Nonbinding
    LockDuration lockDuration() default @LockDuration();
}
