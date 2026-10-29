package mikkelmattern.utils;

import mikkelmattern.entities.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TreeUtil#collectSubtreeIds(Long, List)}, which the cascade in
 * reveal, hide, move and delete all depend on.
 */
class TreeUtilTest {

    // The same tree as the setup guide in Auth.http:
    //   1 Act 1
    //   ├── 2 Limgrave
    //   │   └── 3 Church of Elleh
    //   └── 4 Stormveil Castle
    //   5 Act 2
    private Node act1;
    private Node limgrave;
    private Node church;
    private Node stormveil;
    private Node act2;
    private List<Node> nodes;

    @BeforeEach
    void setUp() {
        act1 = node(1L, null);
        limgrave = node(2L, act1);
        church = node(3L, limgrave);
        stormveil = node(4L, act1);
        act2 = node(5L, null);
        nodes = new ArrayList<>(List.of(act1, limgrave, church, stormveil, act2));
    }

    /**
     * Creates a node with an id and a parent, without a database.
     */
    private static Node node(Long id, Node parent) {
        Node node = new Node();
        node.setId(id);
        node.setParent(parent);
        return node;
    }

    @Test
    @DisplayName("A top-level node includes its whole subtree, at every depth")
    void topLevelNodeIncludesWholeSubtree() {
        Set<Long> ids = TreeUtil.collectSubtreeIds(1L, nodes);

        assertEquals(Set.of(1L, 2L, 3L, 4L), ids);
    }

    @Test
    @DisplayName("A node in the middle includes only its own children, not its parent or siblings")
    void middleNodeLeavesOutParentAndSiblings() {
        Set<Long> ids = TreeUtil.collectSubtreeIds(2L, nodes);

        assertEquals(Set.of(2L, 3L), ids);
        assertFalse(ids.contains(1L), "The parent should not be included");
        assertFalse(ids.contains(4L), "A sibling should not be included");
    }

    @Test
    @DisplayName("A leaf node gives only its own id")
    void leafNodeGivesOnlyItself() {
        assertEquals(Set.of(3L), TreeUtil.collectSubtreeIds(3L, nodes));
    }

    @Test
    @DisplayName("A top-level node without children gives only its own id")
    void topLevelNodeWithoutChildren() {
        assertEquals(Set.of(5L), TreeUtil.collectSubtreeIds(5L, nodes));
    }

    @Test
    @DisplayName("A deep chain is followed all the way down")
    void deepChainIsFollowedToTheBottom() {
        List<Node> chain = new ArrayList<>();
        Node parent = null;
        for (long id = 1; id <= 100; id++) {
            Node child = node(id, parent);
            chain.add(child);
            parent = child;
        }

        Set<Long> ids = TreeUtil.collectSubtreeIds(1L, chain);

        assertEquals(100, ids.size());
        assertTrue(ids.contains(100L), "The deepest node should be included");
    }

    @Test
    @DisplayName("A node with many children includes all of them")
    void manyChildrenAreAllIncluded() {
        List<Node> wide = new ArrayList<>();
        Node root = node(1L, null);
        wide.add(root);
        for (long id = 2; id <= 51; id++) {
            wide.add(node(id, root));
        }

        assertEquals(51, TreeUtil.collectSubtreeIds(1L, wide).size());
    }

    @Test
    @DisplayName("The order of the node list doesn't matter")
    void orderOfNodesDoesNotMatter() {
        Collections.reverse(nodes);

        assertEquals(Set.of(1L, 2L, 3L, 4L), TreeUtil.collectSubtreeIds(1L, nodes));
    }

    @Test
    @DisplayName("An empty node list gives only the root id")
    void emptyListGivesOnlyRoot() {
        assertEquals(Set.of(1L), TreeUtil.collectSubtreeIds(1L, List.of()));
    }

    @Test
    @DisplayName("A loop in the tree doesn't make it run forever")
    void loopDoesNotRunForever() {
        // Broken data: 1 and 2 are each other's parent
        Node a = node(1L, null);
        Node b = node(2L, a);
        a.setParent(b);

        Set<Long> ids = assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> TreeUtil.collectSubtreeIds(1L, List.of(a, b)));

        assertEquals(Set.of(1L, 2L), ids);
    }
}
