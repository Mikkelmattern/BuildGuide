package mikkelmattern.controller;

import io.javalin.http.Context;
import mikkelmattern.DTOs.GuideDTO;
import mikkelmattern.enums.ReadingMode;
import mikkelmattern.security.AuthUser;
import mikkelmattern.services.GuideService;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Handles HTTP requests for guides under {@code /guides}.
 * <p>
 * Each method reads what it needs from the request, passes it to the
 * {@link GuideService} and writes the result as JSON.
 */
public class GuideController {

    private final GuideService guideService;

    /**
     * Creates a guide controller that uses the given service.
     *
     * @param guideService the service used to read and write guides
     */
    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    /**
     * Returns all guides as JSON. Handles {@code GET /guides}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void getAllGuides(@NotNull Context ctx) {
        ctx.json(guideService.getAllGuides());
    }

    /**
     * Returns one guide as JSON, looked up by the {@code id} path parameter.
     * Handles {@code GET /guides/{id}}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void getGuideById(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
       AuthUser authUser = ctx.attribute("user");
       Set<Long> guestReveal = ctx.sessionAttribute("reveals");
       ReadingMode readingMode = ctx.queryParamAsClass("mode", ReadingMode.class)
                       .getOrDefault(ReadingMode.STORY);

        ctx.json(guideService.getGuideById(id, authUser, guestReveal, readingMode));
    }

    /**
     * Creates a guide from a JSON body with {@code title} and {@code content}, written by the
     * logged-in user, and returns it as JSON. Handles {@code POST /guides}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void createGuide(@NotNull Context ctx) {
        GuideDTO guideDTO = ctx.bodyAsClass(GuideDTO.class);
        AuthUser authUser = ctx.attribute("user");
        ctx.status(201).json(guideService.createGuide(guideDTO, authUser));
    }

    /**
     * Updates the guide with the {@code id} path parameter from a JSON body with
     * {@code title} and/or {@code content}, and returns it as JSON.
     * Only the author or an admin may do this. Handles {@code PUT /guides/{id}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void updateGuide(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        GuideDTO changes = ctx.bodyAsClass(GuideDTO.class);
        AuthUser authUser = ctx.attribute("user");
        ctx.json(guideService.updateGuide(id, changes, authUser));
    }

    /**
     * Deletes the guide with the {@code id} path parameter and returns it as JSON.
     * Only the author or an admin may do this. Handles {@code DELETE /guides/{id}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void deleteGuide(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = ctx.attribute("user");
        ctx.json(guideService.deleteGuide(id, authUser));
    }

    public void getGuidesByGame(@NotNull Context context) {
            Long id = context.pathParamAsClass("gameId", Long.class).get();
            context.json((guideService.getGuidesByGame(id)));

    }
}
