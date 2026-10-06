package mikkelmattern.DTO;

import lombok.Builder;

@Builder
public record UserDTO(
        Long id,
        String username,
        String email
) {
}
