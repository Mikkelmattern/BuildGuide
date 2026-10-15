package mikkelmattern.DTOs;

import lombok.Builder;

import java.time.Instant;

/**
 * A guide as sent to and from the API.
 * <p>
 * Holds the author's id and username instead of the whole {@code User}.
 *
 * @param id             the guide's id
 * @param title          the guide's title
 * @param content        the guide's text
 * @param createdAt      when the guide was created
 * @param updatedAt      when the guide was last changed
 * @param authorId       the id of the user who wrote the guide
 * @param authorUsername the username of the user who wrote the guide
 */
@Builder
public record GuideDTO(
        Long id,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt,
        Long authorId,
        String authorUsername
) {
}
