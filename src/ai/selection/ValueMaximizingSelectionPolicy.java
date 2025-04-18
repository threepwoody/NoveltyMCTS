package ai.selection;

import ai.Board;
import ai.Move;
import ai.NullMoveException;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import utils.Util;

import java.util.List;
import java.util.Random;

public abstract class ValueMaximizingSelectionPolicy extends PruningSelectionPolicy {

    private boolean samplingAllMovesOnce = false;

    public boolean isSamplingAllMovesOnce() {
        return samplingAllMovesOnce;
    }

    public void setSamplingAllMovesOnce(boolean samplingAllMovesOnce) {
        this.samplingAllMovesOnce = samplingAllMovesOnce;
    }

    abstract public double searchValue(SearchNode node, Move move, int numberOfLegalMoves, Board simulationBoard, SimulationLog log);

    @Override
    public Move selectFromPrunedMoves(List<Move> prunedMoves, SearchNode node, Board board, Random random, SimulationLog log, boolean randomizeOrder) throws NullMoveException {
        Move result = null;
        double best = Double.NEGATIVE_INFINITY;
        int start = random.nextInt(prunedMoves.size());
        if(randomizeOrder) {
            if (Util.isDebug()) {
                System.out.println("starting with legal move number " + (start + 1) + " out of " + prunedMoves.size());
                System.out.println("parent runs (=sum of all children runs): " + node.getTotalChildrenSamples());
            }
            for (int i = start; i < prunedMoves.size(); i++) {
                Move move = prunedMoves.get(i);
                if (samplingAllMovesOnce) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
            for (int i = 0; i < start; i++) {
                Move move = prunedMoves.get(i);
                if (samplingAllMovesOnce) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue = searchValue(node, move, prunedMoves.size(), board, log);
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
                if (samplingAllMovesOnce) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue = searchValue(node, move, prunedMoves.size(), board, log);
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

}
