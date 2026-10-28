package mikkelmattern.controller;

import io.javalin.http.Context;
import mikkelmattern.security.AuthUser;
import mikkelmattern.services.GameService;
import org.jetbrains.annotations.NotNull;

public class GameController {
    GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    public void getAllGames(@NotNull Context context) {
        context.json(gameService.getAllGames());
    }

    public void searchRawg(@NotNull Context context) {
        AuthUser authuser = context.attribute("user");
        String query = context.queryParam("query");
        context.json(gameService.searchRawg(query, authuser));
    }

    public void importGame(@NotNull Context context) {
        AuthUser authUser = context.attribute("user");
        Long id = context.pathParamAsClass("id", Long.class).get();
        context.json(gameService.importGame(id, authUser));
    }

    public void getGameById(@NotNull Context context) {
        Long id = context.pathParamAsClass("id", Long.class).get();
        context.json(gameService.getGameById(id));
    }

    public void getGuidesForGame(@NotNull Context context) {
        Long id = context.pathParamAsClass("id", Long.class).get();
        context.json((gameService.getGameById(id)));
    }
}
