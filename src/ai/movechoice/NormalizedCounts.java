package ai.movechoice;

import ai.Move;
import ai.nodes.SearchNode;
import utils.FitnessEvaluator;
import utils.RouletteWheelSelector;

import java.util.List;
import java.util.Random;

public class NormalizedCounts extends FinalMoveChooser implements FitnessEvaluator<Move> {

    private SearchNode node;
    private FinalMoveChooser greedyChooser = new MaxSamples();
    private double temperature = 1;

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public double fitness(Move move) {
        return node.getValueEstimateOf(move).getSamples();
    }

    @Override
    public Move selectMoveWithExploration(SearchNode node, Random random) {
        this.node = node;
        List<Move> moves =  node.getExpandedMoves();
        return RouletteWheelSelector.selectItem(moves, this, temperature, random);
    }

    @Override
    public String toString() {
        return "NormalizedCounts";
    }

    @Override
    public Move selectGreedyMove(SearchNode node, Random random) {
        return greedyChooser.selectMoveWithExploration(node, random);
    }

}
