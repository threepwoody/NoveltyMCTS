package ai.nodes;

import java.util.HashMap;
import java.util.Map;

public abstract class SearchNodeBuilder {

    //default value for two-player games. is overwritten anyway by setGame()
    private double defaultInitialResult = 0.5;
    private double defaultInitialSquaredResult = 0.25;
    private Map<Integer, Double> initialResultForColor = new HashMap<>();
    private Map<Integer, Double> initialSquaredResultForColor = new HashMap<>();
    private int initialSamples = 1;

    public abstract SearchNode buildSearchNode();

    public double getInitialResultForColor(int color) {
        if(initialResultForColor.containsKey(color)) {
            return initialResultForColor.get(color);
        } else {
            return defaultInitialResult;
        }
    }

    public double getInitialSquaredResultForColor(int color) {
        if(initialSquaredResultForColor.containsKey(color)) {
            return initialSquaredResultForColor.get(color);
        } else {
            return defaultInitialSquaredResult;
        }
    }

    public int getInitialSamples() {
        return initialSamples;
    }

    public void setInitialSamples(int initialSamples) {
        this.initialSamples = initialSamples;
    }

    public void setDefaultInitialResult(double defaultInitialResult) {
        this.defaultInitialResult = defaultInitialResult;
    }

    public void setDefaultInitialSquaredResult(double defaultInitialSquaredResult) {
        this.defaultInitialSquaredResult = defaultInitialSquaredResult;
    }

    public void setInitialResultForColor(int color, double result) {
        initialResultForColor.put(color, result);
    }

    public void setInitialSquaredResultForColor(int color, double result) {
        initialSquaredResultForColor.put(color, result);
    }

}
