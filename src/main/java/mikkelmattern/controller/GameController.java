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
        Long rawgId = context.pathParamAsClass("rawgId", Long.class).get();
        context.json(gameService.importGame(rawgId, authUser)).status(201);
    }

    public void getGameById(@NotNull Context context) {
        Long id = context.pathParamAsClass("id", Long.class).get();
        context.json(gameService.getGameById(id));
    }
}
