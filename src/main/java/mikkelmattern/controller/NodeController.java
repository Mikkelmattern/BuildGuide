package mikkelmattern.controller;

import io.javalin.http.Context;
import mikkelmattern.DTOs.GuideDTO;
import mikkelmattern.DTOs.NodeDTO;
import mikkelmattern.records.NodeRequest;
import mikkelmattern.security.AuthUser;
import mikkelmattern.services.NodeService;
import org.jetbrains.annotations.NotNull;

public class NodeController {

   private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void createNode(@NotNull Context ctx){
        long guideId = ctx.pathParamAsClass("guideId", Long.class).get();
        NodeRequest request = ctx.bodyAsClass(NodeRequest.class);
        AuthUser authUser = ctx.attribute("user");
        ctx.status(201).json(nodeService.createNode(guideId, request, authUser));
    }

    /**
     * Updates the node with the {@code id} path parameter and returns the updated node as JSON.
     * Only fields that are set in the request body are changed. Only the guide's author or an admin
     * may do this. Handles {@code PUT /nodes/{id}}.
     *
     * @param ctx the Javalin context for the current request; the body is read as a {@link NodeRequest},
     *            and the logged-in {@link AuthUser} is read from the attribute {@code "user"}
     */
    public void updateNode(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        NodeRequest changes = ctx.bodyAsClass(NodeRequest.class);
        AuthUser authUser = ctx.attribute("user");
        ctx.json(nodeService.updateNode(id, changes, authUser));
    }

    /**
     * Deletes the node with the {@code id} path parameter, together with all nodes under it,
     * and returns the deleted node as JSON. Only the guide's author or an admin may do this.
     * Handles {@code DELETE /nodes/{id}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void deleteNode(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = ctx.attribute("user");
        ctx.json(nodeService.deleteNode(id, authUser));
    }
}
