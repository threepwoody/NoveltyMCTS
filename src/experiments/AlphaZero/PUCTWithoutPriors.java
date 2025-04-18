package experiments.AlphaZero;

import ai.Move;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;

public class PUCTWithoutPriors extends PUCT {

    public PUCTWithoutPriors(AlphaZeroGame game) {
        super(game);
    }

    @Override
    protected double upperConfidenceBound(SearchNode node, Move move, ValueEstimate valueEstimateOfMove) {
        int runsOfAllMoves = node.getTotalChildrenSamples();
        int runsOfMove = valueEstimateOfMove.getSamples();
        return getExplorationParameter()*(Math.sqrt(runsOfAllMoves)/(1+runsOfMove));
    }

}
