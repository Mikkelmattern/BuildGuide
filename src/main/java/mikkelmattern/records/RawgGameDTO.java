package mikkelmattern.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgGameDTO(
        Long id,
        String name,
        LocalDate released,
        @JsonProperty("background_image") String backgroundImage
) {}

