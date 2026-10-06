package mikkelmattern.mappers;

import mikkelmattern.DTO.UserDTO;
import mikkelmattern.entities.User;

public class UserMapper {
    public static UserDTO toDTO(User user){
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public static User toEntity(UserDTO dto){
        return User.builder()
                .id(dto.id())
                .username(dto.username())
                .email(dto.email())
                .build();
    }
}
