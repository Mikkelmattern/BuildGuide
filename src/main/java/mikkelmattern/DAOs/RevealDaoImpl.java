package mikkelmattern.DAOs;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.Reveal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA implementation of {@link IRevealDao}.
 * <p>
 * A {@link Reveal} records that a user has revealed one node in a guide. Each user can
 * reveal a node only once, since {@code (user_id, node_id)} is unique.
 */
public class RevealDaoImpl extends DAO implements IRevealDao {
    /**
     * Creates a DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public RevealDaoImpl(EntityManagerFactory emf) {
        super(emf);
    }

    /**
     * Finds the ids of the nodes a user has revealed in a guide.
     *
     * @param userId  the reader's id
     * @param guideId the guide's id
     * @return the ids of the revealed nodes, or an empty set if the user hasn't revealed any
     */
    @Override
    public Set<Long> findRevealedNodeIds(Long userId, Long guideId) {
        // Only the ids are selected, so no Reveal or Node entities have to be loaded.
        // A Set makes it fast to check whether a node is revealed when mapping the guide
        return executeQuery(em -> new HashSet<>(em.createQuery("SELECT r.node.id FROM Reveal r "
                        + "WHERE r.user.id = :userId AND r.node.guide.id = :guideId", Long.class)
                .setParameter("userId", userId)
                .setParameter("guideId", guideId)
                .getResultList()
        ));
    }

    /**
     * Saves several reveals in one transaction, e.g. a node together with the nodes it reveals.
     * <p>
     * Either all reveals are saved or none are. If one of them already exists, the unique
     * constraint fails and the whole transaction is rolled back, so pass only new reveals.
     *
     * @param reveals the reveals to save
     */
    @Override
    public void saveAll(List<Reveal> reveals) {
        executeUpdate(em -> reveals.forEach(em::persist));
    }

    /**
     * Hides nodes again by deleting the user's reveals of them.
     * Ids of nodes the user hasn't revealed are ignored.
     *
     * @param userId  the reader's id
     * @param nodeIds the ids of the nodes to hide
     */
    @Override
    public void deleteByUserAndNodes(Long userId, Set<Long> nodeIds) {
        // "IN ()" with an empty list is invalid SQL, and there is nothing to delete anyway
        if (nodeIds.isEmpty()) return;

        // A bulk delete runs straight in the database, without loading the reveals first

        executeUpdate(em -> em.createQuery(
                                "DELETE FROM Reveal r " + "WHERE r.user.id = :userId AND r.node.id IN :nodeIds"
                        ).setParameter("userId", userId)
                        .setParameter("nodeIds", nodeIds)
                        .executeUpdate()

        );
    }

    /**
     * Resets a user's progress in a guide by deleting all their reveals in it.
     *
     * @param userId  the reader's id
     * @param guideId the guide's id
     */
    @Override
    public void deleteByUserAndGuide(Long userId, Long guideId) {
        // JPA doesn't allow joins in a bulk DELETE, and r.node.guide.id would need one.
        // The subquery finds the guide's node ids instead
        executeUpdate(em -> em.createQuery(
                                "DELETE FROM Reveal r " + "WHERE r.user.id = :userId "
                                        + "AND r.node.id IN (SELECT n.id FROM Node n WHERE n.guide.id = :guideId)"
                        ).setParameter("userId", userId)
                        .setParameter("guideId", guideId)
                        .executeUpdate()
        );
    }
}
