package ai.noveltymcts;

import ai.Evaluation;

import static utils.Util.round;

public class BasicNoveltyEvaluation extends Evaluation implements NoveltyEvaluation{

    private double novelty;

    public BasicNoveltyEvaluation(double[] evalForColor, double novelty) {
        super(evalForColor);
        this.novelty = novelty;
    }

    public double getNovelty() { return novelty; }

    public String toString() {
        String result = "[";
        for(int i = 0; i< getValueForColor().length-1; i++) {
            result += "color "+i+": "+round(getValueForColor(i),5)+", ";
        }
        result += "color "+(getValueForColor().length-1)+": "+round(getValueForColor(getValueForColor().length-1),5);
        result += ", novelty: "+novelty;
        result += "]";
        return result;
    }

}
