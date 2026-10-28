package mikkelmattern.utils;

import mikkelmattern.entities.Node;

import java.util.*;

/**
 * Helpers for working with a guide's node tree.
 */
public final class TreeUtil {

    private TreeUtil() {
    }

    /**
     * Collects the id of a node and of every node under it, at any depth.
     *
     * @param rootId the id of the node to start from
     * @param nodes  all nodes of the guide
     * @return the root id plus the ids of all its descendants
     */
    public static Set<Long> collectSubtreeIds(Long rootId, List<Node> nodes) {
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Node n : nodes) {
            if (n.getParent() != null) {
                childrenByParent
                        .computeIfAbsent(n.getParent().getId(), k -> new ArrayList<>())
                        .add(n.getId());
            }
        }

        Set<Long> result = new HashSet<>();
        Deque<Long> toVisit = new ArrayDeque<>();
        toVisit.push(rootId);

        while (!toVisit.isEmpty()) {
            Long id = toVisit.pop();
            result.add(id);
            toVisit.addAll(childrenByParent.getOrDefault(id, List.of()));
        }
        return result;
    }
}