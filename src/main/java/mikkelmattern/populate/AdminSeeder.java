package mikkelmattern.populate;

import mikkelmattern.DAOs.IUserDao;
import mikkelmattern.entities.User;
import mikkelmattern.enums.Role;

public class AdminSeeder {

    private final IUserDao userDao;

    public AdminSeeder(IUserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Creates an admin user from environment variables if no admin exists yet.
     * Does nothing if an admin already exists.
     */
    public void seed() {
        if (userDao.existsWithRole(Role.ADMIN)) {
            return;
        }

        String username = System.getenv("ADMIN_USERNAME");
        String password = System.getenv("ADMIN_PASSWORD");
        if (username == null || password == null) {
            System.out.println("No admin exists and ADMIN_USERNAME/ADMIN_PASSWORD are not set");
            return;
        }

        User admin = new User(username, BCrypt.hashpw(password, BCrypt.gensalt()));
        admin.addRole(Role.ADMIN);
        userDao.create(admin);
    }
}
