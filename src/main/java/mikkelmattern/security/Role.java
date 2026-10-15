package mikkelmattern.security;

import io.javalin.security.RouteRole;

/**
 * The roles used to protect routes.
 * <ul>
 *     <li>{@link #ADMIN} - administrators</li>
 *     <li>{@link #USER} - logged-in users</li>
 *     <li>{@link #ANYONE} - open routes that need no login</li>
 * </ul>
 */
public enum Role implements RouteRole {
    ADMIN, USER, ANYONE;


}
