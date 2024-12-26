package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

import io.quarkus.datasource.common.runtime.DataSourceUtil;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcSchedulerLockExecutor {
    String dataSourceName() default DataSourceUtil.DEFAULT_DATASOURCE_NAME;

    String lockAtMostFor() default "";

    String lockAtLeastFor() default "";
}
