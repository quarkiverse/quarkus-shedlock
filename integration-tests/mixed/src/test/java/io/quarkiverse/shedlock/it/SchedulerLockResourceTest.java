package io.quarkiverse.shedlock.it;

import static com.mongodb.client.model.Filters.eq;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;

import io.agroal.api.AgroalDataSource;
import io.quarkiverse.shedlock.providers.inmemory.runtime.DefaultInMemoryLockProvider;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;

@QuarkusTest
public class SchedulerLockResourceTest {
    @Inject
    AgroalDataSource agroalDataSource;
    @Inject
    MongoClient mongoClient;
    @InjectSpy
    DefaultInMemoryLockProvider defaultInMemoryLockProvider;

    @Test
    public void testShedlockInMemoryEndpoint() {
        given()
                .when().get("/shedlock/in-memory")
                .then()
                .statusCode(204);

        verify(defaultInMemoryLockProvider, times(1)).lock(any());
    }

    @Test
    public void testShedlockJdbcEndpoint() {
        given()
                .when().get("/shedlock/jdbc")
                .then()
                .statusCode(204);

        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement countLocksStatement = connection.prepareStatement(
                        "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingJdbcLock'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testShedlockMongoEndpoint() {
        given()
                .when().get("/shedlock/mongo")
                .then()
                .statusCode(204);

        assertThat(mongoClient.getDatabase("shedLock")
                .getCollection("shedLock")
                .countDocuments(eq("_id", "io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingMongoLock")))
                .isEqualTo(1L);
    }

    @AfterEach
    public void tearDown() {
        try (final Connection connection = agroalDataSource.getConnection();
                final PreparedStatement truncateStatement = connection.prepareStatement(
                        "TRUNCATE TABLE shedlock")) {
            truncateStatement.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        mongoClient.getDatabase("shedLock").drop();
    }
}
