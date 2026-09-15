package app.config;

import app.entities.Unicorn;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(Unicorn.class);
        // TODO: Add more entities here...
    }
}
