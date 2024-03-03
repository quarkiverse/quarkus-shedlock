package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.Map;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.shedlock.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JdbcConfig {
    String DEFAULT = "<default>";

    /**
     * data sources configuration
     */
    @ConfigDocSection
    @WithUnnamedKey(DEFAULT)
    @ConfigDocMapKey("datasource")
    Map<String, DataSourceConfig> datasources();

    @ConfigGroup
    interface DataSourceConfig {
        /**
         * table name for datasource (default to shedLock)
         */
        @WithDefault(SchedulerLockInterceptorBase.SHED_LOCK)
        String tableName();
    }
}
