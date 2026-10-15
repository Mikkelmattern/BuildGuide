package mikkelmattern.mappers;

import mikkelmattern.DTOs.UserInDTO;
import mikkelmattern.DTOs.UserOutDTO;
import mikkelmattern.entities.User;

import java.util.List;

/**
 * Converts between {@link User} entities and the user DTOs:
 * {@link UserInDTO} coming in and {@link UserOutDTO} going out.
 * <p>
 * The method docs are inherited from {@link IMapper}.
 */
public class UserMapper implements IMapper<User, UserInDTO, UserOutDTO> {

    @Override
    public UserOutDTO toDTO(User user) {
        return UserOutDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    public List<UserOutDTO> toDTOList(List<User> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The password is hashed by the {@link User} constructor.
     */
    @Override
    public User toEntity(UserInDTO dto) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * A {@link UserOutDTO} has no password, so the returned user has none set.
     * Don't save it as a new user; use it to look up or identify an existing one.
     */
    @Override
    public User toEntityFromOut(UserOutDTO dto) {
        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .build();
        // The builder has no id, so it is set afterwards
        user.setId(dto.id());
        return user;
    }

    @Override
    public List<User> toEntityList(List<UserInDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}
