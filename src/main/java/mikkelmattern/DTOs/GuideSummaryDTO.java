package mikkelmattern.DTOs;

import java.time.Instant;

public record GuideSummaryDTO(Long id, String title, String authorName, Instant updatedAt) {}