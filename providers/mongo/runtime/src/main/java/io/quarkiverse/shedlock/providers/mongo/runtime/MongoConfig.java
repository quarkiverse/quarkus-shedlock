package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.util.Map;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.shedlock.mongo")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MongoConfig {
    String DEFAULT = "<default>";

    /**
     * Mongo client configuration
     */
    @ConfigDocSection
    @WithUnnamedKey(DEFAULT)
    @ConfigDocMapKey("mongoclient")
    Map<String, MongoClientConfig> mongoclients();

    @ConfigGroup
    interface MongoClientConfig {
        /**
         * database name for mongo client (default to shedLock)
         */
        @WithDefault(SchedulerLockInterceptorBase.SHED_LOCK)
        String databaseName();
    }
}
