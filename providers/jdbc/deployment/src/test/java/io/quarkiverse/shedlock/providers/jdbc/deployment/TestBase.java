package io.quarkiverse.shedlock.providers.jdbc.deployment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;

import io.agroal.api.AgroalDataSource;

public abstract class TestBase {
    @Inject
    AgroalDataSource agroalDataSource;

    @AfterEach
    public void tearDown() {
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
