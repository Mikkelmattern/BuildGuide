package mikkelmattern.mappers;

import mikkelmattern.DTOs.GuideDTO;
import mikkelmattern.entities.Guide;
import mikkelmattern.entities.User;

/**
 * Converts between {@link Guide} entities and {@link GuideDTO}s.
 */
public class GuideMapper {

    /**
     * Converts a guide to a DTO, with the author's id and username.
     *
     * @param guide the guide to convert; its author must be set
     * @return the guide as a DTO
     */
    public static GuideDTO toDTO(Guide guide){
        return GuideDTO.builder()
                .id(guide.getId())
                .title(guide.getTitle())
                .content(guide.getContent())
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .authorId(guide.getAuthor().getId())
                .authorUsername(guide.getAuthor().getUsername())
                .build();
    }

    /**
     * Converts a DTO to a guide entity. The timestamps aren't copied, since JPA sets them.
     *
     * @param dto    the DTO to convert
     * @param author the user who wrote the guide
     * @return the guide entity
     */
    public static Guide toEntity(GuideDTO dto, User author){
        return Guide.builder()
                .id(dto.id())
                .title(dto.title())
                .content(dto.content())
                .author(author)
                .build();
    }
}
