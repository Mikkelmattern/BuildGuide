package mikkelmattern.DTOs;

/**
 * The response to a successful login.
 *
 * @param token    the JWT to send in the {@code Authorization: Bearer <token>} header
 * @param username the username of the user who logged in
 */
public record TokenDTO(String token, String username) {
}
