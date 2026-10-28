package mikkelmattern.records;

public record NodeRequest(String title, String content, Long parentId,
                          Boolean moveToTop, Integer priorityNumber, Integer storyOrderNumber) {
}
