package mikkelmattern.DAOs;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.Node;

import java.util.List;
import java.util.Set;

public class NodeDaoImpl extends DAO implements INodeDao {
    /**
     * Creates a DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public NodeDaoImpl(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    public List<Node> findByGuideId(Long guideId) {
        return executeQuery(em ->
                em.createQuery("SELECT n FROM Node n WHERE n.guide.id = :id", Node.class)
                        .setParameter("id", guideId)
                        .getResultList());
    }

    @Override
    public Node findById(Long id) {
        return executeQuery(em -> em.find(Node.class, id));
    }

    @Override
    public Node create(Node node) {
        executeUpdate(em -> em.persist(node));
        return node;
    }

    @Override
    public Node update(Node node) {
        executeUpdate(em -> em.merge(node));
        return node;
    }

    @Override
    public void deleteAll(Set<Long> nodeIds) {
        if (nodeIds.isEmpty()) {
            return;
        }
        executeUpdate(em -> em.createQuery("DELETE FROM Node n WHERE n.id IN  :nodeIds")
                .setParameter("nodeIds", nodeIds)
                .executeUpdate());
    }
}
