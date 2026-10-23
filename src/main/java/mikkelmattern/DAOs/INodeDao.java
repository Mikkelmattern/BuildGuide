package mikkelmattern.DAOs;

import mikkelmattern.entities.Node;

import java.util.List;
import java.util.Set;

public interface INodeDao {
    List<Node> findByGuideId(Long guideId); // all nodes of a guide, flat, one query
    Node findById(Long id);
    Node create(Node node);
    Node update(Node node);
    void deleteAll(Set<Long> nodeIds);      // a node + its subtree
}
