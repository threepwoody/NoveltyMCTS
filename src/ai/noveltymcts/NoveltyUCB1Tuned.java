package ai.noveltymcts;

import ai.Board;
import ai.Move;
import ai.NullMoveException;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;
import ai.selection.UCB1Tuned;
import utils.Util;

import java.util.List;
import java.util.Random;

public class NoveltyUCB1Tuned extends UCB1Tuned {

    private double noveltyWeight = 10;

    public double getNoveltyWeight() {
        return noveltyWeight;
    }

    public void setNoveltyWeight(double noveltyWeight) {
        this.noveltyWeight = noveltyWeight;
    }

    @Override
    public double searchValue(SearchNode node, Move move, int numberOfLegalMoves, Board board, SimulationLog log) {
        ValueEstimate valueEstimateOfMove = node.getValueEstimateOf(move);
        double result = valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove());
        double novelty = ((NoveltySearchNode)node).getNoveltyOf(move);
        int samples = valueEstimateOfMove.getSamples();
        if(noveltyWeight!=0) {
            double noveltyWeight = weightingParameter(samples);
            result = novelty*noveltyWeight + result*(1-noveltyWeight);
        }
        if(getExplorationParameter()==0) {
            return result;
        } else {
            return result + upperConfidenceBound(node, valueEstimateOfMove, numberOfLegalMoves, move.getColorOfMove());
        }
    }

    @Override
    public Move selectFromPrunedMoves(List<Move> prunedMoves, SearchNode node, Board board, Random random, SimulationLog log, boolean randomizeOrder) throws NullMoveException {
        Move result = null;
        double best = Double.NEGATIVE_INFINITY;
        int start = random.nextInt(prunedMoves.size());
        if(randomizeOrder) {
            if (Util.isDebug()) System.out.println("choosing move in node " + node.getHash());
            if (Util.isDebug())
                System.out.println("starting with legal move number " + (start + 1) + " out of " + prunedMoves.size());
            for (int i = start; i < prunedMoves.size(); i++) {
                Move move = prunedMoves.get(i);
                if (isSamplingAllMovesOnce()) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue;
                searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (Double.isNaN(searchValue)) { //TODO this is the only reason for overriding this method - still needed??
                    Util.DEBUG = true;
                    searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                    throw new NullMoveException();
                }
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
            for (int i = 0; i < start; i++) {
                Move move = prunedMoves.get(i);
                if (isSamplingAllMovesOnce()) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue;
                searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (Double.isNaN(searchValue)) {
                    Util.DEBUG = true;
                    searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                    throw new NullMoveException();
                }
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
        } else {
            for (int i = 0; i < prunedMoves.size(); i++) {
                Move move = prunedMoves.get(i);
                if (isSamplingAllMovesOnce()) {
                    if (!node.hasChildForMove(move)) {
                        if (Util.isDebug()) System.out.println("move chosen: " + move);
                        return move;
                    }
                }
                double searchValue;
                searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                if (Util.isDebug())
                    System.out.println("value of move " + move + " is: " + searchValue + " (" + node.getValueEstimateOf(move).getSamples() + " runs)");
                if (Double.isNaN(searchValue)) {
                    Util.DEBUG = true;
                    searchValue = searchValue(node, move, prunedMoves.size(), board, log);
                    throw new NullMoveException();
                }
                if (searchValue > best) {
                    best = searchValue;
                    result = move;
                }
            }
        }
        if(Util.isDebug()) System.out.println("move chosen: "+result);
        if(result==null) {
            System.out.println(board);
            System.out.println(prunedMoves);
            throw new NullMoveException();
        }
        return result;
    }

    @Override
    public String toString() {
        return "NoveltyUCB1Tuned with exploration factor="+getExplorationParameter()+", novelty weight="+noveltyWeight;
    }

    //this version combines value estimate and novelty estimate in a RAVE-like manner
    public double weightingParameter(int samplesOfMove) {
        double beta = Math.sqrt(noveltyWeight/(3*samplesOfMove+noveltyWeight));
        return beta;
    }

}
