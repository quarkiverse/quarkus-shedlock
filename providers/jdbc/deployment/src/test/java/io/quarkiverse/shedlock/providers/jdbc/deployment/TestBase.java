package io.quarkiverse.shedlock.providers.jdbc.deployment;

import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
