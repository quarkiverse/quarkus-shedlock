package io.quarkiverse.shedlock.providers.jdbc.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.agroal.api.AgroalDataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;

public class JdbcLockProviderProducer {
    @ApplicationScoped
    @Produces
    public LockProvider lockProvider(final AgroalDataSource agroalDataSource,
            final JdbcConfig jdbcConfig) {
        return new JdbcLockProvider(agroalDataSource, jdbcConfig.tableName());
    }
}
