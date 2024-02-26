package io.quarkiverse.shedlock.it;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import net.javacrumbs.shedlock.core.LockProvider;

@QuarkusTest
public class ShedlockResourceTest {
    @InjectSpy
    LockProvider lockProvider;

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/shedlock")
                .then()
                .statusCode(204);

        verify(lockProvider, times(1)).lock(any());
    }
}
