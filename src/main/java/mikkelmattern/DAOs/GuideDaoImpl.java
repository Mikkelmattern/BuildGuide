package mikkelmattern.DAOs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.Guide;

import java.util.List;
import java.util.function.Function;

/**
 * JPA implementation of {@link IGuideDao}.
 */
public class GuideDaoImpl extends DAO implements IGuideDao {

    /**
     * Creates a guide DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public GuideDaoImpl(EntityManagerFactory emf) {
        super(emf);

    }

    @Override
    public Guide find(long id) {
        return executeQuery(em -> em.find(Guide.class, id));
    }

    @Override
    public Guide create(Guide guide) {
        executeUpdate(em -> em.persist(guide));
        return guide;
    }

    @Override
    public void update(Guide guide) {
        executeUpdate(em -> em.merge(guide));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if the guide was deleted, {@code false} if it didn't exist
     */
    @Override
    public boolean delete(Guide guide) {
        return executeUpdateWithResult(em -> {
            // A detached guide must be looked up again before it can be removed
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

    /**
     * Finds all guides.
     *
     * @return every guide in the database
     */
    @Override
    public List<Guide> getAll() {
        return executeQuery(em -> em.createQuery("SELECT g FROM Guide g", Guide.class).getResultList());
    }

    /**
     * Finds all guides written by a user.
     *
     * @param userId the author's id
     * @return the author's guides, or an empty list if they have none
     */
    @Override
    public List<Guide> findByAuthorId(long userId) {
        return executeQuery(em -> {
            return em.createQuery("SELECT g FROM Guide g WHERE g.author.id = :userId", Guide.class)
                    .setParameter("userId", userId)
                    .getResultList();
        });
    }

    @Override
    public List<Guide> getGuidesByGame(long gameId) {
        return executeQuery(em ->
                em.createQuery("SELECT g FROM Guide g WHERE g.game.id = :gameId", Guide.class)
                        .setParameter("gameId", gameId).getResultList());
    }

    /**
     * Runs an action that changes the database inside a transaction and returns its result.
     * The transaction is committed if the action succeeds and rolled back if it throws.
     *
     * @param action the work to do with the entity manager
     * @param <R>    the type of the result
     * @return the result of the action
     */
    private <R> R executeUpdateWithResult(Function<EntityManager, R> action) {
        EntityManager em = super.emf.createEntityManager();
        try {
            em.getTransaction().begin();
            R result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            // Undo any partial changes before passing the error on
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

}
