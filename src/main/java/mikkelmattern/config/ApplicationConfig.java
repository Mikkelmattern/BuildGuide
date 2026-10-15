package mikkelmattern.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.security.Role;
import mikkelmattern.security.controller.ISecurityController;
import mikkelmattern.security.controller.SecurityController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds and starts the Javalin server.
 * <p>
 * Uses a fluent builder: each method adds a configuration step or a route group
 * and returns {@code this}, and {@link #start(int)} applies them all. Example:
 * <pre>{@code
 * new ApplicationConfig(securityController)
 *         .route(userRoutes.getRoutes())
 *         .cors()
 *         .security()
 *         .apiExceptions()
 *         .start(7070);
 * }</pre>
 */
public class ApplicationConfig {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private final ISecurityController securityController;

    // Collected by the builder methods and applied when the server starts
    private final List<EndpointGroup> routes = new ArrayList<>();
    private final List<Consumer<JavalinConfig>> configSteps = new ArrayList<>();

    private Javalin app;

    /**
     * Creates a config with the base settings already added.
     *
     * @param securityController the controller used by {@link #security()} to check requests
     */
    public ApplicationConfig(ISecurityController securityController) {
        this.securityController = securityController;
        configSteps.add(this::applyBaseConfig);
    }

    /**
     * Adds a group of routes to register when the server starts.
     *
     * @param route the route group, e.g. from {@code UserRoutes.getRoutes()}
     * @return this config, for chaining
     */
    public ApplicationConfig route(EndpointGroup route) {
        routes.add(route);
        return this;
    }

    /**
     * Enables CORS for any host, and answers {@code OPTIONS} requests with the methods each route allows.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig cors() {
        configSteps.add(config -> {
            config.bundledPlugins.enableCors(cors ->
                    cors.addRule(CorsPluginConfig.CorsRule::anyHost)
            );
            config.bundledPlugins.enableHttpAllowedMethodsOnRoutes();
        });
        return this;
    }

    /**
     * Runs authentication and then authorization before every matched route.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig security() {
        configSteps.add(config -> {
            // Order matters: authorize reads the user that authenticate stores
            config.routes.beforeMatched(securityController::authenticate);
            config.routes.beforeMatched(securityController::authorize);
        });
        return this;
    }

    /**
     * Turns a thrown {@link ApiException} into a JSON response with its status code and message,
     * e.g. {@code {"status": 404, "msg": "User not found"}}.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig apiExceptions() {
        configSteps.add(config ->
                config.routes.exception(ApiException.class, (e, ctx) -> {
                    int statusCode = e.getStatusCode();
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("status", statusCode)
                            .put("msg", e.getMessage());
                    ctx.status(statusCode).json(body);
                })
        );
        return this;
    }

    /**
     * Catches any other exception, logs it and returns a generic 500 JSON response,
     * so internal details aren't sent to the client.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig exceptions() {
        configSteps.add(config ->
                config.routes.exception(Exception.class, (e, ctx) -> {
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("status", 500)
                            .put("msg", "An unexpected error occurred");
                    logger.error("Unhandled exception", e);
                    ctx.status(500).json(body);
                })
        );
        return this;
    }

    /**
     * Returns a JSON body for 404 responses. Uses the context attribute {@code "msg"}
     * as the message if it's set, otherwise {@code "Not found"}.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig notFound() {
        configSteps.add(config ->
                config.routes.error(404, ctx -> {
                    // A handler can set "msg" to give a more specific message
                    String message = ctx.attribute("msg");
                    ObjectNode body = jsonMapper.createObjectNode()
                            .put("msg", message == null ? "Not found" : message);
                    ctx.json(body);
                })
        );
        return this;
    }

    /**
     * Prints the header names of every incoming request to the console. Useful for debugging.
     *
     * @return this config, for chaining
     */
    public ApplicationConfig requestLogger() {
        configSteps.add(config ->
                config.routes.before(ctx ->
                        ctx.req().getHeaderNames().asIterator().forEachRemaining(System.out::println)
                )
        );
        return this;
    }

    /**
     * Creates the Javalin app with all added config steps and routes, and starts it.
     *
     * @param port the port to listen on
     * @return the running Javalin app
     */
    public Javalin start(int port) {
        app = Javalin.create(config -> {
            // Apply config steps first, then register the routes
            for (Consumer<JavalinConfig> step : configSteps) {
                step.accept(config);
            }
            for (EndpointGroup route : routes) {
                config.routes.apiBuilder(route);
            }
        });

        app.start(port);
        return app;
    }

    /**
     * Stops the server if it's running. Safe to call more than once.
     */
    public void stopServer() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    /**
     * Base settings that are always applied: JSON as the default content type,
     * {@code /api} as the context path, dev logging, a route overview at {@code /routes},
     * static files from {@code /public} on the classpath, and start/stop messages.
     *
     * @param config the Javalin config to change
     */
    private void applyBaseConfig(JavalinConfig config) {
        config.http.defaultContentType = "application/json";
        config.router.contextPath = "/api";

        config.bundledPlugins.enableDevLogging();
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);

        config.staticFiles.add(files -> {
            files.hostedPath = "/";
            files.directory = "/public";
            files.location = Location.CLASSPATH;
        });

        config.events.serverStarted(() ->
                System.out.println("Javalin started on http://localhost:7070/api")
        );

        config.events.serverStopped(() ->
                System.out.println("Javalin stopped")
        );
    }

}
