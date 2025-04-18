package ai.selection;

import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

public class UCB1 extends UCB1Tuned {

    @Override
    public double upperConfidenceBound(SearchNode node, ValueEstimate valueEstimateOfMove, int numberOfLegalMoves, int colorOfMove) {
        int sumOfRunsOfChildren = node.getTotalChildrenSamples();
        double logParentRunCount = log(sumOfRunsOfChildren);
        return getExplorationParameter() *sqrt(logParentRunCount/valueEstimateOfMove.getSamples());
    }

    @Override
    public String toString() {
        return "UCB1 with exploration factor="+getExplorationParameter();
    }

}
