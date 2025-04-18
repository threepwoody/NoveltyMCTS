package ai.selection;

import ai.Board;
import ai.Move;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;

import static java.lang.Math.*;

//expects MCTS to run with UCTSearchNodes
public class UCB1Tuned extends ValueMaximizingSelectionPolicy implements ExplorationExploitationPolicy {

    private double explorationFactor = 1;

    /**
     * Return the UCT upper bound for node. The UCB1-TUNED policy, explained in
     * the tech report by Gelly, et al, "Modification of UCT with Patterns in
     * Monte-Carlo Go". The formula is at the bottom of p. 5 in that paper.
     */
    public double upperConfidenceBound(SearchNode node, ValueEstimate valueEstimateOfMove, int numberOfLegalMoves, int colorOfMove) {
        int sumOfRunsOfChildren = node.getTotalChildrenSamples();
        double logParentRunCount = log(sumOfRunsOfChildren);
        double term1 = valueEstimateOfMove.getAverageSquaredResultForColor(colorOfMove);
        double term2 = -(valueEstimateOfMove.getAverageResultForColor(colorOfMove)*valueEstimateOfMove.getAverageResultForColor(colorOfMove));
        double term3 = sqrt(2 * logParentRunCount / valueEstimateOfMove.getSamples());
        double v = term1 + term2 + term3;
        double factor1 = logParentRunCount / valueEstimateOfMove.getSamples();
        double factor2 = min(0.25, v);
        double exploration = explorationFactor;
        return exploration*sqrt(factor1 * factor2);
    }

    @Override
    public double getExplorationParameter() {
        return explorationFactor;
    }

    @Override
    public void setExplorationParameter(double exploration) {
        this.explorationFactor = exploration;
    }

    @Override
    public double searchValue(SearchNode node, Move move, int numberOfLegalMoves, Board board, SimulationLog log) {
        ValueEstimate valueEstimateOfMove = node.getValueEstimateOf(move);
        if(getExplorationParameter()==0) {
            return valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove());
        } else {
            return valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove()) + upperConfidenceBound(node, valueEstimateOfMove, numberOfLegalMoves, move.getColorOfMove());
        }
    }

    @Override
    public String toString() {
        return "UCB1Tuned with exploration factor="+explorationFactor;
    }

}
