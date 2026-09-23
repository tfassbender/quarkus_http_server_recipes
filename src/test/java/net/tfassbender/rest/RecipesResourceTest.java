package net.tfassbender.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
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
                .body("name", contains("ohne_titel", "spaghetti", "tomatensuppe"))
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
    void searchByIngredients_splitsResultsByTags() {
        given()
                .queryParam("q", "Tomaten ei #nudeln")
                .when().get("/recipes/search")
                .then()
                .statusCode(200)
                .body("ingredientCount", equalTo(2))
                .body("tags", contains("nudeln"))
                // "Tomaten" only appears in the spaghetti's Zubereitung, which is not searched
                .body("results.name", contains("spaghetti"))
                .body("results[0].matchedIngredients", contains("ei"))
                .body("resultsMissingTags.name", contains("tomatensuppe"))
                .body("resultsMissingTags[0].score", equalTo(2));
    }

    @Test
    void searchByIngredients_emptyQuery_returnsNoResults() {
        given()
                .when().get("/recipes/search")
                .then()
                .statusCode(200)
                .body("results", empty())
                .body("resultsMissingTags", empty());
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
