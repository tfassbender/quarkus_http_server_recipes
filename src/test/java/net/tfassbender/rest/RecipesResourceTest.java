package net.tfassbender.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class RecipesResourceTest {

    @Test
    void listRecipes_returnsOnlyTxtFilesWithTitleAndTags() {
        given()
                .when().get("/recipes")
                .then()
                .statusCode(200)
                .body("name", contains("ohne_titel", "spaghetti"))
                .body("[0].title", nullValue())
                .body("[1].title", equalTo("Spaghetti Aglio e Olio"))
                .body("[1].tags", contains("nudeln", "vegetarisch"));
    }

    @Test
    void getRecipe_rendersMarkdownWithoutTagLine() {
        given()
                .when().get("/recipes/spaghetti")
                .then()
                .statusCode(200)
                .body(containsString("<h1>Spaghetti Aglio e Olio</h1>"))
                .body(containsString("<table>"))
                .body(not(containsString("tags:")));
    }

    @Test
    void getRecipe_escapesRawHtml() {
        given()
                .when().get("/recipes/spaghetti")
                .then()
                .statusCode(200)
                .body(not(containsString("<script>")))
                .body(containsString("&lt;script&gt;"));
    }

    @Test
    void getRecipe_unknownName_returns404() {
        given()
                .when().get("/recipes/does-not-exist")
                .then()
                .statusCode(404);
    }

    @Test
    void getRecipe_pathTraversal_returns404() {
        given()
                .when().get("/recipes/..%2F..%2Fbuild.gradle")
                .then()
                .statusCode(404);
    }
}
