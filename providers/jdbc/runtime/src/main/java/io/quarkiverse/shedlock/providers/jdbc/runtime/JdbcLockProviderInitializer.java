package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class JdbcLockProviderInitializer {
    private final JdbcConfig jdbcConfig;

    public JdbcLockProviderInitializer(final JdbcConfig jdbcConfig) {
        this.jdbcConfig = Objects.requireNonNull(jdbcConfig);
    }

    void createTable(@Observes StartupEvent startupEvent) {
        jdbcConfig.datasources().forEach((dataSourceName, dataSourceConfig) -> {
            final AgroalDataSource agroalDataSource = Arc.container()
                    .select(AgroalDataSource.class, JdbcConfig.DEFAULT.equals(dataSourceName) ? new Default.Literal()
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
                final PreparedStatement preparedStatement = connection
                        .prepareStatement(String.format(databaseCreationSql, dataSourceConfig.tableName()));
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
