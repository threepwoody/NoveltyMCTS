package experiments.AlphaZero;

import ai.*;
import utils.UnknownPropertyException;

import java.util.List;

import static experiments.GameFactory.createGame;

public class StateNetworkPolicyOutputPlayer implements Player {

    private Board board;
    private StateNetworkEvaluator evaluator;
    private AlphaZeroGame game;
    private String networkParameterFile;

    public StateNetworkPolicyOutputPlayer() {
    }

    public StateNetworkPolicyOutputPlayer(Game game) {
        this();
        setGame(game);
    }

    @Override
    public void acceptPlayMove(Move m) {
        board.play(m);
    }

    @Override
    public Move bestMove() throws NullMoveException {
        MovePriorEvaluation movePriorEvaluation = (MovePriorEvaluation) evaluator.staticEval(board);
        List<Double[]> movePriorsPerDimension = movePriorEvaluation.getMovePriors();
        List<Move> legalMoves = board.getLegalMoves();
        double maxPrior = Double.NEGATIVE_INFINITY;
        Move bestMove = null;
        for(Move move : legalMoves) {
            double prior = getMovePrior(game.moveIndex(1, move), movePriorsPerDimension);
            if(prior>maxPrior) {
                maxPrior = prior;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private double getMovePrior(int[] moveIndex, List<Double[]> movePriorsPerDimension) {
        double result = 1;
        for(int dim=0;dim<moveIndex.length;dim++) {
            result *= movePriorsPerDimension.get(dim)[moveIndex[dim]];
        }
        return result;
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
        evaluator = game.newStateNetworkEvaluator(networkParameterFile, true, false);
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
            throw new UnknownPropertyException(property + " is not a known property for StateNetworkPolicyOutputPlayer.");
        }
    }

}
