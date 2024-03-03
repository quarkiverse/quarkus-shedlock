package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.lang.annotation.*;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface MongoSchedulerLock {
    @Nonbinding
    String mongoClientName() default MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME;

    @Nonbinding
    LockDuration lockDuration() default @LockDuration();
}
