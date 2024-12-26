package io.quarkiverse.it.shedlock.jdbc;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JdbcStorageLockableResourceTest {
    @Test
    void shouldLockUsingJdbcDefaultStorageUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            given()
                    .when().post("/jdbcStorageLockableResource/interceptor/default")
                    .then()
                    .statusCode(204);
        }

        given()
                .when().get("/jdbcStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingJdbcMasterStorageUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            given()
                    .when().post("/jdbcStorageLockableResource/interceptor/master")
                    .then()
                    .statusCode(204);
        }

        given()
                .when().get("/jdbcStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingJdbcDefaultStorageUsingExecutor() {
        for (int execution = 0; execution < 5; execution++) {
            if (execution == 0) {
                given()
                        .when().post("/jdbcStorageLockableResource/execute/default")
                        .then()
                        .statusCode(200)
                        .body("executed", is(true))
                        .body("result", is(1));
            } else {
                given()
                        .when().post("/jdbcStorageLockableResource/execute/default")
                        .then()
                        .statusCode(200)
                        .body("executed", is(false))
                        .body("result", nullValue());
            }
        }

        given()
                .when().get("/jdbcStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingJdbcMasterStorageUsingExecutor() {
        for (int execution = 0; execution < 5; execution++) {
            if (execution == 0) {
                given()
                        .when().post("/jdbcStorageLockableResource/execute/master")
                        .then()
                        .statusCode(200)
                        .body("executed", is(true))
                        .body("result", is(1));
            } else {
                given()
                        .when().post("/jdbcStorageLockableResource/execute/master")
                        .then()
                        .statusCode(200)
                        .body("executed", is(false))
                        .body("result", nullValue());
            }
        }

        given()
                .when().get("/jdbcStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        given()
                .when().post("/jdbcStorageLockableResource/reset")
                .then()
                .log().all()
                .statusCode(204);
    }
}
