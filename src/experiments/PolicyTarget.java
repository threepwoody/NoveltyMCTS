package experiments;

import ai.Move;

import java.util.HashMap;
import java.util.Map;

public class PolicyTarget {

    private Map<Move, Double> targetValuesOfMoves;

    public PolicyTarget() {
        targetValuesOfMoves = new HashMap<>();
    }

    public PolicyTarget(int capacity) {
        targetValuesOfMoves = new HashMap<>((int)Math.ceil(capacity*1.5));
    }

    public Map<Move, Double> getTargetValuesOfMoves() {
        return targetValuesOfMoves;
    }

    public void setTargetValueOfMove(Move move, double value) {
        targetValuesOfMoves.put(move, value);
    }

}
