package mikkelmattern.controller;

import io.javalin.http.Context;
import mikkelmattern.security.AuthUser;
import mikkelmattern.services.RevealService;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class RevealController {

    RevealService revealService;

    public RevealController(RevealService revealService) {
        this.revealService = revealService;
    }

    public void revealNode(@NotNull Context context) {
        long nodeId = context.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = context.attribute("user");
        Set<Long> guestReveals = guestReveals(context);
        revealService.revealNode(nodeId, authUser, guestReveals);
        context.sessionAttribute("reveals", guestReveals);
        context.status(204);
    }

    public void hideNode(@NotNull Context context) {
        long nodeId = context.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = context.attribute("user");
        Set<Long> guestReveals = guestReveals(context);
        revealService.hideNode(nodeId, authUser, guestReveals);
        context.sessionAttribute("reveals", guestReveals);
        context.status(204);
    }

    public void hideGuide(@NotNull Context context) {
        long nodeId = context.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = context.attribute("user");
        Set<Long> guestReveals = guestReveals(context);
        revealService.hideGuide(nodeId, authUser, guestReveals);
        context.sessionAttribute("reveals", guestReveals);
        context.status(204);
    }

    private Set<Long> guestReveals(@NotNull Context context) {
        Set<Long> reveals = context.sessionAttribute("reveals");
        return reveals != null ? reveals : new HashSet<>();
    }
}
