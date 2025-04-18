package experiments.AlphaZero;

import ai.Evaluation;

import java.util.List;

import static utils.Util.round;

public class BasicMovePriorEvaluation extends Evaluation implements MovePriorEvaluation {

    private List<Double[]> movePriors;

    public BasicMovePriorEvaluation(double[] evalForColor, List<Double[]> movePriors) {
        super(evalForColor);
        this.movePriors = movePriors;
    }

    public List<Double[]> getMovePriors() { return movePriors; }

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
        result += "]";
        return result;
    }

}
