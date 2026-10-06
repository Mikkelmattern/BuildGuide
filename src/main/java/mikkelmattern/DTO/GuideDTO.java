package mikkelmattern.DTO;

import lombok.Builder;

import java.time.Instant;

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
