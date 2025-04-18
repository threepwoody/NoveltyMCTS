package experiments.AlphaZero;

import ai.Evaluation;
import ai.Move;
import ai.nodes.SearchNode;
import ai.nodes.SearchNodeBuilder;
import ai.nodes.StateActionValueSearchNode;
import ai.nodes.ValueEstimate;
import jsat.distributions.multivariate.Dirichlet;
import jsat.linear.ConstantVector;
import jsat.linear.Vec;
import utils.Util;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static utils.Util.round;

public class DeepLearningSearchNode implements SearchNode {

    private static Dirichlet dirichlet = new Dirichlet(new ConstantVector(1,1));
    private static double[] dirichletAlpha = new double[]{0.3,0.3}; //e.g. 10/b where b is average branching factor
    private List<Double[]> movePriorsPerDimension = null;
    private StateActionValueSearchNode node;

    public DeepLearningSearchNode() {
        node = new StateActionValueSearchNode();
    }

    @Override
    public void addChildForMove(SearchNode child, Move move) {
        node.addChildForMove(child, move);
    }

    @Override
    public SearchNode getChildForMove(Move move) {
        return node.getChildForMove(move);
    }

    @Override
    public Collection<SearchNode> getChildNodes() {
        return node.getChildNodes();
    }

    @Override
    public int getColorToPlay() {
        return node.getColorToPlay();
    }

    @Override
    public void setColorToPlay(int colorToPlay) {
        node.setColorToPlay(colorToPlay);
    }

    @Override
    public List<Move> getExpandedMoves() {
        return node.getExpandedMoves();
    }

    @Override
    public long getHash() {
        return node.getHash();
    }

    public double getMovePrior(int[] moveIndex) {
        double result = 1;
        for(int dim=0;dim<moveIndex.length;dim++) {
            result *= movePriorsPerDimension.get(dim)[moveIndex[dim]];
        }
        return result;
    }

    @Override
    public SearchNode getNext() {
        return node.getNext();
    }

    @Override
    public void setNext(SearchNode next) {
        node.setNext(next);
    }

    public StateActionValueSearchNode getNode() {
        return node;
    }

    public void setNode(StateActionValueSearchNode node) {
        this.node = node;
    }

    @Override
    public int getNumberOfLegalMoves() {
        return node.getNumberOfLegalMoves();
    }

    @Override
    public int getRealChildrenSamples() {
        return node.getRealChildrenSamples();
    }

    @Override
    public void setRealChildrenSamples(int visits) {
        node.setRealChildrenSamples(visits);
    }

    @Override
    public int getTotalChildrenSamples() {
        return node.getTotalChildrenSamples();
    }

    @Override
    public ValueEstimate getValueEstimateOf(Move move) {
        return node.getValueEstimateOf(move);
    }

    @Override
    public boolean hasChildForMove(Move move) {
        return node.hasChildForMove(move);
    }

    public void initializeAsRoot(Random random) {
        for(int dim=0; dim<movePriorsPerDimension.size(); dim++) {
            Double[] result = new Double[movePriorsPerDimension.get(dim).length];
            Vec alphas = new ConstantVector(dirichletAlpha[dim], result.length);
            dirichlet.setAlphas(alphas);
            Vec dirichletSample = dirichlet.sample(1, random).get(0);
            for (int i = 0; i < result.length; i++) {
                result[i] = 0.75 * movePriorsPerDimension.get(dim)[i] + 0.25 * dirichletSample.get(i);
            }
            movePriorsPerDimension.set(dim, result);
        }
    }

    @Override
    public boolean isFresh() {
        return node.isFresh();
    }

    @Override
    public boolean isMarked() {
        return node.isMarked();
    }

    @Override
    public void setMarked(boolean marked) {
        node.setMarked(marked);
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move) {
        node.recordEvaluation(evaluation, move);
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move, int weight) {
        node.recordEvaluation(evaluation, move, weight);
    }

    public void recordMovePriors(List<Double[]> priors) {
        if(Util.isDebug()) {
            System.out.println("recording node priors in node "+getHash());
            for(int dim=0; dim<priors.size(); dim++) {
                System.out.print("(");
                Double[] movePriorsForDim = priors.get(dim);
                for (int i = 0; i < movePriorsForDim.length - 1; i++) {
                    System.out.print(round(movePriorsForDim[i], 3) + " ");
                }
                System.out.print(round(movePriorsForDim[movePriorsForDim.length - 1], 3));
                System.out.print(")");
            }
            System.out.println();
        }
        movePriorsPerDimension = priors;
    }

    @Override
    public void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves) {
        node.reset(hash, builder, numberOfColors, numberOfLegalMoves);
        movePriorsPerDimension = null;
    }

}
