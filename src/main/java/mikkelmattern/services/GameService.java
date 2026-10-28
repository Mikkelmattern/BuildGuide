package mikkelmattern.services;

import lombok.AllArgsConstructor;
import mikkelmattern.DAOs.IGameDao;
import mikkelmattern.DTOs.GameDTO;
import mikkelmattern.clients.RawgClient;
import mikkelmattern.entities.Game;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.mappers.GameMapper;
import mikkelmattern.records.RawgGameDTO;
import mikkelmattern.security.AuthUser;
import mikkelmattern.security.SecurityService;

import java.util.List;

@AllArgsConstructor
public class GameService {
    RawgClient rawgClient;
    IGameDao gameDao;
    SecurityService securityService;



    public List<RawgGameDTO> searchRawg(String query, AuthUser authUser) {
        if(!securityService.verifyAdminAccess(authUser)){
            throw new ApiException(403, "Admin access only");
        }
        if (query == null || query.isBlank()) {
            throw new ApiException(400, "Missing search query");
        }
        return rawgClient.search(query);
    }

    public GameDTO importGame(long rawgId, AuthUser authUser) {
       if(!securityService.verifyAdminAccess(authUser)){
           throw new ApiException(403, "Admin access only");
       }
        if (gameDao.findByRawgId(rawgId) != null) {
            throw new ApiException(409, "Game is already imported");
        }
        RawgGameDTO rawg = rawgClient.getGame(rawgId);

        Game game = new Game();
        game.setRawgId(rawg.id());
        game.setName(rawg.name());
        game.setCoverImageUrl(rawg.backgroundImage());
        game.setReleased(rawg.released());

        return GameMapper.toDTO(gameDao.create(game));
    }

    public List<GameDTO> getAllGames() {
       List<Game> games = gameDao.getAllGames();
       return games.stream().map(GameMapper::toDTO).toList();
    }

    public GameDTO getGameById(Long id){
        return GameMapper.toDTO(gameDao.findById(id));
    }
}
