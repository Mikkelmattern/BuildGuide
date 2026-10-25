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

}
