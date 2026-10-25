package mikkelmattern.records;

public record NodeRequest(
        String title,
        String content,
        Long parentId,          // null = top-level node
        Integer priorityNumber,
        Integer storyOrderNumber
) {}
