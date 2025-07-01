package io.quarkiverse.shedlock.common.deployment;

import java.lang.annotation.*;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.shedlock.common.runtime.LockDuration;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface TestSchedulerLock {
    @Nonbinding
    LockDuration lockDuration() default @LockDuration();
}
