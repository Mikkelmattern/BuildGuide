package mikkelmattern.security;

import io.javalin.validation.ValidationException;
import mikkelmattern.entities.User;

/**
 * Data access for security: logging in, registering users and managing their roles.
 */
public interface ISecurityDAO {

    /**
     * Finds a user and checks their password. Used for login.
     *
     * @param username the username to look up
     * @param password the plain-text password to check
     * @return the user, if the username exists and the password matches
     * @throws ValidationException if the user doesn't exist or the password is wrong
     */
    User getVerifiedUser(String username, String password) throws ValidationException;

    /**
     * Creates and saves a new user. Used for register.
     *
     * @param username the new user's username
     * @param password the new user's plain-text password
     * @return the created user
     */
    User createUser(String username, String password);

    /**
     * Creates a role.
     *
     * @param role the name of the role
     * @return the created role
     */
    Role createRole(String role);

    /**
     * Gives an existing user a role.
     *
     * @param username the user to update
     * @param role     the name of the role to add
     * @return the updated user
     */
    User addUserRole(String username, String role);
}
