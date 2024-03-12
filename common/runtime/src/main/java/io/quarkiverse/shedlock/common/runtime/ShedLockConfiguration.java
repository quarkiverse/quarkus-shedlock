package io.quarkiverse.shedlock.common.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.shedlock")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ShedLockConfiguration {
    /**
     * how long the lock should be kept in case the executing node dies
     */
    String defaultsLockAtMostFor();

    /**
     * value which is much longer than normal execution time
     */
    Optional<String> defaultsLockAtLeastFor();
}
