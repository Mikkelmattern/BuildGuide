package mikkelmattern.config;

import mikkelmattern.utils.Utils;
import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

/**
 * Provides the application's single {@link EntityManagerFactory}.
 * <p>
 * Connects to the database from environment variables when {@code DEPLOYED} is set,
 * and otherwise from {@code config.properties} on a local PostgreSQL database.
 */
public final class HibernateConfig {

    private static volatile EntityManagerFactory emf;

    private HibernateConfig() {}

    /**
     * Returns the entity manager factory, creating it on the first call.
     *
     * @return the shared entity manager factory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        // Double-checked locking, so only one factory is created even with several threads
        if (emf == null) {
            synchronized (HibernateConfig.class) {
                if (emf == null) {
                    emf = HibernateEmfBuilder.build(buildProps());
                }
            }
        }
        return emf;
    }

    /**
     * Builds the Hibernate properties for the current environment.
     *
     * @return the base properties plus the connection details
     */
    private static Properties buildProps() {
        Properties props = HibernateBaseProperties.createBase();

        // Teaching-friendly default - change to update in production
        props.put("hibernate.hbm2ddl.auto", "create");

        if (System.getenv("DEPLOYED") != null) {
            setDeployedProperties(props);
        } else {
            setDevProperties(props);
        }
        return props;
    }

    /**
     * Sets the connection details from the environment variables {@code CONNECTION_STR},
     * {@code DB_NAME}, {@code DB_USERNAME} and {@code DB_PASSWORD}.
     *
     * @param props the properties to add the connection details to
     */
    private static void setDeployedProperties(Properties props) {
        String dbName = System.getenv("DB_NAME");
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + dbName);
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
    }

    /**
     * Sets the connection details for a local database on {@code localhost:5432},
     * reading {@code DB_NAME}, {@code DB_USERNAME} and {@code DB_PASSWORD} from {@code config.properties}.
     *
     * @param props the properties to add the connection details to
     */
    private static void setDevProperties(Properties props) {
        String dbName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String username = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String password = Utils.getPropertyValue("DB_PASSWORD", "config.properties");

        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/" + dbName);
        props.put("hibernate.connection.username", username);
        props.put("hibernate.connection.password", password);
    }
}