package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.Map;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.shedlock.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JdbcConfig {
    /**
     * data sources configuration
     */
    @ConfigDocSection
    @ConfigDocMapKey("datasource-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(DataSourceUtil.DEFAULT_DATASOURCE_NAME)
    Map<String, DataSourceConfig> dataSources();

    @ConfigGroup
    interface DataSourceConfig {
        /**
         * table name for datasource (default to shedLock)
         */
        @WithDefault(SchedulerLockInterceptorBase.SHED_LOCK)
        String tableName();
    }
}
