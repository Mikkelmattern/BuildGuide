package mikkelmattern.DTOs;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * A guide as sent to and from the API.
 * <p>
 * Holds the author's id and username instead of the whole {@code User}.
 *
 * @param id             the guide's id
 * @param title          the guide's title
 * @param nodes          the guide's top-level nodes, each with its children
 * @param createdAt      when the guide was created
 * @param updatedAt      when the guide was last changed
 * @param authorId       the id of the user who wrote the guide
 * @param authorUsername the username of the user who wrote the guide
 */
@Builder
public record GuideDTO(
        Long id,
        String title,
        List<NodeDTO> nodes,
        Instant createdAt,
        Instant updatedAt,
        Long authorId,
        String authorUsername
) {
}
