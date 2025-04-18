package ai.noveltymcts;

import ai.Evaluation;
import ai.Move;
import ai.nodes.SearchNodeBuilder;
import ai.nodes.StateActionValueSearchNode;
import utils.Util;

public class BasicNoveltySearchNode extends StateActionValueSearchNode implements NoveltySearchNode {

    private double novelty = 0;
    private int noveltySamples = 0;

    public double getNoveltyOf(Move move) {
        if(hasChildForMove(move)) {
            return ((NoveltySearchNode)getChildForMove(move)).getStateNovelty();
        } else {
            return 0;
        }
    }

    @Override
    public double getStateNovelty() {
        return novelty;
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move) {
        super.recordEvaluation(evaluation, move);
        double newNovelty = ((NoveltyEvaluation) evaluation).getNovelty();
        noveltySamples++;
        novelty = novelty + (newNovelty-novelty)/noveltySamples;
        if(Util.isDebug()) {
            System.out.println("recording novelty "+newNovelty+" in node with hash "+ getHash());
            System.out.println("novelty samples: "+noveltySamples+", total samples: "+ getRealChildrenSamples());
            System.out.println("average novelty now: "+novelty);
        }
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move, int weight) {
        super.recordEvaluation(evaluation, move, weight);
        double newNovelty = ((NoveltyEvaluation) evaluation).getNovelty();
        novelty = (novelty*noveltySamples + newNovelty*weight)/(double)(noveltySamples+weight);
        noveltySamples += weight;
        if(Util.isDebug()) {
            System.out.println("recording novelty "+newNovelty+" in node with hash "+ getHash());
            System.out.println("novelty samples: "+noveltySamples+", total samples: "+ getRealChildrenSamples());
            System.out.println("average novelty now: "+novelty);
        }
    }

    @Override
    public void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves) {
        super.reset(hash, builder, numberOfColors, numberOfLegalMoves);
        novelty = 0;
        noveltySamples = 0;
    }

}
