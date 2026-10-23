package mikkelmattern.controller;


import io.javalin.http.Context;
import mikkelmattern.DTOs.TokenDTO;
import mikkelmattern.DTOs.UserInDTO;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.entities.User;
import mikkelmattern.security.AuthUser;
import mikkelmattern.security.SecurityService;
import mikkelmattern.services.UserService;
import org.jetbrains.annotations.NotNull;

/**
 * Handles HTTP requests for users under {@code /users}.
 * <p>
 * Each method reads what it needs from the request, passes it to the
 * {@link UserService} and writes the result as JSON.
 */
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    /**
     * Creates a user controller that uses the given services.
     *
     * @param userService     the service used to read and write users
     * @param securityService the service used for security checks, e.g. login
     */
    public UserController(UserService userService, SecurityService securityService) {
        this.securityService = securityService;
        this.userService = userService;
    }

    /**
     * Returns all users as JSON. Handles {@code GET /users}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void getAllUsers(@NotNull Context ctx) {
        ctx.json(userService.getAllUsersInDTO());
    }

    /**
     * Creates a new user from a JSON body with {@code username}, {@code email}
     * and {@code password}, and returns the created user as JSON. Handles {@code POST /users}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void createUser(@NotNull Context ctx) {
        UserInDTO userInDTO = ctx.bodyAsClass(UserInDTO.class);
        ctx.status(201).json(userService.createUser(userInDTO));
    }

    /**
     * Logs a user in from a JSON body with {@code username} and {@code password},
     * and returns a {@link TokenDTO} with the JWT. Handles {@code POST /auth/login}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void login(@NotNull Context ctx) {
        // Login only needs the username and password, so email is ignored
        UserInDTO userInDTO = ctx.bodyAsClass(UserInDTO.class);
        String token = securityService.login(userInDTO.username(), userInDTO.password());
        ctx.json(new TokenDTO(token, userInDTO.username()));
    }

    /**
     * Returns the logged-in user as JSON, read fresh from the database by the id in the token.
     * Handles {@code GET /auth/me}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void me(@NotNull Context ctx) {
        AuthUser authUser = ctx.attribute("user");
        ctx.json(userService.getUserDTOByID(authUser.id()));
    }

    /**
     * Gives the user with the {@code id} path parameter the role in the {@code role} path parameter,
     * and returns the updated user as JSON. Handles {@code POST /users/{id}/roles/{role}}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void addRole(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        ctx.json(userService.addRole(id, ctx.pathParam("role")));
    }

    /**
     * Removes the role in the {@code role} path parameter from the user with the {@code id}
     * path parameter, and returns the updated user as JSON. Handles {@code DELETE /users/{id}/roles/{role}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void removeRole(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = ctx.attribute("user");
        ctx.json(userService.removeRole(id, ctx.pathParam("role"), authUser));
    }

    /**
     * Returns one user as JSON, looked up by the {@code id} path parameter.
     * Handles {@code GET /users/{id}}.
     *
     * @param ctx the Javalin context for the current request
     */
    public void getUserById(@NotNull Context ctx) {
        String id = ctx.pathParam("id");

        ctx.json(userService.getUserDTOByID(Long.parseLong(id)));
    }

    /**
     * Updates the user with the {@code id} path parameter from a JSON body with any of
     * {@code username}, {@code email} and {@code password}, and returns the updated user as JSON.
     * Only the user themself or an admin may do this. Handles {@code PUT /users/{id}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void updateUser(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();     // which user
        AuthUser authUser = ctx.attribute("user");                  // who is asking (from the token)
        UserInDTO changes = ctx.bodyAsClass(UserInDTO.class);       // what to change (from JSON)

        ctx.json(userService.updateUser(id, changes, authUser));
    }

    /**
     * Deletes the user with the {@code id} path parameter and returns the deleted user as JSON.
     * Only the user themself or an admin may do this. Handles {@code DELETE /users/{id}}.
     *
     * @param ctx the Javalin context for the current request; the logged-in {@link AuthUser}
     *            is read from the attribute {@code "user"}
     */
    public void deleteUser(@NotNull Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        AuthUser authUser = ctx.attribute("user");
        ctx.json(userService.deleteUser(id, authUser));
    }
}
