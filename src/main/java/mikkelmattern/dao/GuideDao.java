package mikkelmattern.dao;

import mikkelmattern.entities.Guide;

public interface GuideDao {
    public Guide find(long id);

    public Guide save(Guide guide);

    public void update(Guide guide);

    public boolean delete(Guide guide);
}
