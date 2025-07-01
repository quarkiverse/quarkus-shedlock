package io.quarkiverse.shedlock.providers.inmemory.runtime;

import java.lang.annotation.*;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.shedlock.common.runtime.LockDuration;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface InMemorySchedulerLock {
    @Nonbinding
    LockDuration lockDuration() default @LockDuration();
}
