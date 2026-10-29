package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.GuideController;
import mikkelmattern.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Routes for guides under {@code /guides}, and the roles each one requires.
 */
public class GuideRoutes implements IRoutes {

    private final GuideController guideController;

    /**
     * Creates the guide routes.
     *
     * @param guideController the controller that handles the requests
     */
    public GuideRoutes(GuideController guideController) {
        this.guideController = guideController;
    }

    /**
     * Returns the guide routes:
     * <ul>
     *     <li>{@code GET /guides} - all guides (anyone)</li>
     *     <li>{@code POST /guides} - create a guide (author or admin)</li>
     *     <li>{@code GET /guides/{id}} - one guide (anyone)</li>
     *     <li>{@code PUT /guides/{id}} - update a guide (its author or an admin)</li>
     *     <li>{@code DELETE /guides/{id}} - delete a guide (its author or an admin)</li>
     * </ul>
     *
     * @return the endpoint group to register
     */
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("guides", () -> {
                get(guideController::getAllGuides, Role.ANYONE);
                post(guideController::createGuide, Role.USER, Role.ADMIN);
                path("mine", () -> {
                    get(guideController::getMyAuthoredGuides, Role.USER, Role.ADMIN);
                });
                path("{id}", () -> {
                    get(guideController::getGuideById, Role.ANYONE);
                    put(guideController::updateGuide, Role.USER, Role.ADMIN);
                    delete(guideController::deleteGuide, Role.USER, Role.ADMIN);
                    path("publish", () -> {
                        post(guideController::publish, Role.USER, Role.ADMIN);
                        delete(guideController::unpublish, Role.USER, Role.ADMIN);
                    });
                });
            });
            path("games/{gameId}/guides", () -> {
                    get(guideController::getGuidesByGame, Role.ANYONE);
            });
        };
    }
}
