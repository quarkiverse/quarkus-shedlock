package io.quarkiverse.shedlock.it;

import static com.mongodb.client.model.Filters.eq;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;

import io.quarkus.mongodb.MongoClientName;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SchedulerLockResourceTest {
    @Inject
    MongoClient defaultMongoClient;
    @Inject
    @MongoClientName("cluster1")
    MongoClient clusterOneMongoClient;

    @Test
    public void testShedlockEndpoint() {
        given()
                .when().get("/shedlock")
                .then()
                .statusCode(204);

        assertThat(defaultMongoClient.getDatabase("shedLock")
                .getCollection("shedLock")
                .countDocuments(eq("_id", "io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingLock")))
                .isEqualTo(1L);
    }

    @Test
    public void testShedlockEndpointOnClusterOne() {
        given()
                .when().get("/shedlock/clusterOne")
                .then()
                .statusCode(204);

        assertThat(clusterOneMongoClient.getDatabase("customDatabase")
                .getCollection("shedLock")
                .countDocuments(eq("_id", "io.quarkiverse.shedlock.it.SchedulerLockResource_runUsingLockOnClusterOne")))
                .isEqualTo(1L);
    }

    @AfterEach
    public void tearDown() {
        defaultMongoClient.getDatabase("shedLock").drop();
        clusterOneMongoClient.getDatabase("customDatabase").drop();
    }
}
