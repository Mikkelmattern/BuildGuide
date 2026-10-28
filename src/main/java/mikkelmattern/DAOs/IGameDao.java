package mikkelmattern.DAOs;

import mikkelmattern.entities.Game;

import java.util.List;

public interface IGameDao {
    Game findByRawgId(long rawgId);

    Game findById(long id);

    Game create(Game game);

    List<Game> getAllGames();

}
