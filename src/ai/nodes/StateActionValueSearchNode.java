package ai.nodes;

import ai.Evaluation;
import ai.Move;
import utils.Util;

import java.util.HashMap;

public class StateActionValueSearchNode extends BasicSearchNode {

    private HashMap<Move, ValueEstimate> moveValueEstimates;
    //seen children are those that have been sampled before and have at least one valid result stored for them. the corresponding child node needs not be expanded yet
    private ValueEstimate untriedMoveValueEstimate;

    @Override
    public int getTotalChildrenSamples() {
        return getRealChildrenSamples()+getNumberOfLegalMoves()*untriedMoveValueEstimate.getSamples();
    }

    @Override
    public ValueEstimate getValueEstimateOf(Move move) {
        if(moveValueEstimates.containsKey(move)) {
            return moveValueEstimates.get(move);
        } else {
            return untriedMoveValueEstimate;
        }
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move) {
        super.recordEvaluation(evaluation, move);
        if(move==null) {
            if(Util.isDebug()) { System.out.println("recording nothing, because no move made from here"); }
            return;
        }
        if(Util.isDebug()) { System.out.println("recording "+evaluation+" for move "+move); }
        if(Util.isDebug() && !moveValueEstimates.containsKey(move)) { System.out.println("first recording for this move!"); }
        ValueEstimate valueEstimate;
        if(moveValueEstimates.containsKey(move)) {
            valueEstimate = moveValueEstimates.get(move);
        } else {
            valueEstimate = new ValueEstimate(untriedMoveValueEstimate);
            moveValueEstimates.put(move, valueEstimate);
        }
        valueEstimate.recordEvaluation(evaluation);
        setRealChildrenSamples(getRealChildrenSamples()+1);
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move, int weight) {
        super.recordEvaluation(evaluation, move, weight);
        if(move==null) {
//            if(Util.isDebug()) { System.out.println("recording nothing in node with hash "+ getHash() + ", because no move made from here"); }
            if(Util.isDebug()) { System.out.println("recording nothing, because no move made from here"); }
            return;
        }
//        if(Util.isDebug()) { System.out.println("recording "+evaluation+" for move "+move+" in state of node with hash "+ getHash()); }
        if(Util.isDebug()) { System.out.println("recording "+evaluation+" for move "+move+" with weight of "+weight+" samples"); }
        if(Util.isDebug() && !moveValueEstimates.containsKey(move)) { System.out.println("first recording for this move!"); }
        ValueEstimate valueEstimate;
        if(moveValueEstimates.containsKey(move)) {
            valueEstimate = moveValueEstimates.get(move);
        } else {
            valueEstimate = new ValueEstimate(untriedMoveValueEstimate);
            moveValueEstimates.put(move, valueEstimate);
        }
        valueEstimate.recordEvaluation(evaluation, weight);
        setRealChildrenSamples(getRealChildrenSamples()+weight);
    }

    @Override
    public void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves) {
        super.reset(hash, builder, numberOfColors, numberOfLegalMoves);
        moveValueEstimates = new HashMap<>();
        untriedMoveValueEstimate = new ValueEstimate(builder, numberOfColors);
    }

}
