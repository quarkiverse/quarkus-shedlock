package io.quarkiverse.shedlock.providers.mongo.runtime;

import java.util.Map;

import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkus.mongodb.runtime.MongoClientBeanUtil;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.shedlock.mongo")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MongoConfig {
    /**
     * Mongo client configuration
     */
    @ConfigDocSection
    @ConfigDocMapKey("mongoclient-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(MongoClientBeanUtil.DEFAULT_MONGOCLIENT_NAME)
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
