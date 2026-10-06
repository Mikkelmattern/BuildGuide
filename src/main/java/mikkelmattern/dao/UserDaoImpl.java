package mikkelmattern.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.User;

import java.util.function.Consumer;
import java.util.function.Function;

public class UserDaoImpl implements UserDao {

    private final EntityManagerFactory emf;

    public UserDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public User find(long id) {
        return executeQuery(em -> em.find(User.class, id));
    }

    @Override
    public User save(User user) {
        executeUpdate( em -> em.persist(user));
        return user;
    }

    @Override
    public void update(User user) {
         executeUpdate(em -> em.merge(user));
    }

    @Override
    public User delete(User u) {
        executeUpdate(em -> {
            if (!em.contains(u)) {
                User existing = em.find(User.class, u.getId());
                if (existing == null) {
                    return;
                }
                em.remove(existing);
            } else {
                em.remove(u);
            }
        });
        return u;
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