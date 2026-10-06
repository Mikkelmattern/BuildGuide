package mikkelmattern.dao;

import mikkelmattern.entities.User;

public interface UserDao {
    public User find(long id);

    public User save(User user);

    public void update(User user);

    public User delete(User user);
}
