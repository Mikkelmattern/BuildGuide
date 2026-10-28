package mikkelmattern.security.controller;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.security.RouteRole;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.security.AuthUser;
import mikkelmattern.enums.Role;
import mikkelmattern.security.TokenService;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles security for incoming HTTP requests.
 * <p>
 * Checks that a request carries a valid JWT (authentication) and that the user has a role
 * the route allows (authorization) before it reaches the route handlers.
 * Routes marked with {@link Role#ANYONE} are open and skip both checks.
 */
public class SecurityController implements ISecurityController {

    private static SecurityController instance;
    private TokenService tokenService;

    /**
     * Creates a security controller that uses the given token service to verify JWTs.
     *
     * @param tokenService the service used to verify tokens from the Authorization header
     */
    public SecurityController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Authenticates the request by verifying the bearer token in the Authorization header.
     * <p>
     * Open routes ({@link Role#ANYONE}) and CORS preflight requests ({@code OPTIONS}) are let
     * through without a token. For all other requests, the token is verified and the resulting
     * {@link AuthUser} is stored on the context under the attribute {@code "user"}, so later
     * handlers can read who is logged in.
     *
     * @param ctx the Javalin context for the current request
     * @throws ApiException with status 401 if the header is missing or malformed,
     *                      or if the token is invalid or expired
     */
    @Override
    public void authenticate(@NotNull Context ctx) {
        // 1. Open routes and CORS preflight requests need no token
        if (ctx.method() == HandlerType.OPTIONS) {
            return;
        }

        // Open routes work without a token, but if one is sent, the user is still read,
        // so e.g. GET /guides/{id} can show a logged-in reader's saved reveals
        if (isOpenRoute(ctx)) {
            String header = ctx.header("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    ctx.attribute("user", tokenService.verify(header.substring("Bearer ".length())));
                } catch (ApiException e) {
                    // An invalid or expired token on an open route just means the request is a guest's
                }
            }
            return;
        }

        // 2. Read the header: "Authorization: Bearer <token>"
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(401, "Missing or invalid Authorization header");
        }
        String token = header.substring("Bearer ".length());

        // 3. Verify signature and expiry, and read the claims
        AuthUser user = tokenService.verify(token);   // throws ApiException(401) if invalid

        // 4. Store who the user is, for authorize and the controllers
        ctx.attribute("user", user);
    }

    /**
     * Authorizes the request by checking that the logged-in user has one of the roles
     * the matched route allows.
     * <p>
     * Must run after {@link #authenticate(Context)}, since it reads the {@link AuthUser}
     * stored on the context under {@code "user"}. Open routes ({@link Role#ANYONE}) and CORS
     * preflight requests ({@code OPTIONS}) are let through without a check.
     *
     * @param ctx the Javalin context for the current request
     * @throws ApiException with status 401 if no authenticated user is on the context,
     *                      or 403 if the user has none of the route's roles
     */
    @Override
    public void authorize(@NotNull Context ctx) {
        // 1. Open routes and CORS preflight requests need no role check,
        //    since authenticate didn't store a user for them
        if (isOpenRoute(ctx) || ctx.method() == HandlerType.OPTIONS) return;

        // 2. Get the user that authenticate stored on the context
        AuthUser user = ctx.attribute("user");
        if (user == null) throw new ApiException(401, "Not authenticated");

        // 3. Collect the roles the route allows, as strings (e.g. "ADMIN", "USER")
        Set<String> allowed = ctx
                .routeRoles().stream()
                .map(RouteRole::toString).collect(Collectors.toSet());

        // 4. The user needs at least one of the allowed roles
        boolean permitted = user.roles().stream().anyMatch(allowed::contains);
        if (!permitted) throw new ApiException(403, "You dont have access to this resource");
    }

    /**
     * Checks whether the matched route is open to everyone.
     *
     * @param ctx the Javalin context for the current request
     * @return {@code true} if the route's roles include {@link Role#ANYONE}, otherwise {@code false}
     */
    private boolean isOpenRoute(Context ctx) {
        return ctx.routeRoles().contains(Role.ANYONE);
    }
}
