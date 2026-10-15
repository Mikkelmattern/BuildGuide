package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;
import mikkelmattern.controller.UserController;
import mikkelmattern.security.Role;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * Routes for users under {@code /users}, and the role each one requires.
 */
public class UserRoutes implements IRoutes {

    private final UserController userController;

    /**
     * Creates the user routes.
     *
     * @param guideController the controller that handles the requests
     */
    public UserRoutes(UserController guideController) {
        this.userController = guideController;
    }

    /**
     * Returns the user routes:
     * <ul>
     *     <li>{@code GET /users} - all users (admin)</li>
     *     <li>{@code POST /users} - create a user (anyone)</li>
     *     <li>{@code GET /users/{id}} - one user (admin)</li>
     *     <li>{@code PUT /users/{id}} - update a user (user)</li>
     *     <li>{@code DELETE /users/{id}} - delete a user (admin)</li>
     *     <li>{@code POST /auth/login} - log in and get a JWT (anyone)</li>
     * </ul>
     *
     * @return the endpoint group to register
     */
    @Override
    public EndpointGroup getRoutes() {
        return () -> {
            path("auth", () -> {
                post("login", userController::login, Role.ANYONE);
            });
            path("users", () -> {
                get(userController::getAllUsers, Role.ADMIN);
                post(userController::createUser, Role.ANYONE);
                path("{id}", () -> {
                    get(userController::getUserById, Role.ADMIN);
                    put(userController::updateUser, Role.USER);
                    delete(userController::deleteUser, Role.ADMIN, Role.USER);
                });
            });
        };
    }
}