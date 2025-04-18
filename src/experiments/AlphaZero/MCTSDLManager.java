package experiments.AlphaZero;

import ai.Evaluation;
import ai.MCTS;
import ai.movechoice.FinalMoveChooser;
import ai.movechoice.MaxSamples;

import java.util.Arrays;
import java.util.List;

public class MCTSDLManager {

    private boolean lastSearchWasLoggable;
    private double loggingProbability = 1.0;
    private boolean longSearchDirichletNoise;
    private FinalMoveChooser longSearchFinalMoveChooser;
    private int longSearchSimulations;
    private boolean maskingIllegalMoves = false;
    private double priorTemperature = 1;
    private double shortSearchProbability = 0;
    private int shortSearchSimulations;
    private boolean usingDirichletNoise = false;
    private boolean usingTemperatureForPriors = false;

    public void afterSearch(MCTS mcts) {
        restoreLongSearchSettings(mcts);
    }

    public void beforeSearch(MCTS mcts) {
        Evaluation evaluation = mcts.getBoardEvaluator().evaluate(mcts.getGame(), mcts.getSimulationBoard(), mcts.getRoot(), mcts.getSimulationLog(), mcts.getRandom());
        if(mcts.getRoot() instanceof DeepLearningSearchNode) {
            List<Double[]> movePriors = ((MovePriorEvaluation)evaluation).getMovePriors();
            if(usingTemperatureForPriors) {
                movePriors = temperedSoftmax(movePriors, priorTemperature);
            }
            ((DeepLearningSearchNode)mcts.getRoot()).recordMovePriors(movePriors);
        }
        if(usingDirichletNoise) {
            ((DeepLearningSearchNode)mcts.getRoot()).initializeAsRoot(mcts.getRandom());
        }
        storeLongSearchSettings(mcts);
        if(shortSearchProbability>0 && mcts.getRandom().nextDouble()<shortSearchProbability) {
            setShortSearchSettings(mcts);
            lastSearchWasLoggable = false;
        } else {
            if(mcts.getRandom().nextDouble()<loggingProbability) {
                lastSearchWasLoggable = true;
            } else {
                lastSearchWasLoggable = false;
            }
        }
    }

    public void handleEvaluation(MovePriorEvaluation evaluation, MCTS mcts) {
        List<Double[]> movePriors = evaluation.getMovePriors();
        if(usingTemperatureForPriors) {
            movePriors = temperedSoftmax(movePriors, priorTemperature);
        }
        if(maskingIllegalMoves) {
            System.out.println("Masking illegal moves currently not implemented");
        } else {
            ((DeepLearningSearchNode) mcts.getCurrentNode()).recordMovePriors(movePriors);
        }
    }

    public boolean lastSearchWasLoggable() {
        return lastSearchWasLoggable;
    }

    private void restoreLongSearchSettings(MCTS mcts) {
        mcts.getSearchTimer().setSearchBudget(longSearchSimulations);
        usingDirichletNoise = longSearchDirichletNoise;
        mcts.setFinalMoveChooser(longSearchFinalMoveChooser);
    }

    public void setLoggingProbability(double loggingProbability) {
        this.loggingProbability = loggingProbability;
    }

    public void setMaskingIllegalMoves(boolean maskingIllegalMoves) {
        this.maskingIllegalMoves = maskingIllegalMoves;
    }

    public void setPriorTemperature(double priorTemperature) {
        this.priorTemperature = priorTemperature;
    }

    public void setShortSearchProbability(double shortSearchProbability) {
        this.shortSearchProbability = shortSearchProbability;
    }

    private void setShortSearchSettings(MCTS mcts) {
        mcts.getSearchTimer().setSearchBudget(shortSearchSimulations);
        usingDirichletNoise = false;
        mcts.setFinalMoveChooser(new MaxSamples());
    }

    public void setShortSearchSimulations(int shortSearchSimulations) {
        this.shortSearchSimulations = shortSearchSimulations;
    }

    public void setUsingDirichletNoise(boolean usingDirichletNoise) {
        this.usingDirichletNoise = usingDirichletNoise;
    }

    public void setUsingTemperatureForPriors(boolean usingTemperatureForPriors) {
        this.usingTemperatureForPriors = usingTemperatureForPriors;
    }

    private void storeLongSearchSettings(MCTS mcts) {
        longSearchSimulations = mcts.getSearchTimer().getSearchBudget();
        longSearchDirichletNoise = usingDirichletNoise;
        longSearchFinalMoveChooser = mcts.getFinalMoveChooser();
    }

    public List<Double[]> temperedSoftmax(List<Double[]> input, double temperature) {
        for(int dim=0; dim<input.size(); dim++) {
            Double[] x = input.get(dim);
            double[] value = Arrays.stream(x).mapToDouble(Double::doubleValue).map(y -> Math.exp(y / temperature)).toArray();
            double total = Arrays.stream(value).sum();
            input.set(dim, (Double[]) Arrays.stream(value).map(p -> p / total).mapToObj(Double::valueOf).toArray());
        }
        return input;
    }

}
