package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.NodeController;
import mikkelmattern.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

public class NodeRoutes implements IRoutes {
    private final NodeController nodeController;
    public NodeRoutes(NodeController nodeController){
        this.nodeController = nodeController;
    }
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("guides/{guideId}/nodes", () -> {
                post(nodeController::createNode, Role.USER, Role.ADMIN);
            });
            path("nodes/{id}", () -> {
                put(nodeController::updateNode, Role.USER, Role.ADMIN);
                delete(nodeController::deleteNode, Role.USER, Role.ADMIN);
            });
        };
    }
}
