package mikkelmattern.DTOs;

import java.util.List;

public record NodeDTO(
        Long id,
        String title,
        int priorityNumber,
        int storyOrderNumber,
        boolean revealed,
        String content,          // null when not revealed
        List<NodeDTO> children
) {}
