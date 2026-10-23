package mikkelmattern.DAOs;

import mikkelmattern.entities.Reveal;

import java.util.List;
import java.util.Set;

public interface IRevealDao {
    Set<Long> findRevealedNodeIds(Long userId, Long guideId); // load a guide for a reader

    void saveAll(List<Reveal> reveals);                       // R1–R3: reveal a node / cascade

    void deleteByUserAndNodes(Long userId, Set<Long> nodeIds); // R5: hide again

    void deleteByUserAndGuide(Long userId, Long guideId);     // R7: reset progress
}
