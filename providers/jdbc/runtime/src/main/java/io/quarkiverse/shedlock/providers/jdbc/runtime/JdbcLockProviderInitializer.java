package io.quarkiverse.shedlock.providers.jdbc.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class JdbcLockProviderInitializer {
    private final AgroalDataSource agroalDataSource;
    private final JdbcConfig jdbcConfig;

    public JdbcLockProviderInitializer(final AgroalDataSource agroalDataSource,
            final JdbcConfig jdbcConfig) {
        this.agroalDataSource = Objects.requireNonNull(agroalDataSource);
        this.jdbcConfig = Objects.requireNonNull(jdbcConfig);
    }

    void createTable(@Observes StartupEvent startupEvent) {
        final String databaseCreationSql = """
                CREATE TABLE IF NOT EXISTS %s (
                  name VARCHAR(64),
                  lock_until TIMESTAMP(3) NULL,
                  locked_at TIMESTAMP(3) NULL,
                  locked_by VARCHAR(255),
                  PRIMARY KEY (name)
                )
                """;
        try (final Connection connection = agroalDataSource.getConnection()) {
            final PreparedStatement preparedStatement = connection
                    .prepareStatement(String.format(databaseCreationSql, jdbcConfig.tableName()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
