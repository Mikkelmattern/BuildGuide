package mikkelmattern.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

/**
 * Factory that turns Hibernate properties into an {@link EntityManagerFactory}.
 */
final class HibernateEmfBuilder {

    private HibernateEmfBuilder() {}

    /**
     * Builds an entity manager factory from the given properties, with all entities registered.
     *
     * @param props the Hibernate properties, including connection details
     * @return the new entity manager factory
     * @throws ExceptionInInitializerError if Hibernate fails to start, e.g. the database can't be reached
     */
    static EntityManagerFactory build(Properties props) {
        try {
            Configuration configuration = new Configuration();
            configuration.setProperties(props);

            // Register entities to make Hibernate aware
            EntityRegistry.registerEntities(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            // Make JPA compliant version of Hibernates sf
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            return sf.unwrap(EntityManagerFactory.class);

        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
}