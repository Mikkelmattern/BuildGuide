package mikkelmattern.mappers;

import mikkelmattern.DTO.GuideDTO;
import mikkelmattern.entities.Guide;
import mikkelmattern.entities.User;

public class GuideMapper {
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

    public static Guide toEntity(GuideDTO dto, User author){
        return Guide.builder()
                .id(dto.id())
                .title(dto.title())
                .content(dto.content())
                .author(author)
                .build();
    }
}
