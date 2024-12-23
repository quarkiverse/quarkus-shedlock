package io.quarkiverse.it.shedlock.mongo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MongoClientLockableResourceTest {
    @Test
    void shouldLockUsingMongoDefaultStorage() {
        for (int called = 0; called < 5; called++) {
            given()
                    .when().post("/mongoStorageLockableResource/default")
                    .then()
                    .statusCode(204);
        }

        given()
                .when().get("/mongoStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingMongoClusterOneStorage() {
        for (int called = 0; called < 5; called++) {
            given()
                    .when().post("/mongoStorageLockableResource/clusterOne")
                    .then()
                    .statusCode(204);
        }

        given()
                .when().get("/mongoStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        given()
                .when().post("/mongoStorageLockableResource/reset")
                .then()
                .log().all()
                .statusCode(204);
    }
}
