package io.quarkiverse.it.shedlock.mongo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MongoClientLockableResourceTest {
    @Test
    void shouldLockUsingMongoDefaultStorageUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            given()
                    .when().post("/mongoStorageLockableResource/interceptor/default")
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
    void shouldLockUsingMongoClusterOneStorageUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            given()
                    .when().post("/mongoStorageLockableResource/interceptor/clusterOne")
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
    void shouldLockUsingMongoDefaultStorageUsingExecutor() {
        for (int execution = 0; execution < 5; execution++) {
            if (execution == 0) {
                given()
                        .when().post("/mongoStorageLockableResource/execute/default")
                        .then()
                        .statusCode(200)
                        .body("executed", is(true))
                        .body("result", is(1));
            } else {
                given()
                        .when().post("/mongoStorageLockableResource/execute/default")
                        .then()
                        .statusCode(200)
                        .body("executed", is(false))
                        .body("result", nullValue());
            }
        }

        given()
                .when().get("/mongoStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingMongoClusterOneStorageUsingExecutor() {
        for (int execution = 0; execution < 5; execution++) {
            if (execution == 0) {
                given()
                        .when().post("/mongoStorageLockableResource/execute/clusterOne")
                        .then()
                        .statusCode(200)
                        .body("executed", is(true))
                        .body("result", is(1));
            } else {
                given()
                        .when().post("/mongoStorageLockableResource/execute/clusterOne")
                        .then()
                        .statusCode(200)
                        .body("executed", is(false))
                        .body("result", nullValue());
            }
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
