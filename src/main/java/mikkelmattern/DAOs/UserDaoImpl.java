package mikkelmattern.DAOs;


import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.User;

import java.util.List;


/**
 * JPA implementation of {@link UserDao}, using the transaction helpers from {@link DAO}.
 */
public class UserDaoImpl extends DAO implements UserDao {

    private static UserDaoImpl instance;

    /**
     * Creates a user DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public UserDaoImpl(EntityManagerFactory emf) {
        super(emf);
    }

    /**
     * Returns the shared user DAO, creating it on the first call.
     * The factory passed on later calls is ignored.
     *
     * @param emf the factory used to create entity managers
     * @return the shared user DAO
     */
    public static synchronized UserDaoImpl getInstance(EntityManagerFactory emf){
        if(instance == null){
           return instance = new UserDaoImpl(emf);
        }
        return instance;
    }

    @Override
    public User findByID(long id) {
        return executeQuery(em -> em.find(User.class, id));
    }

    @Override
    public User save(User user) {
        executeUpdate(em -> em.persist(user));
        return user;
    }

    @Override
    public void update(User user) {
        executeUpdate(em -> em.merge(user));
    }

    @Override
    public User delete(User u) {
        executeUpdate(em -> {
            // A detached user must be looked up again before it can be removed
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

    /**
     * Finds all users.
     *
     * @return every user in the database
     */
    public List<User> getAll() {
        return executeQuery(em -> em.createQuery("SELECT u FROM User u", User.class).getResultList());
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to look up
     * @return the user, or {@code null} if none has that username
     */
    public User findUserByName(String username) {
        return executeQuery(em -> em.createQuery(
                        "SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }
}