package io.quarkiverse.shedlock.it;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SchedulerLockResourceTest {
    @Inject
    AgroalDataSource defaultDataSource;
    @DataSource("master")
    AgroalDataSource masterDataSource;

    @Test
    public void testShedlockEndpoint() {
        given()
                .when().get("/shedlock")
                .then()
                .statusCode(204);

        final Integer count;
        try (final Connection connection = defaultDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingLock'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testShedlockEndpointOnMaster() {
        given()
                .when().get("/shedlock/master")
                .then()
                .statusCode(204);

        final Integer count;
        try (final Connection connection = masterDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM myShedLockTableName WHERE name = 'io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingLockOnMaster'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @AfterEach
    public void tearDown() {
        try (final Connection connection = defaultDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        try (final Connection connection = masterDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE myShedLockTableName")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
