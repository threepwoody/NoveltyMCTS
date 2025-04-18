package ai;

import utils.UnknownPropertyException;

public interface Player {

    void acceptPlayMove(Move m);

    Move bestMove() throws NullMoveException;

    default void endGame() {};

    Board getBoard();

    Game getGame();

    void setGame(Game game);

    void initialize();

    void setProperty(String property, String value) throws UnknownPropertyException;

}
