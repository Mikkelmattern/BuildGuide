package mikkelmattern.DTOs;

import lombok.Builder;

/**
 * A user as sent to and from the API. Leaves out the password and roles.
 *
 * @param id       the user's id
 * @param username the user's username
 * @param email    the user's email
 */
@Builder
public record UserOutDTO(
        Long id,
        String username,
        String email
) {
}
