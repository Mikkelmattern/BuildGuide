package mikkelmattern.utils;

import mikkelmattern.exceptions.ApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * General helper methods.
 */
public class Utils {

    /**
     * Reads a value from a properties file on the classpath.
     *
     * @param propName     the name of the property, e.g. {@code "DB_NAME"}
     * @param resourceName the properties file, e.g. {@code "config.properties"}
     * @return the property's value, with surrounding whitespace removed
     * @throws ApiException with status 500 if the property is missing or the file can't be read
     */
    public static String getPropertyValue(String propName, String resourceName)  {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourceName)) {
            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);
            if (value != null) {
                return value.trim();  // Trim whitespace
            } else {
                throw new ApiException(500, String.format("Property %s not found in %s", propName, resourceName));
            }
        } catch (IOException ex) {
            throw new ApiException(500, String.format("Could not read property %s.", propName));
        }
    }
}