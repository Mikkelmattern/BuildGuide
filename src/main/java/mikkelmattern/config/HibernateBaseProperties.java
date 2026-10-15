package mikkelmattern.config;

import java.util.Properties;

/**
 * Hibernate settings shared by every environment.
 */
final class HibernateBaseProperties {

    private HibernateBaseProperties() {}

    /**
     * Creates the base Hibernate properties: the PostgreSQL driver, SQL logging turned off
     * and the Hikari connection pool settings. Connection details are added separately.
     *
     * @return a new set of base properties
     */
    static Properties createBase() {
        Properties props = new Properties();
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.current_session_context_class", "thread");

        // SQL logging - set to true to see the queries Hibernate runs
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");

        // Connection pool: up to 10 connections, at least 2 idle, 20 second timeout
        props.put("hibernate.hikari.maximumPoolSize", "10");
        props.put("hibernate.hikari.minimumIdle", "2");
        props.put("hibernate.hikari.connectionTimeout", "20000");
        return props;
    }
}