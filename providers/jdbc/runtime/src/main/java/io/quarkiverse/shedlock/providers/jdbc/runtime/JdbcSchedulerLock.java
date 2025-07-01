package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.lang.annotation.*;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import io.quarkiverse.shedlock.common.runtime.LockDuration;
import io.quarkus.datasource.common.runtime.DataSourceUtil;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface JdbcSchedulerLock {
    @Nonbinding
    String dataSourceName() default DataSourceUtil.DEFAULT_DATASOURCE_NAME;

    @Nonbinding
    LockDuration lockDuration() default @LockDuration();
}
