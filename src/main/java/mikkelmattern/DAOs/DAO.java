package mikkelmattern.DAOs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for DAOs, with helpers that open and close the entity manager
 * and handle transactions.
 */
public class DAO {
    final EntityManagerFactory emf;

    /**
     * Creates a DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public DAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Runs an action that changes the database inside a transaction.
     * The transaction is committed if the action succeeds and rolled back if it throws.
     *
     * @param action the work to do with the entity manager, e.g. {@code em -> em.persist(user)}
     */
    void executeUpdate(Consumer<EntityManager> action) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
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

    /**
     * Runs a read-only action and returns its result. No transaction is used.
     *
     * @param action the query to run, e.g. {@code em -> em.find(User.class, id)}
     * @param <R>    the type of the result
     * @return the result of the action
     */
    <R> R executeQuery(Function<EntityManager, R> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }
}
