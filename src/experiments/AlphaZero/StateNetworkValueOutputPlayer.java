package experiments.AlphaZero;

import ai.*;
import utils.UnknownPropertyException;

import java.util.List;

import static experiments.GameFactory.createGame;

public class StateNetworkValueOutputPlayer implements Player {

    private Board board;
    private StateNetworkEvaluator evaluator;
    private AlphaZeroGame game;
    private String networkParameterFile;

    public StateNetworkValueOutputPlayer() {
    }

    public StateNetworkValueOutputPlayer(Game game) {
        this();
        setGame(game);
    }

    @Override
    public void acceptPlayMove(Move m) {
        board.play(m);
    }

    @Override
    public Move bestMove() throws NullMoveException {
        Board simulationBoard = game.newBoard();
        List<Move> legalMoves = board.getLegalMoves();
        double maxValue = Double.NEGATIVE_INFINITY;
        Move bestMove = null;
        for(Move move : legalMoves) {
            simulationBoard.copyDataFrom(board);
            simulationBoard.play(move);
            Evaluation evaluation = evaluator.staticEval(simulationBoard);
            double value = evaluation.getValueForColor(board.getColorToPlay());
            if(value>maxValue) {
                maxValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void setGame(Game game) {
        this.game = (AlphaZeroGame) game;
        board = game.newBoard();
    }

    @Override
    public void initialize() {
        evaluator = game.newStateNetworkEvaluator(networkParameterFile, false, false);
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("evaluation")) {
            networkParameterFile = value;
        } else if (property.equals("game")) {
            Game prototype = createGame(value);
            setGame(prototype);
        } else if(property.startsWith("color")) {
        } else if (property.equals("randomized")) {
        } else {
            throw new UnknownPropertyException(property + " is not a known property for StateNetworkValueOutputPlayer.");
        }
    }

}
