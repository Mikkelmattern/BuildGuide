package mikkelmattern.security;

import mikkelmattern.enums.Role;

import java.util.Set;

/**
 * Security behaviour a user entity must provide: password checks and role management.
 */
public interface ISecurityUser {

    /**
     * Returns the user's roles as strings, e.g. for putting them in a JWT.
     *
     * @return the names of the user's roles
     */
    Set<String> getRolesAsStrings();

    /**
     * Checks a plain-text password against the user's stored password.
     *
     * @param pw the plain-text password to check
     * @return {@code true} if the password matches, otherwise {@code false}
     */
    boolean verifyPassword(String pw);

    /**
     * Gives the user a role.
     *
     * @param role the role to add
     */
    void addRole(Role role);

    /**
     * Removes a role from the user.
     *
     * @param role the name of the role to remove
     */
    void removeRole(String role);
}
