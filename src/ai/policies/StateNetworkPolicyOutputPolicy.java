package ai.policies;

import ai.Board;
import ai.Game;
import ai.Move;
import ai.SimulationLog;
import experiments.AlphaZero.AlphaZeroGame;
import experiments.AlphaZero.MovePriorEvaluation;
import experiments.AlphaZero.StateNetworkEvaluator;

import java.util.List;
import java.util.Random;

public class StateNetworkPolicyOutputPolicy implements Policy {

    private StateNetworkEvaluator evaluator;

    public StateNetworkPolicyOutputPolicy(AlphaZeroGame game, String networkParameterFile) {
        evaluator = game.newStateNetworkEvaluator(networkParameterFile, true, false);
    }

    public StateNetworkPolicyOutputPolicy(StateNetworkEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    private double getMovePrior(int[] moveIndex, List<Double[]> movePriorsPerDimension) {
        double result = 1;
        for(int dim=0;dim<moveIndex.length;dim++) {
            result *= movePriorsPerDimension.get(dim)[moveIndex[dim]];
        }
        return result;
    }

    @Override
    public Move selectMove(Game game, Board board, Random random, SimulationLog log) {
        AlphaZeroGame azGame = (AlphaZeroGame) game;
        MovePriorEvaluation movePriorEvaluation = (MovePriorEvaluation) evaluator.staticEval(board);
        List<Double[]> movePriorsPerDimension = movePriorEvaluation.getMovePriors();
        List<Move> legalMoves = board.getLegalMoves();
        double maxPrior = Double.NEGATIVE_INFINITY;
        Move bestMove = null;
        for(Move move : legalMoves) {
            double prior = getMovePrior(azGame.moveIndex(1, move), movePriorsPerDimension);
            if(prior>maxPrior) {
                maxPrior = prior;
                bestMove = move;
            }
        }
        return bestMove;
    }

}
