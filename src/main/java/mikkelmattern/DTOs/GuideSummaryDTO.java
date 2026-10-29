package mikkelmattern.DTOs;

import lombok.Builder;

import java.time.Instant;
@Builder
public record GuideSummaryDTO(Long id, String title, String authorName, Instant createdAt, Instant updatedAt, Long gameId, boolean published) {}