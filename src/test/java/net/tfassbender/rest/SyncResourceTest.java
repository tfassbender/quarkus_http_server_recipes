package net.tfassbender.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class SyncResourceTest {

    @Test
    void status_reportsInitialSync() {
        given()
                .when().get("/sync")
                .then()
                .statusCode(200)
                .body("gitEnabled", equalTo(false))
                .body("lastSuccess", notNullValue())
                .body("lastError", nullValue());
    }

    @Test
    void manualSync_isLimitedGlobally() {
        given()
                .when().post("/sync")
                .then()
                .statusCode(200)
                .body("lastSuccess", notNullValue());

        given()
                .when().post("/sync")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .body("retryAfterSeconds", greaterThan(0))
                .body("retryAfterSeconds", lessThanOrEqualTo(60));
    }
}
