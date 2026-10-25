package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.UserController;
import mikkelmattern.enums.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Routes for logging in under {@code /auth} and for users under {@code /users},
 * and the roles each one requires.
 */
public class UserRoutes implements IRoutes {

    private final UserController userController;

    /**
     * Creates the user routes.
     *
     * @param userController the controller that handles the requests
     */
    public UserRoutes(UserController userController) {
        this.userController = userController;
    }

    /**
     * Returns the user routes:
     * <ul>
     *     <li>{@code POST /auth/login} - log in and get a JWT (anyone)</li>
     *     <li>{@code GET /auth/me} - the logged-in user (any logged-in user)</li>
     *     <li>{@code GET /users} - all users (admin)</li>
     *     <li>{@code POST /users} - create a user (anyone)</li>
     *     <li>{@code GET /users/{id}} - one user (admin)</li>
     *     <li>{@code PUT /users/{id}} - update a user (the user themself or an admin)</li>
     *     <li>{@code DELETE /users/{id}} - delete a user (the user themself or an admin)</li>
     *     <li>{@code POST /users/{id}/roles/{role}} - give a user a role (admin)</li>
     *     <li>{@code DELETE /users/{id}/roles/{role}} - remove a role from a user (admin)</li>
     * </ul>
     *
     * @return the endpoint group to register
     */
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("auth", () -> {
                post("login", userController::login, Role.ANYONE);
                get("me", userController::me, Role.USER, Role.ADMIN);
            });
            path("users", () -> {
                get(userController::getAllUsers, Role.ADMIN);
                post(userController::createUser, Role.ANYONE);
                path("{id}", () -> {
                    get(userController::getUserById, Role.ADMIN);
                    put(userController::updateUser, Role.USER, Role.ADMIN);
                    delete(userController::deleteUser, Role.USER, Role.ADMIN);
                    path("roles/{role}", () -> {
                        post(userController::addRole, Role.ADMIN);
                        delete(userController::removeRole, Role.ADMIN);
                    });
                });
            });
        };
    }
}
