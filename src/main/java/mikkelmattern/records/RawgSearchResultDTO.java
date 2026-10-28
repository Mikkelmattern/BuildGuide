package mikkelmattern.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgSearchResultDTO(List<RawgGameDTO> results) {}
