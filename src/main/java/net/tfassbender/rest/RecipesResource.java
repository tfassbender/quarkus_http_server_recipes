package net.tfassbender.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.tfassbender.service.RecipeSummary;
import net.tfassbender.service.RecipesService;

import java.io.IOException;
import java.util.List;

@Path("/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipesResource {

    @Inject
    private RecipesService service;

    @GET
    public Response listRecipes() {
        try {
            List<RecipeSummary> files = service.listRecipeFiles();
            return Response.ok(files).build();
        } catch (IOException e) {
            return Response.serverError()
                    .entity("Failed to read recipes: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{filename}")
    public Response getRecipe(@PathParam("filename") String filename) {
        try {
            String html = service.getRecipeHtml(filename);
            return Response.ok(html).type(MediaType.TEXT_PLAIN).build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Recipe not found: " + e.getMessage())
                    .build();
        }
    }
}