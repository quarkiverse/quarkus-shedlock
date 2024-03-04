package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.function.Supplier;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class DataSourceNameRecorder {
    public Supplier<DataSourceName> dataSourceNameSupplier(final String dataSourceName) {
        return () -> (DataSourceName) () -> dataSourceName;
    }
}
