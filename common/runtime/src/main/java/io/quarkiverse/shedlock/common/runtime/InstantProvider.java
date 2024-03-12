package io.quarkiverse.shedlock.common.runtime;

import java.time.Instant;

public interface InstantProvider {
    Instant now();
}
