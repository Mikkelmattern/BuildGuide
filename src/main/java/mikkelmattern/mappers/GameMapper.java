package mikkelmattern.mappers;

import mikkelmattern.DTOs.GameDTO;
import mikkelmattern.entities.Game;

public class GameMapper {
    public static GameDTO toDTO(Game game){
        return GameDTO.builder()
                .name(game.getName())
                .rawgId(game.getRawgId())
                .id(game.getId())
                .released(game.getReleased())
                .coverImageUrl(game.getCoverImageUrl())
                .build();
    }
}
