package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.GameController;
import mikkelmattern.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GameRoutes implements IRoutes {

    GameController gameController;

    public GameRoutes(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Returns the routes in this group.
     *
     * @return the endpoint group to register
     */
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("games", () -> {
                get(gameController::getAllGames, Role.ANYONE);
                get("search", gameController::searchRawg, Role.ADMIN);
                post("import/{rawgId}", gameController::importGame, Role.ADMIN);
                path("{id}", () -> {
                    get(gameController::getGameById, Role.ANYONE);
                });
            });
        };
    }
}
