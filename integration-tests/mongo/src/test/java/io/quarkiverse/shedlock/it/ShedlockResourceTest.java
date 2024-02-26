package io.quarkiverse.shedlock.it;

import static com.mongodb.client.model.Filters.eq;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import net.javacrumbs.shedlock.core.LockProvider;

@QuarkusTest
public class ShedlockResourceTest {
    @InjectSpy
    LockProvider lockProvider;
    @Inject
    MongoClient mongoClient;
    @Inject
    MongoDatabase mongoDatabase;

    @AfterEach
    public void tearDown() {
        mongoClient.getDatabase(mongoDatabase.getName()).drop();
    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/shedlock")
                .then()
                .statusCode(204);

        verify(lockProvider, times(1)).lock(any());

        assertThat(mongoDatabase.getCollection("shedLock").countDocuments(eq("_id", "lockable")))
                .isEqualTo(1L);
    }
}
