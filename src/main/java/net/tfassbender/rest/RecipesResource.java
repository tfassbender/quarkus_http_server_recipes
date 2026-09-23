package net.tfassbender.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.tfassbender.service.RecipeSummary;
import net.tfassbender.service.RecipesService;

import java.util.List;

@Path("/recipes")
public class RecipesResource {

    private final RecipesService service;

    public RecipesResource(RecipesService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeSummary> listRecipes() {
        return service.listRecipes();
    }

    /**
     * Returns the recipe rendered as HTML. Sent as text/plain because the frontend inserts it as a string.
     */
    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRecipe(@PathParam("name") String name) {
        return service.getRecipeHtml(name);
    }
}
