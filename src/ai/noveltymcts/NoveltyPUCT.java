package ai.noveltymcts;

import ai.Board;
import ai.Move;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import ai.nodes.ValueEstimate;
import experiments.AlphaZero.AlphaZeroGame;
import experiments.AlphaZero.DeepLearningSearchNode;

public class NoveltyPUCT extends NoveltyUCB1Tuned {

    private AlphaZeroGame game;

    public NoveltyPUCT(AlphaZeroGame game) {
        this.game = game;
    }

    private double networkPrior(SearchNode node, Move move) {
        return ((DeepLearningSearchNode)node).getMovePrior(game.moveIndex(1, move));
    }

    @Override
    public double searchValue(SearchNode node, Move move, int numberOfLegalMoves, Board simulationBoard, SimulationLog log) {
        ValueEstimate valueEstimateOfMove = node.getValueEstimateOf(move);
        double result = valueEstimateOfMove.getAverageResultForColor(move.getColorOfMove());
        double novelty = ((NoveltySearchNode)node).getNoveltyOf(move);
        int samples = valueEstimateOfMove.getSamples();
        double noveltyWeight = weightingParameter(samples);
        result = novelty*noveltyWeight + result*(1-noveltyWeight);
        return result + upperConfidenceBound(node, move, valueEstimateOfMove);
    }

    @Override
    public String toString() {
        return "NoveltyPUCT with exploration factor="+getExplorationParameter()+", novelty weight="+getNoveltyWeight();
    }

    protected double upperConfidenceBound(SearchNode node, Move move, ValueEstimate valueEstimateOfMove) {
        int runsOfAllMoves = node.getTotalChildrenSamples();
        int runsOfMove = valueEstimateOfMove.getSamples();
        return getExplorationParameter()*networkPrior(node, move)*(Math.sqrt(runsOfAllMoves)/(1+runsOfMove));
    }

}
