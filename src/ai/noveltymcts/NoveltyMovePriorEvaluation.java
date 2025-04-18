package ai.noveltymcts;

import ai.Evaluation;
import experiments.AlphaZero.MovePriorEvaluation;

import java.util.List;

import static utils.Util.round;

public class NoveltyMovePriorEvaluation extends Evaluation implements NoveltyEvaluation, MovePriorEvaluation {

    private List<Double[]> movePriors;
    private double novelty;

    public NoveltyMovePriorEvaluation(double[] evalForColor, List<Double[]> movePriors, double novelty) {
        super(evalForColor);
        this.movePriors = movePriors;
        this.novelty = novelty;
    }

    public List<Double[]> getMovePriors() { return movePriors; }

    public double getNovelty() { return novelty; }

    public String toString() {
        String result = "[";
        for(int i = 0; i< getValueForColor().length-1; i++) {
            result += "color "+i+": "+round(getValueForColor(i),5)+", ";
        }
        result += "color "+(getValueForColor().length-1)+": "+round(getValueForColor(getValueForColor().length-1),3);
        result += ", move priors: ";
        for(int dim=0; dim<movePriors.size(); dim++) {
            result += "(";
            Double[] movePriorsForDim = movePriors.get(dim);
            for (int i = 0; i < movePriorsForDim.length - 1; i++) {
                result += round(movePriorsForDim[i], 3) + " ";
            }
            result += round(movePriorsForDim[movePriorsForDim.length - 1], 3);
            result += ")";
        }
        result += ", novelty: "+novelty;
        result += "]";
        return result;
    }

}
