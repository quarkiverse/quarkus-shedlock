package io.quarkiverse.shedlock.common.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface LockDuration {
    @Nonbinding
    String lockAtMostFor() default "";

    @Nonbinding
    String lockAtLeastFor() default "";
}
