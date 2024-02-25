package io.quarkiverse.shedlock.providers.jdbc.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.shedlock.jdbc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JdbcConfig {
    /**
     * table name to store locks (default: shedLock)
     */
    @WithDefault("shedLock")
    String tableName();
}
