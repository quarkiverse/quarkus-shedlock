package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.common.runtime.SchedulerLockInterceptorBase;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class JdbcLockProviderInitializer {
    private final JdbcConfig jdbcConfig;
    private final DataSources dataSources;

    public JdbcLockProviderInitializer(final JdbcConfig jdbcConfig,
            final DataSources dataSources) {
        this.jdbcConfig = Objects.requireNonNull(jdbcConfig);
        this.dataSources = Objects.requireNonNull(dataSources);
    }

    void createTable(@Observes StartupEvent startupEvent) {
        dataSources.dataSourcesName().stream()
                .map(DataSourceName::name)
                .filter(dataSourceName -> Optional.ofNullable(jdbcConfig.dataSources().get(dataSourceName))
                        .map(JdbcConfig.DataSourceConfig::enableTableCreation)
                        .orElse(Boolean.TRUE))
                .forEach(dataSourceName -> {
                    final AgroalDataSource agroalDataSource = Arc.container()
                            .select(AgroalDataSource.class,
                                    DataSourceUtil.DEFAULT_DATASOURCE_NAME.equals(dataSourceName) ? new Default.Literal()
                                            : new DataSource.DataSourceLiteral(dataSourceName))
                            .get();
                    final String databaseCreationSql = """
                            CREATE TABLE IF NOT EXISTS %s (
                              name VARCHAR(255),
                              lock_until TIMESTAMP(3) NULL,
                              locked_at TIMESTAMP(3) NULL,
                              locked_by VARCHAR(255),
                              PRIMARY KEY (name)
                            )
                            """;
                    try (final Connection connection = agroalDataSource.getConnection()) {
                        final String tableName = Optional.ofNullable(jdbcConfig.dataSources().get(dataSourceName))
                                .map(JdbcConfig.DataSourceConfig::tableName)
                                .orElse(SchedulerLockInterceptorBase.SHED_LOCK);
                        final PreparedStatement preparedStatement = connection
                                .prepareStatement(String.format(databaseCreationSql, tableName));
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
