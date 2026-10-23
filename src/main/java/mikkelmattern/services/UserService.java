package mikkelmattern.services;

import mikkelmattern.DAOs.IUserDao;
import mikkelmattern.DTOs.UserInDTO;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.entities.User;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.UserMapper;
import mikkelmattern.security.AuthUser;
import mikkelmattern.enums.Role;
import mikkelmattern.security.SecurityService;

import java.util.List;

/**
 * Business logic for users. Calls the DAO and converts the results to {@link UserOutDTO}s.
 */
public class UserService {
    private final IUserDao userDao;
    private final UserMapper userMapper = new UserMapper();
    private final SecurityService securityService;

    /**
     * Creates a user service that uses the given DAO.
     *
     * @param userDao         the DAO used to read and write users
     * @param securityService the service used for access checks
     */
    public UserService(IUserDao userDao, SecurityService securityService) {
        this.userDao = userDao;
        this.securityService = securityService;
    }

    /**
     * Gets all users from the database.
     *
     * @return every user as a DTO
     */
    public List<UserOutDTO> getAllUsersInDTO() {
        return userMapper.toDTOList(userDao.getAll());
    }

    /**
     * Saves a new user with the {@link Role#USER} role.
     *
     * @param userInDTO the new user's details; the password is hashed by the {@link User} constructor
     * @return the saved user as a DTO, with their generated id
     * @throws ApiException with status 400 if the email, username or password is missing,
     *                      or 409 if the username or email is already in use
     */
    public UserOutDTO createUser(UserInDTO userInDTO) {
        User user = userMapper.toEntity(userInDTO);
        user.addRole(Role.USER);
        if (userInDTO.email() == null) {
            throw new ApiException(400, "No email set");
        } else if (userInDTO.username() == null) {
            throw new ApiException(400, "No username set");
        } else if (userInDTO.password() == null) {
            throw new ApiException(400, "No password set");
        }

        if (userDao.findUserByName(userInDTO.username()) != null) {
            throw new ApiException(409, "Username is already taken");
        }
        if (userDao.findUserByEmail(userInDTO.email()) != null) {
            throw new ApiException(409, "Email is already in use");
        }
        return userMapper.toDTO(userDao.save(user));
    }

    /**
     * Gets one user by id.
     *
     * @param id the user's id
     * @return the user as a DTO
     * @throws ApiException with status 404 if no user has that id
     */
    public UserOutDTO getUserDTOByID(long id) {
        return userMapper.toDTO(findUserOrThrow(id));
    }

    /**
     * Updates an existing user. The user is loaded from the database, so the password hash
     * and roles are kept, and only the fields that are set in {@code changes} are changed.
     *
     * @param id       the id of the user to update
     * @param changes  the new values; {@code null} fields are left unchanged, and a new password is hashed
     * @param authUser the logged-in user making the request
     * @return the updated user as a DTO
     * @throws ApiException with status 404 if no user has that id,
     *                      403 if {@code authUser} is neither that user nor an admin,
     *                      or 409 if the new username or email is already used by another user
     */
    public UserOutDTO updateUser(long id, UserInDTO changes, AuthUser authUser) {
        // Load the real row, so the password hash and roles are kept
        User user = userDao.findByID(id);
        if (user == null) {
            throw new ApiException(404, "Could not find the user");
        }

        // Only the user themself or an admin may change it
        securityService.verifyAccess(user, authUser);

        if (changes.username() != null && !changes.username().equals(user.getUsername())
                && userDao.findUserByName(changes.username()) != null) {
            throw new ApiException(409, "Username is already taken");
        }

        if (changes.email() != null && !changes.email().equals(user.getEmail())
                && userDao.findUserByEmail(changes.email()) != null) {
            throw new ApiException(409, "Email is already in use");
        }

        // Only change the fields that were sent
        if (changes.username() != null) user.setUsername(changes.username());
        if (changes.email() != null) user.setEmail(changes.email());
        if (changes.password() != null) user.setPassword(changes.password());   // hashes it

        userDao.update(user);
        return userMapper.toDTO(user);
    }

    /**
     * Deletes a user.
     *
     * @param id       the id of the user to delete
     * @param authUser the logged-in user making the request
     * @return the deleted user as a DTO
     * @throws ApiException with status 404 if no user has that id,
     *                      or 403 if {@code authUser} is neither that user nor an admin
     */
    public UserOutDTO deleteUser(long id, AuthUser authUser) {
        User user = userDao.findByID(id);
        if (user == null) {
            throw new ApiException(404, "Could not find the user");
        }
        securityService.verifyAccess(user, authUser);
        return userMapper.toDTO(userDao.delete(user));
    }

    /**
     * Gives a user a role. Only admins may call this, which the route enforces.
     * <p>
     * The user's token still holds their old roles, so the change takes effect at their next login.
     *
     * @param id       the id of the user to update
     * @param roleName the name of the role to add, e.g. {@code "AUTHOR"}
     * @return the updated user as a DTO
     * @throws ApiException with status 404 if no user has that id,
     *                      or 400 if {@code roleName} isn't a role that can be given
     */
    public UserOutDTO addRole(long id, String roleName) {
        User user = findUserOrThrow(id);
        user.addRole(parseRole(roleName));
        userDao.update(user);
        return userMapper.toDTO(user);
    }

    /**
     * Removes a role from a user. Only admins may call this, which the route enforces.
     * <p>
     * The user's token still holds their old roles, so the change takes effect at their next login.
     *
     * @param id       the id of the user to update
     * @param roleName the name of the role to remove, e.g. {@code "AUTHOR"}
     * @param authUser the logged-in admin making the request
     * @return the updated user as a DTO
     * @throws ApiException with status 404 if no user has that id, 400 if {@code roleName}
     *                      isn't a role that can be given, or 400 if an admin tries to
     *                      remove their own admin role
     */
    public UserOutDTO removeRole(long id, String roleName, AuthUser authUser) {
        User user = findUserOrThrow(id);
        Role role = parseRole(roleName);
        // Stops the last admin from locking themself out
        if (role == Role.ADMIN && user.getId().equals(authUser.id())) {
            throw new ApiException(400, "You can't remove your own admin role");
        }
        user.removeRole(role.name());
        userDao.update(user);
        return userMapper.toDTO(user);
    }

    /**
     * Finds a user by id.
     *
     * @param id the user's id
     * @return the user
     * @throws ApiException with status 404 if no user has that id
     */
    private User findUserOrThrow(long id) {
        User user = userDao.findByID(id);
        if (user == null) {
            throw new ApiException(404, "Could not find the user");
        }
        return user;
    }

    /**
     * Turns a role name into a {@link Role}, ignoring case.
     *
     * @param roleName the role's name, e.g. {@code "author"}
     * @return the role
     * @throws ApiException with status 400 if the name isn't a role, or is {@link Role#ANYONE},
     *                      which only marks open routes and can't be given to a user
     */
    private Role parseRole(String roleName) {
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            if (role != Role.ANYONE) {
                return role;
            }
        } catch (IllegalArgumentException ignored) {
            // Falls through to the error below
        }
        throw new ApiException(400, "Unknown role: " + roleName);
    }
}
