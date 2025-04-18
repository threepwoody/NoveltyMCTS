package ai.selection;

import ai.Board;
import ai.Move;
import ai.NullMoveException;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;
import utils.Util;

import java.util.List;
import java.util.Random;

public class EpsilonGreedy extends PruningSelectionPolicy implements ExplorationExploitationPolicy {

    //probability of making a random move
    private double epsilon = 0.1;

    public EpsilonGreedy() {}

    public EpsilonGreedy(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public Move selectFromPrunedMoves(List<Move> prunedMoves, SearchNode node, Board board, Random random, SimulationLog log, boolean randomizeOrder) {
        Move result = null;
        double best = Double.NEGATIVE_INFINITY;
        int start = random.nextInt(prunedMoves.size());
        if(Util.isDebug()) System.out.println("choosing move in node "+node.getHash());
        //either return random move
        if(random.nextDouble()<epsilon) {
            if(Util.isDebug()) System.out.println("random move chosen: "+prunedMoves.get(start));
            return prunedMoves.get(start);
        }
        if(randomizeOrder) {
            //or the one with the highest value estimate, without any further exploration term added
            for (int i = start; i < prunedMoves.size(); i++) {
                Move move = prunedMoves.get(i);
                double searchValue;
                searchValue = searchValue(node, move, board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
            for (int i = 0; i < start; i++) {
                Move move = prunedMoves.get(i);
                double searchValue;
                searchValue = searchValue(node, move, board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
        } else {
            for (int i = 0; i < prunedMoves.size(); i++) {
                Move move = prunedMoves.get(i);
                double searchValue;
                searchValue = searchValue(node, move, board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
        }
        if(Util.isDebug()) System.out.println("move chosen: "+result);
        return result;
    }

    public double searchValue(SearchNode node, Move move, Board board, SimulationLog log) {
        ValueEstimate valueEstimateOfMove = node.getValueEstimateOf(move);
        return valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove());
    }

    @Override
    public void setExplorationParameter(double exploration) {
        epsilon = exploration;
    }

    @Override
    public double getExplorationParameter() {
        return epsilon;
    }

    @Override
    public String toString() {
        return "EpsilonGreedy with epsilon="+epsilon+", exploration factor="+getExplorationParameter();
    }

}
