package mikkelmattern.services;

import mikkelmattern.DTOs.UserInDTO;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.DAOs.UserDaoImpl;
import mikkelmattern.entities.User;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.UserMapper;
import mikkelmattern.security.AuthUser;
import mikkelmattern.security.Role;
import mikkelmattern.security.SecurityService;

import java.util.List;

/**
 * Business logic for users. Calls the DAO and converts the results to {@link UserOutDTO}s.
 */
public class UserService {
    private final UserDaoImpl userDao;
    private final UserMapper userMapper = new UserMapper();
    private final SecurityService securityService;

    /**
     * Creates a user service that uses the given DAO.
     *
     * @param userDao         the DAO used to read and write users
     * @param securityService the service used for access checks
     */
    public UserService(UserDaoImpl userDao, SecurityService securityService) {
        this.userDao = userDao;
        this.securityService = securityService;
    }

    /**
     * Not implemented yet. Passwords are already hashed by the {@link User} constructor.
     *
     * @param user the user whose password to hash
     */
    public static void hashPassword(User user) {

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
     */
    public UserOutDTO createUser(UserInDTO userInDTO) {
        User user = userMapper.toEntity(userInDTO);
        user.addRole(Role.USER);
        return userMapper.toDTO(userDao.save(user));
    }

    /**
     * Gets one user by id.
     *
     * @param id the user's id
     * @return the user as a DTO
     */
    public UserOutDTO getUserDTOByID(long id) {
        return userMapper.toDTO(userDao.findByID(id));
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
     *                      or 403 if {@code authUser} is neither that user nor an admin
     */
    public UserOutDTO updateUser(long id, UserInDTO changes, AuthUser authUser) {
        // Load the real row, so the password hash and roles are kept
        User user = userDao.findByID(id);
        if (user == null) {
            throw new ApiException(404, "Could not find the user");
        }

        // Only the user themself or an admin may change it
        securityService.verifyAccess(user, authUser);

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
     * @throws ApiException with status 400 if no user has that id,
     *                      or 403 if {@code authUser} is neither that user nor an admin
     */
    public UserOutDTO deleteUser(long id, AuthUser authUser) {
        User user = userDao.findByID(id);
        if (user == null) {
            throw new ApiException(400, "Could not find the user");
        }
        securityService.verifyAccess(user, authUser);
        return userMapper.toDTO(userDao.delete(user));
    }
}
