package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.Objects;

public record DataSourceName(String name) {
    public DataSourceName {
        Objects.requireNonNull(name);
    }
}
