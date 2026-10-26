package mikkelmattern.DTOs;

import java.util.Set;

/**
 * The result of revealing or hiding a node.
 *
 * @param guideId the id of the guide the nodes belong to
 * @param nodeIds the ids of the node and every node under it that were revealed or hidden
 * @param count   how many nodes that is, e.g. for a "reveal 14 sections?" confirmation
 */
public record RevealDTO(Long guideId, Set<Long> nodeIds, int count) {

    public RevealDTO(Long guideId, Set<Long> nodeIds) {
        this(guideId, nodeIds, nodeIds.size());
    }
}
