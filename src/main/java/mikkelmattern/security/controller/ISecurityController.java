package mikkelmattern.security.controller;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

/**
 * Security checks that run before a request reaches the route handlers.
 */
public interface ISecurityController {

    /**
     * Checks who is making the request, typically by verifying a token,
     * and stores the user on the context for later handlers.
     *
     * @param context the Javalin context for the current request
     */
    void authenticate(@NotNull Context context);

    /**
     * Checks that the authenticated user is allowed to access the matched route.
     *
     * @param context the Javalin context for the current request
     */
    void authorize(@NotNull Context context);
}
