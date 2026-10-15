package mikkelmattern;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.config.ApplicationConfig;
import mikkelmattern.config.HibernateConfig;
import mikkelmattern.DAOs.UserDaoImpl;
import mikkelmattern.controller.UserController;
import mikkelmattern.entities.User;
import mikkelmattern.routes.UserRoutes;
import mikkelmattern.security.Role;
import mikkelmattern.security.SecurityService;
import mikkelmattern.security.TokenService;
import mikkelmattern.security.controller.SecurityController;
import mikkelmattern.services.UserService;
import mikkelmattern.utils.Utils;

/**
 * Entry point of the application.
 */
public class Main {

    /**
     * Sets up the database, security and routes, saves a test user and starts the server.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Database and DAO
        EntityManagerFactory entityManagerFactory = HibernateConfig.getEntityManagerFactory();
        UserDaoImpl userDaoImpl = new UserDaoImpl(entityManagerFactory);

        // One TokenService, shared by both sides so tokens are signed and verified with the same secret
        String secret = Utils.getPropertyValue("SECRET_KEY", "config.properties");
        TokenService tokenService = new TokenService(secret);
        SecurityService securityService = new SecurityService(userDaoImpl, tokenService);        // creates tokens
        SecurityController securityController = new SecurityController(tokenService);           // verifies tokens

        // Services, controllers and routes
        UserService userService = new UserService(userDaoImpl, securityService);
        UserController userController = new UserController(userService, securityService);
        UserRoutes userRoutes = new UserRoutes(userController);

        // Test user (hbm2ddl.auto is "create", so the tables are empty on every start)
        User user = User.builder()
                .username("MikkelMattern")
                .email("Mini0011@stud.ek.dk")
                .password("test123")
                .build();
        // Both roles, since authorize needs an exact match and some routes only allow USER
        user.addRole(Role.ADMIN);
        user.addRole(Role.USER);

        userDaoImpl.save(user);

        // Start the server
        new ApplicationConfig(securityController)
                .route(userRoutes.getRoutes())
                .cors()
                .security()
                .apiExceptions()
                .exceptions()
                .notFound()
                .start(7070);
    }
}
