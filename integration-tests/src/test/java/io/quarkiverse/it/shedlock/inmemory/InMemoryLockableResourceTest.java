package io.quarkiverse.it.shedlock.inmemory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InMemoryLockableResourceTest {
    @Test
    void shouldLockUsingInterceptor() {
        for (int execution = 0; execution < 5; execution++) {
            given()
                    .when().post("/inMemoryStorageLockableResource/interceptor")
                    .then()
                    .statusCode(204);
        }

        given()
                .when().get("/inMemoryStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @Test
    void shouldLockUsingExecutor() {
        for (int execution = 0; execution < 5; execution++) {
            if (execution == 0) {
                given()
                        .when().post("/inMemoryStorageLockableResource/execute")
                        .then()
                        .log().all()
                        .statusCode(200)
                        .body("executed", is(true))
                        .body("result", is(1));
            } else {
                given()
                        .when().post("/inMemoryStorageLockableResource/execute")
                        .then()
                        .log().all()
                        .statusCode(200)
                        .body("executed", is(false))
                        .body("result", nullValue());
            }
        }

        given()
                .when().get("/inMemoryStorageLockableResource/callCount")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("1"));
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        given()
                .when().post("/inMemoryStorageLockableResource/reset")
                .then()
                .log().all()
                .statusCode(204);
    }
}
