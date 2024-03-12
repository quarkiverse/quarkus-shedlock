package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.util.List;
import java.util.function.Supplier;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class DataSourcesRecorder {
    public Supplier<DataSources> dataSourcesSupplier(final List<DataSourceName> dataSourcesName) {
        return () -> (DataSources) () -> dataSourcesName;
    }
}
