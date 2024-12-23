package io.quarkiverse.it.shedlock.jdbc;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JdbcStorageLockableResourceTest {
    @Test
    void shouldLockUsingJdbcDefaultStorage() {
        for (int called = 0; called < 5; called++) {
            given()
                    .when().post("/jdbcStorageLockableResource/default")
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
    void shouldLockUsingJdbcMasterStorage() {
        for (int called = 0; called < 5; called++) {
            given()
                    .when().post("/jdbcStorageLockableResource/master")
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
