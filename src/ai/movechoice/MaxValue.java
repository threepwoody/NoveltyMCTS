package ai.movechoice;

import ai.nodes.ValueEstimate;

public class MaxValue extends MaxSamples {

    @Override
    public double primaryValueEstimateScore(ValueEstimate valueEstimate, int colorOfMove) {
        return valueEstimate.getAverageResultForColor(colorOfMove);
    }

    @Override
    public double secondaryValueEstimateScore(ValueEstimate valueEstimate, int colorOfMove) {
        return valueEstimate.getSamples();
    }

    @Override
    public String toString() {
        return "MaxValue";
    }

}
