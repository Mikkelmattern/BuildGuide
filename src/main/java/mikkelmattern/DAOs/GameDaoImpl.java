package mikkelmattern.DAOs;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.entities.Game;

import java.util.List;

public class GameDaoImpl extends DAO implements IGameDao {
    /**
     * Creates a DAO that uses the given entity manager factory.
     *
     * @param emf the factory used to create entity managers
     */
    public GameDaoImpl(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    public Game findByRawgId(long id) {
        return executeQuery(em ->
                em.createQuery("SELECT g FROM Game g WHERE g.rawgId = :rawgId", Game.class)
                        .setParameter("rawgId", id).getSingleResult()
        );
    }

    @Override
    public Game findById(long id) {
        return executeQuery(em -> em.find(Game.class, id));
    }

    @Override
    public Game create(Game game) {
        executeUpdate(em -> em.persist(game));
        return game;
    }

    @Override
    public List<Game> getAllGames() {
        return executeQuery(em -> em.createQuery("SELECT g FROM Game g", Game.class).getResultList());
    }
}
