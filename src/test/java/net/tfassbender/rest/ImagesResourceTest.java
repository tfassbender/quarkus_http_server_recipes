package net.tfassbender.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ImagesResourceTest {

    @Test
    void getImage_returnsImageWithContentType() {
        given()
                .when().get("/images/spaghetti.png")
                .then()
                .statusCode(200)
                .contentType("image/png");
    }

    @Test
    void getImage_unknownImage_returns404() {
        given()
                .when().get("/images/does-not-exist.png")
                .then()
                .statusCode(404);
    }

    @Test
    void getImage_unsupportedType_returns404() {
        given()
                .when().get("/images/..%2Fspaghetti.txt")
                .then()
                .statusCode(404);
    }

    @Test
    void getImage_pathTraversal_returns404() {
        given()
                .when().get("/images/..%2Foutside-images-dir.png")
                .then()
                .statusCode(404);
    }
}
