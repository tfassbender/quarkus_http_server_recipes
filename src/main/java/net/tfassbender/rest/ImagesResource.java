package net.tfassbender.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import net.tfassbender.service.ImageService;

@Path("/images")
public class ImagesResource {

    private final ImageService service;

    public ImagesResource(ImageService service) {
        this.service = service;
    }

    /**
     * Serves an image from the recipes' "images" directory; the path may contain subdirectories.
     */
    @GET
    @Path("/{path: .+}")
    public Response getImage(@PathParam("path") String path) {
        ImageService.Image image = service.findImage(path).orElseThrow(NotFoundException::new);
        return Response.ok(image.file(), image.contentType()).build();
    }
}
