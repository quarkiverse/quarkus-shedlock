package io.quarkiverse.shedlock.it;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class ShedlockResourceTest {
    @InjectSpy
    LockProvider lockProvider;

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

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/shedlock")
                .then()
                .statusCode(204);

        verify(lockProvider, times(1)).lock(any());

        final Integer count;
        try (final Connection connection = agroalDataSource.getConnection();
             final PreparedStatement countLocksStatement = connection.prepareStatement(
                     "SELECT COUNT(*) AS count FROM shedlock WHERE name = 'lockable'")) {
            final ResultSet countLocksResultSet = countLocksStatement.executeQuery();
            countLocksResultSet.next();
            count = countLocksResultSet.getInt("count");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(count).isEqualTo(1);
    }
    TODO faire mongo puis la doc et voila !
}
