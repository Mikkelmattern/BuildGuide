package mikkelmattern.config;

import org.hibernate.cfg.Configuration;
import mikkelmattern.entities.*;

/**
 * Lists the entity classes Hibernate should know about.
 */
final class EntityRegistry {

    private EntityRegistry() {}

    /**
     * Registers every entity class on the Hibernate configuration.
     *
     * @param configuration the Hibernate configuration to add the entities to
     */
    static void registerEntities(Configuration configuration) {
        // TODO: Add more entities here...
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Guide.class);
        configuration.addAnnotatedClass(Node.class);
        configuration.addAnnotatedClass(Reveal.class);
    }
}
