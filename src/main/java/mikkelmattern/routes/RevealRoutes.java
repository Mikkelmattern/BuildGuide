package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.RevealController;
import mikkelmattern.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RevealRoutes implements IRoutes {
    RevealController revealController;

    public RevealRoutes(RevealController revealController) {
        this.revealController = revealController;
    }

    /**
     * Returns the reveal routes:
     * <ul>
     *     <li>{@code POST /nodes/{id}/reveal} - reveal a node and everything under it (anyone)</li>
     *     <li>{@code DELETE /nodes/{id}/reveal} - hide a node and everything under it again (anyone)</li>
     *     <li>{@code DELETE /guides/{id}/reveals} - reset all reveals for a guide (anyone)</li>
     * </ul>
     * Logged-in users' reveals are saved in the database, guests' in their session.
     *
     * @return the endpoint group to register
     */
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("nodes/{id}/reveal", () -> {
                post(revealController::revealNode, Role.ANYONE);
                delete(revealController::hideNode, Role.ANYONE);
            });
            path("guides/{id}/reveals", () -> {
                delete(revealController::hideGuide, Role.ANYONE);
            });

        };


    }
}
