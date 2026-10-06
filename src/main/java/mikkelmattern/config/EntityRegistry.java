package mikkelmattern.config;

import org.hibernate.cfg.Configuration;
import mikkelmattern.entities.*;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        // TODO: Add more entities here...
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Guide.class);
    }
}
