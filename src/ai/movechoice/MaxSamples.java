package ai.movechoice;

import ai.Move;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;

import java.util.*;

public class MaxSamples extends FinalMoveChooser {

    public Move selectMoveWithExploration(SearchNode node, Random random) {
        List<Move> moves =  node.getExpandedMoves();
        if(moves.size()==0) return null;
        Collections.shuffle(moves, random);
        moves.sort((m1, m2) -> compareValueEstimates(node.getValueEstimateOf(m2),m2.getColorOfMove(), node.getValueEstimateOf(m1),m1.getColorOfMove()));
        return moves.get(0);
    }

    protected int compareValueEstimates(ValueEstimate valueEstimate1, int colorOfMove1, ValueEstimate valueEstimate2, int colorOfMove2) {
        double primaryValueEstimateScore1 = primaryValueEstimateScore(valueEstimate1, colorOfMove1);
        double primaryValueEstimateScore2 = primaryValueEstimateScore(valueEstimate2, colorOfMove2);
        if(primaryValueEstimateScore1==primaryValueEstimateScore2) {
            return Double.compare(secondaryValueEstimateScore(valueEstimate1, colorOfMove1), secondaryValueEstimateScore(valueEstimate2, colorOfMove2));
        } else {
            return Double.compare(primaryValueEstimateScore1, primaryValueEstimateScore2);
        }
    }

    public double primaryValueEstimateScore(ValueEstimate valueEstimate, int colorOfMove) {
        return valueEstimate.getSamples();
    }

    public double secondaryValueEstimateScore(ValueEstimate valueEstimate, int colorOfMove) {
        return valueEstimate.getAverageResultForColor(colorOfMove);
    }

    @Override
    public String toString() {
        return "MaxSamples";
    }

}
