package mikkelmattern.mappers;

import mikkelmattern.DTOs.NodeDTO;
import mikkelmattern.entities.Node;

import java.util.List;

/**
 * Converts {@link Node} entities to {@link NodeDTO}s.
 */
public class NodeMapper {

    private NodeMapper() {
        // Only static methods, so no instances
    }

    /**
     * Converts a node to a DTO for its author, with the content always included.
     * Children are not included; the list is empty.
     * Used after creating or updating a node, where the author gets back what they wrote.
     *
     * @param node the node to convert
     * @return the node as a DTO, marked as revealed and with an empty children list
     */
    public static NodeDTO toAuthorDTO(Node node) {
        return new NodeDTO(
                node.getId(),
                node.getTitle(),
                node.getPriorityNumber(),
                node.getStoryOrderNumber(),
                true,
                node.getContent(),
                List.of()
        );
    }
}