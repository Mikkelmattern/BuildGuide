package mikkelmattern.mappers;

import mikkelmattern.DTOs.GuideDTO;
import mikkelmattern.DTOs.GuideSummaryDTO;
import mikkelmattern.DTOs.NodeDTO;
import mikkelmattern.entities.Guide;
import mikkelmattern.entities.Node;
import mikkelmattern.entities.User;
import mikkelmattern.enums.ReadingMode;
import mikkelmattern.records.GuideRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts between {@link Guide} entities and the guide DTOs:
 * {@link GuideDTO} with the full node tree, and {@link GuideSummaryDTO} for lists.
 */
public class GuideMapper {

    /**
     * Converts a guide to a DTO with its nodes as a tree.
     * <p>
     * Nodes are sorted by {@code storyOrderNumber} in {@link ReadingMode#STORY} and by
     * {@code priorityNumber} in {@link ReadingMode#FASTEST}. A node's content is only
     * included if its id is in {@code revealedIds}.
     *
     * @param guide       the guide to convert; its author and nodes must be loaded
     * @param revealedIds the ids of the nodes the reader has revealed
     * @param mode        decides the order of the nodes
     * @return the guide as a DTO
     */
    public static GuideDTO toDTO(Guide guide, List<Node> nodes, Set<Long> revealedIds, ReadingMode mode) {
        return GuideDTO.builder()
                .id(guide.getId())
                .title(guide.getTitle())
                .nodes(toNodeTree(nodes, revealedIds, mode))
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .authorId(guide.getAuthor().getId())
                .authorUsername(guide.getAuthor().getUsername())
                .build();
    }

    /**
     * Converts a guide to a DTO with every node revealed, in story order.
     * Used for the author's own views, e.g. after creating or updating a guide.
     *
     * @param guide the guide to convert; its author and nodes must be loaded
     * @return the guide as a DTO
     */
    public static GuideDTO toDTO(Guide guide) {
        Set<Long> allIds = new HashSet<>();
        for (Node node : guide.getNodes()) {
            allIds.add(node.getId());
        }
        return toDTO(guide, guide.getNodes(),  allIds, ReadingMode.STORY);
    }

    /**
     * Converts a guide to a short summary without its nodes.
     *
     * @param guide the guide to convert; its author must be set
     * @return the guide as a summary DTO
     */
    public static GuideSummaryDTO toSummaryDTO(Guide guide) {
        return new GuideSummaryDTO(
                guide.getId(),
                guide.getTitle(),
                guide.getAuthor().getUsername(),
                guide.getUpdatedAt()
        );
    }

    /**
     * Converts a list of guides to summaries.
     *
     * @param guides the guides to convert
     * @return the summaries, in the same order
     */
    public static List<GuideSummaryDTO> toSummaryDTOList(List<Guide> guides) {
        return guides.stream()
                .map(GuideMapper::toSummaryDTO)
                .toList();
    }

    /**
     * Creates a new guide entity from a request. The id, nodes and timestamps
     * aren't set, since they're added later or set by JPA.
     *
     * @param request the new guide's title
     * @param author  the user who writes the guide
     * @return the guide entity
     */
    public static Guide toEntity(GuideRequest request, User author) {
        return Guide.builder()
                .title(request.title())
                .author(author)
                .build();
    }

    /**
     * Builds the node tree from the guide's flat list of nodes.
     * <p>
     * The tree is built from each node's parent id instead of {@link Node#getChildren()},
     * so the lazy children lists don't have to be loaded.
     *
     * @param nodes       all nodes in the guide
     * @param revealedIds the ids of the nodes the reader has revealed
     * @param mode        decides the order of the nodes
     * @return the top-level nodes, each with its children
     */
    private static List<NodeDTO> toNodeTree(List<Node> nodes, Set<Long> revealedIds, ReadingMode mode) {
        // Group the nodes by parent id; top-level nodes are under null
        Map<Long, List<Node>> childrenByParentId = new HashMap<>();
        for (Node node : nodes) {
            Long parentId = node.getParent() == null ? null : node.getParent().getId();
            childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }
        return toNodeDTOs(null, childrenByParentId, revealedIds, orderFor(mode));
    }

    /**
     * Converts the children of one parent to DTOs, sorted, with their own children added recursively.
     *
     * @param parentId           the parent's id, or {@code null} for the top-level nodes
     * @param childrenByParentId every node, grouped by its parent's id
     * @param revealedIds        the ids of the nodes the reader has revealed
     * @param order              the order to sort siblings in
     * @return the children as DTOs
     */
    private static List<NodeDTO> toNodeDTOs(Long parentId, Map<Long, List<Node>> childrenByParentId,
                                            Set<Long> revealedIds, Comparator<Node> order) {
        return childrenByParentId.getOrDefault(parentId, List.of()).stream()
                .sorted(order)
                .map(node -> {
                    boolean revealed = revealedIds != null && revealedIds.contains(node.getId());
                    return new NodeDTO(
                            node.getId(),
                            node.getTitle(),
                            node.getPriorityNumber(),
                            node.getStoryOrderNumber(),
                            revealed,
                            revealed ? node.getContent() : null,
                            toNodeDTOs(node.getId(), childrenByParentId, revealedIds, order)
                    );
                })
                .toList();
    }

    /**
     * Gets the order nodes are shown in for a reading mode.
     *
     * @param mode the reading mode
     * @return a comparator that sorts nodes for that mode
     */
    private static Comparator<Node> orderFor(ReadingMode mode) {
        return switch (mode) {
            case STORY -> Comparator.comparingInt(Node::getStoryOrderNumber);
            case FASTEST -> Comparator.comparingInt(Node::getPriorityNumber);
        };
    }
}
