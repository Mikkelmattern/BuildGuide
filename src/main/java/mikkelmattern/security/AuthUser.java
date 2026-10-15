package mikkelmattern.security;

import java.util.Set;

/**
 * The logged-in user, as read from a verified JWT.
 * <p>
 * Stored on the request context by the security controller so authorization
 * and the controllers can see who made the request.
 *
 * @param id       the user's id from the token's {@code "id"} claim
 * @param username the username from the token's subject
 * @param email    the user's email from the token's {@code "email"} claim
 * @param roles    the user's role names, e.g. {@code "ADMIN"} or {@code "USER"}
 */
public record AuthUser(Long id, String username, String email, Set<String> roles) {
}
