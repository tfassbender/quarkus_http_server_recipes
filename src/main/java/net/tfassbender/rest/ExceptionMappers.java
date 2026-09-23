package net.tfassbender.rest;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.tfassbender.service.RecipeNotFoundException;
import net.tfassbender.sync.PullCooldownException;
import net.tfassbender.sync.SyncFailedException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

    @ServerExceptionMapper
    public Response mapRecipeNotFound(RecipeNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.TEXT_PLAIN)
                .entity(e.getMessage())
                .build();
    }

    @ServerExceptionMapper
    public Response mapPullCooldown(PullCooldownException e) {
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, e.remainingSeconds())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(e.getMessage(), e.remainingSeconds()))
                .build();
    }

    @ServerExceptionMapper
    public Response mapSyncFailed(SyncFailedException e) {
        return Response.serverError()
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
    }
}
