package mikkelmattern.routes;

import io.javalin.apibuilder.EndpointGroup;

/**
 * A group of routes that can be added to the server with {@code ApplicationConfig.route(...)}.
 */
public interface IRoutes {

    /**
     * Returns the routes in this group.
     *
     * @return the endpoint group to register
     */
    public EndpointGroup getRoutes();
}
