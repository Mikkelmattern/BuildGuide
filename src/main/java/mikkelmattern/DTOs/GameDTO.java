package mikkelmattern.DTOs;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GameDTO(Long id,
                      Long rawgId,
                      String name,
                      LocalDate released,
                      String coverImageUrl) {
}
