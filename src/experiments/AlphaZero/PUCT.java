package experiments.AlphaZero;

import ai.Board;
import ai.Move;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;
import ai.selection.ExplorationExploitationPolicy;
import ai.selection.ValueMaximizingSelectionPolicy;

public class PUCT extends ValueMaximizingSelectionPolicy implements ExplorationExploitationPolicy {

    private double explorationFactor = 1;
    private AlphaZeroGame game;

    public PUCT(AlphaZeroGame game) {
        this.game = game;
    }

    @Override
    public double getExplorationParameter() {
        return explorationFactor;
    }

    @Override
    public void setExplorationParameter(double exploration) {
        this.explorationFactor = exploration;
    }

    private double networkPrior(SearchNode node, Move move) {
        return ((DeepLearningSearchNode)node).getMovePrior(game.moveIndex(1, move));
    }

    @Override
    public double searchValue(SearchNode node, Move move, int numberOfLegalMoves, Board simulationBoard, SimulationLog log) {
        ValueEstimate valueEstimateOfMove = node.getValueEstimateOf(move);
        return valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove()) + upperConfidenceBound(node, move, valueEstimateOfMove);
    }

    @Override
    public String toString() {
        return "PUCT";
    }

    protected double upperConfidenceBound(SearchNode node, Move move, ValueEstimate valueEstimateOfMove) {
        int runsOfAllMoves = node.getTotalChildrenSamples();
        int runsOfMove = valueEstimateOfMove.getSamples();
        return explorationFactor*networkPrior(node, move)*(Math.sqrt(runsOfAllMoves)/(1+runsOfMove));
    }

}
