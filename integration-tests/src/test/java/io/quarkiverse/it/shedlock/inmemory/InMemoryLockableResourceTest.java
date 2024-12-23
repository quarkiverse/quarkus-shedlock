package io.quarkiverse.it.shedlock.inmemory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InMemoryLockableResourceTest {
    @Test
    void shouldLockUsingInMemoryStorage() {
        for (int called = 0; called < 5; called++) {
            given()
                    .when().post("/inMemoryStorageLockableResource")
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
