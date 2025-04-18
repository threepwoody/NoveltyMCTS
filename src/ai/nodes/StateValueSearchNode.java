package ai.nodes;

import ai.Evaluation;
import ai.Move;
import utils.Util;

public class StateValueSearchNode extends BasicSearchNode {

    private ValueEstimate stateValueEstimate;
    private ValueEstimate untriedStateValueEstimate;

    @Override
    public int getTotalChildrenSamples() {
        return getRealChildrenSamples()+getNumberOfLegalMoves()*untriedStateValueEstimate.getSamples();
    }

    @Override
    public ValueEstimate getValueEstimateOf(Move move) {
        if(hasChildForMove(move)) {
            return ((StateValueSearchNode)getChildForMove(move)).stateValueEstimate;
        } else {
            return untriedStateValueEstimate;
        }
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move) {
        super.recordEvaluation(evaluation, move);
        if(Util.isDebug()) { System.out.println("recording "+evaluation+" for state of node with hash "+ getHash()); }
        stateValueEstimate.recordEvaluation(evaluation);
        if(move!=null && hasChildForMove(move)) {
            setRealChildrenSamples(getRealChildrenSamples() + 1);
        }
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move, int weight) {
        super.recordEvaluation(evaluation, move, weight);
        if(Util.isDebug()) { System.out.println("recording "+evaluation+" with weight "+weight+" for state of node with hash "+ getHash()); }
        stateValueEstimate.recordEvaluation(evaluation, weight);
        if(move!=null && hasChildForMove(move)) {
            setRealChildrenSamples(getRealChildrenSamples() + weight);
        }
    }

    @Override
    public void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves) {
        super.reset(hash, builder, numberOfColors, numberOfLegalMoves);
        stateValueEstimate = new ValueEstimate(builder, numberOfColors);
        untriedStateValueEstimate = new ValueEstimate(builder, numberOfColors);
    }

    @Override
    public void setColorToPlay(int colorToPlay) {
        super.setColorToPlay(colorToPlay);
    }

}
