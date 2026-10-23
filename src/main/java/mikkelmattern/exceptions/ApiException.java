package mikkelmattern.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An error that should be sent to the client with a specific HTTP status code.
 * <p>
 * {@code ApplicationConfig.apiExceptions()} turns it into a JSON response.
 * The error is logged when the exception is created.
 */
public class ApiException extends RuntimeException {
    private int code;
    private static final Logger logger = LoggerFactory.getLogger(ApiException.class);

    /**
     * Creates and logs an API exception. Client errors (4xx) are logged as warnings,
     * since they are part of normal use, and server errors (5xx) as errors.
     *
     * @param code the HTTP status code, e.g. 401 or 404
     * @param msg  the message to send to the client
     */
    public ApiException(int code, String msg){
        super(msg);
        this.code = code;
        if (code >= 500) {
            logger.error("ApiException (code={}): {}", code, msg);
        } else {
            logger.warn("ApiException (code={}): {}", code, msg);
        }
    }

    /**
     * Returns the HTTP status code for the response.
     *
     * @return the status code
     */
    public int getStatusCode(){
        return code;
    }
}
