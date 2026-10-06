package mikkelmattern.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.Guide;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuideDaoImpl implements GuideDao {
    private final EntityManagerFactory emf;

    public GuideDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Guide find(long id) {
        return executeQuery(em -> em.find(Guide.class, id));
    }

    @Override
    public Guide save(Guide guide) {
        executeUpdate(em -> em.persist(guide));
        return guide;
    }

    @Override
    public void update(Guide guide) {
        executeUpdate(em -> em.merge(guide));
    }

    @Override
    public boolean delete(Guide guide) {
        return executeUpdate(em -> {
            if (!em.contains(guide)) {
                Guide existing = em.find(Guide.class, guide.getId());
                if (existing == null) {
                    return false;
                }
                em.remove(existing);

            } else {
                em.remove(guide);
            }
            return true;
        });
    }

    public List<Guide> findByAuthorId(long userId) {
        return executeQuery(em -> {
            return em.createQuery("SELECT g FROM Guide g WHERE g.author.id = :userId", Guide.class)
                    .setParameter("userId", userId)
                    .getResultList();
        });
    }
    private void executeUpdate(Consumer<EntityManager> action) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private <R> R executeQuery(Function<EntityManager, R> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

}
