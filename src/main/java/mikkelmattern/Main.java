package mikkelmattern;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.DAOs.*;
import mikkelmattern.clients.RawgClient;
import mikkelmattern.config.ApplicationConfig;
import mikkelmattern.config.HibernateConfig;
import mikkelmattern.controller.*;
import mikkelmattern.entities.User;
import mikkelmattern.routes.*;
import mikkelmattern.enums.Role;
import mikkelmattern.security.SecurityService;
import mikkelmattern.security.TokenService;
import mikkelmattern.security.controller.SecurityController;
import mikkelmattern.services.*;
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
        IUserDao userDao = new UserDaoImpl(entityManagerFactory);
        IGuideDao guideDao = new GuideDaoImpl(entityManagerFactory);
        INodeDao nodeDao = new NodeDaoImpl(entityManagerFactory);
        IRevealDao revealDao = new RevealDaoImpl(entityManagerFactory);
        IGameDao gameDao = new GameDaoImpl(entityManagerFactory);

        // RawgClient
        String apiKey = Utils.getPropertyValue("RAWG_API_KEY", "config.properties");
        RawgClient rawgClient = new RawgClient(apiKey);

        // One TokenService, shared by both sides so tokens are signed and verified with the same secret
        String secret = Utils.getPropertyValue("SECRET_KEY", "config.properties");
        TokenService tokenService = new TokenService(secret);
        SecurityService securityService = new SecurityService(userDao, tokenService);     // creates tokens
        SecurityController securityController = new SecurityController(tokenService);           // verifies tokens

        // Services, controllers and routes
        NodeService nodeService = new NodeService(nodeDao, guideDao, securityService);
        NodeController nodeController = new NodeController(nodeService);
        NodeRoutes nodeRoutes = new NodeRoutes(nodeController);

        UserService userService = new UserService(userDao, securityService);
        UserController userController = new UserController(userService, securityService);
        UserRoutes userRoutes = new UserRoutes(userController);

        GuideService guideService = new GuideService(guideDao, userDao, securityService, nodeDao, revealDao, gameDao);
        GuideController guideController = new GuideController(guideService);
        GuideRoutes guideRoutes = new GuideRoutes(guideController);

        RevealService revealService = new RevealService(nodeDao, guideDao, revealDao, userDao);
        RevealController revealController = new RevealController(revealService);
        RevealRoutes revealRoutes = new RevealRoutes(revealController);

        GameService gameService = new GameService(rawgClient, gameDao, securityService);
        GameController gameController = new GameController(gameService);
        GameRoutes gameRoutes = new GameRoutes(gameController);

        // Test user (hbm2ddl.auto is "create", so the tables are empty on every start)
        User user = User.builder()
                .username("admin")
                .email("Mini0003@stud.ek.dk")
                .password("test123")
                .build();
        // Both roles, since authorize needs an exact match and some routes only allow USER
        user.addRole(Role.ADMIN);
        user.addRole(Role.USER);

        userDao.save(user);

        // Start the server
        new ApplicationConfig(securityController)
                .route(userRoutes.getRoutes())
                .route(guideRoutes.getRoutes())
                .route(nodeRoutes.getRoutes())
                .route(revealRoutes.getRoutes())
                .route(gameRoutes.getRoutes())
                .cors()
                .security()
                .apiExceptions()
                .exceptions()
                .notFound()
                .start(7070);
    }
}
