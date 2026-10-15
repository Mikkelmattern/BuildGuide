package mikkelmattern.DAOs;

import mikkelmattern.entities.User;

/**
 * Database operations for {@link User}s.
 */
public interface UserDao {

    /**
     * Finds a user by their id.
     *
     * @param id the user's id
     * @return the user, or {@code null} if none has that id
     */
    public User findByID(long id);

    /**
     * Saves a new user.
     *
     * @param user the user to save
     * @return the saved user, with their generated id
     */
    public User save(User user);

    /**
     * Updates an existing user with the user's current values.
     *
     * @param user the user with the new values
     */
    public void update(User user);

    /**
     * Deletes a user.
     *
     * @param user the user to delete
     * @return the user that was passed in
     */
    public User delete(User user);

}
