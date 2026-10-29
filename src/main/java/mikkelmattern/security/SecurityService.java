package mikkelmattern.security;

import mikkelmattern.DAOs.IUserDao;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.entities.User;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.UserMapper;

/**
 * Handles login: checks a user's credentials and issues a JWT.
 */
public class SecurityService {

    private final IUserDao userDao;
    private final TokenService tokenService;
    private final UserMapper userMapper = new UserMapper();

    /**
     * Creates a security service that looks users up through the given DAO.
     *
     * @param userDao      the DAO used to find users by username
     * @param tokenService the service used to create tokens; must be the same one the
     *                     security controller verifies with, so both use the same secret
     */
    public SecurityService(IUserDao userDao, TokenService tokenService) {
        this.userDao = userDao;
        this.tokenService = tokenService;
    }

    /**
     * Logs a user in and returns a JWT containing their {@link UserOutDTO} and roles.
     *
     * @param username the username to log in with
     * @param password the plain-text password
     * @return a signed JWT for the user
     * @throws ApiException with status 401 if the username or password is wrong
     */
    public String login(String username, String password) {
        User user = userDao.findUserByName(username);
        if (!verifyPassword(user, password)) {
            throw new ApiException(401, "Invalid username or password");
        }
        return tokenService.createToken(userMapper.toDTO(user), user.getRolesAsStrings());
    }

    /**
     * Checks a password against a user, treating a missing user as a failed check.
     *
     * @param user     the user to check, or {@code null} if no user was found
     * @param password the plain-text password
     * @return {@code true} if the user exists and the password matches, otherwise {@code false}
     */
    private boolean verifyPassword(User user, String password) {
        return user != null && user.verifyPassword(password);
    }

    /**
     * Checks whether the logged-in user is the same user as {@code user}.
     *
     * @param user     the user being accessed
     * @param authUser the logged-in user making the request
     * @return {@code true} if the ids match, otherwise {@code false}
     */
    private boolean verifyPersonalAccess(User user, AuthUser authUser) {
        // Compares ids, not usernames, since a username can change while the token still holds the old one
        return user.getId().equals(authUser.id());
    }

    /**
     * Checks whether the logged-in user is an admin.
     *
     * @param authUser the logged-in user making the request
     * @return {@code true} if the user has the {@code "ADMIN"} role, otherwise {@code false}
     */
    public boolean verifyAdminAccess(AuthUser authUser) {
        return authUser.roles().contains("ADMIN");
    }

    /**
     * Checks that the logged-in user may change or delete something owned by {@code user},
     * such as their account or a guide they wrote: either they are that user, or they are an admin.
     *
     * @param user     the owner of what is being accessed
     * @param authUser the logged-in user making the request
     * @throws ApiException with status 403 if the user is neither the owner nor an admin
     */
    public void verifyAccess(User user, AuthUser authUser) {
        if (!(hasAccess(user, authUser)))
            throw new ApiException(403, "You can only change your own data");
    }

    public boolean hasAccess(User user, AuthUser authUser) {
        return verifyAdminAccess(authUser) || verifyPersonalAccess(user, authUser);
    }

    public boolean verifyLoggedIn(User user, AuthUser authUser){
        return authUser.roles().contains("USER") || authUser.roles().contains("ADMIN");
    }

}
