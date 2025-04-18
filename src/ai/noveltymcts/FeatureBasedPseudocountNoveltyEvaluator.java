package ai.noveltymcts;

import ai.Evaluation;
import ai.Board;
import ai.evaluation.StaticEvaluator;
import utils.UnknownPropertyException;
import utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.Game.BLACK;
import static ai.Game.WHITE;

public abstract class FeatureBasedPseudocountNoveltyEvaluator extends StaticEvaluator implements NoveltyFunction  {

    private static double initialCount = 0.5;
    private static int initialSamples = 1;
    private double beta = 0.05;
    private Map<Long, Double> featureCounts;
    private List<Long> featureKeysOfEvaluatedBoard;
    private int samples;

    public FeatureBasedPseudocountNoveltyEvaluator() {
        featureCounts = new HashMap<>();
        samples = initialSamples;
        featureKeysOfEvaluatedBoard = new ArrayList<>();
    }

    private void addObservationOfEvaluatedBoard() {
        samples++;
        for(long featureKey : featureKeysOfEvaluatedBoard) {
            if(!featureCounts.containsKey(featureKey)) {
                featureCounts.put(featureKey, getInitialCount());
            }
            featureCounts.put(featureKey, featureCounts.get(featureKey)+1);
        }
    }

    @Override
    public Evaluation staticEval(Board board) {
        double evaluation = evaluateAndCountFeatures(board);
        double[] evalForColor = new double[2];
        evalForColor[WHITE] = evaluation;
        evalForColor[BLACK] = 1-evaluation;
        double novelty = noveltyOfEvaluatedBoard();
        return new BasicNoveltyEvaluation(evalForColor, novelty);
    }

    @Override
    public void clearObservations() {
        featureCounts = new HashMap<>();
        samples = initialSamples;
    }

    public abstract double evaluateAndCountFeatures(Board board);

    public double getBeta() {
        return beta;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("noveltybeta")) {
            this.beta = Double.parseDouble(value);
        } else {
            throw new UnknownPropertyException(property+" is not a known property for FeatureBasedPseudocountNoveltyEvaluator.");
        }
    }

    protected List<Long> getFeatureKeysOfEvaluatedBoard() {
        return featureKeysOfEvaluatedBoard;
    }

    public double getInitialCount() {
        return initialCount;
    }

    @Override
    public double novelty(Board board, Evaluation evaluation) {
        //TODO might also try the "naive pseudocount" instead, because it's cheaper
        evaluateAndCountFeatures(board);
        return noveltyOfEvaluatedBoard();
    }

    protected double noveltyOfEvaluatedBoard() {
        //TODO might also try the "naive pseudocount" instead, because it's cheaper
        double rho_t = probabilityOfEvaluatedBoard();
        addObservationOfEvaluatedBoard();
        double rho_t1 = probabilityOfEvaluatedBoard();
        double pseudoCount = (rho_t*(1-rho_t1))/(rho_t1-rho_t);
        double noveltyBonus = beta/Math.sqrt(pseudoCount);
        if(Util.isDebug()) {
            System.out.println("Prior probability of seeing this board: "+rho_t);
            System.out.println("Posterior probability of seeing this board: "+rho_t1);
            System.out.println("Pseudocount of this board: "+pseudoCount);
            System.out.println("Novelty bonus for this board: "+noveltyBonus);
        }
        return noveltyBonus;
    }

    private double probabilityOfEvaluatedBoard() {
        double result = 0;
        for(long featureKey : featureKeysOfEvaluatedBoard) {
            if(!featureCounts.containsKey(featureKey)) {
                featureCounts.put(featureKey, getInitialCount());
            }
            result += Math.log(featureCounts.get(featureKey)/samples);
        }
        return Math.exp(result);
    }

    @Override
    public void setSearchingColor(int color) {
    }

    @Override
    public String toString() {
        return "FeatureBasedPseudocountNoveltyEvaluator (static) with beta: "+beta;
    }

}
