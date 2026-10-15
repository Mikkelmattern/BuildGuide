package mikkelmattern.DAOs;

import mikkelmattern.entities.Guide;

/**
 * Database operations for {@link Guide}s.
 */
public interface GuideDao {

    /**
     * Finds a guide by its id.
     *
     * @param id the guide's id
     * @return the guide, or {@code null} if none has that id
     */
    public Guide find(long id);

    /**
     * Saves a new guide.
     *
     * @param guide the guide to save
     * @return the saved guide, with its generated id
     */
    public Guide save(Guide guide);

    /**
     * Updates an existing guide with the guide's current values.
     *
     * @param guide the guide with the new values
     */
    public void update(Guide guide);

    /**
     * Deletes a guide.
     *
     * @param guide the guide to delete
     * @return {@code true} if the guide was deleted, {@code false} if it didn't exist
     */
    public boolean delete(Guide guide);
}
