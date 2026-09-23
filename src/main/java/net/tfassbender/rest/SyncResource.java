package net.tfassbender.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.tfassbender.sync.SyncService;
import net.tfassbender.sync.SyncStatus;

@Path("/sync")
@Produces(MediaType.APPLICATION_JSON)
public class SyncResource {

    private final SyncService syncService;

    public SyncResource(SyncService syncService) {
        this.syncService = syncService;
    }

    @GET
    public SyncStatus status() {
        return syncService.status();
    }

    /**
     * Forces a pull from the remote. Limited globally to one call per cooldown period (429 otherwise).
     */
    @POST
    public SyncStatus sync() {
        return syncService.manualSync();
    }
}
