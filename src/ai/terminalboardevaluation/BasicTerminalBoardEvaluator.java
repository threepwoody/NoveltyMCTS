package ai.terminalboardevaluation;

import ai.Board;
import ai.Evaluation;
import ai.TerminalBoardEvaluator;

import java.util.Set;

public class BasicTerminalBoardEvaluator implements TerminalBoardEvaluator {

    private int numberOfColors;

    public BasicTerminalBoardEvaluator(int numberOfColors) {
        this.numberOfColors = numberOfColors;
    }

    @Override
    public TerminalBoardEvaluator copy() {
        return new BasicTerminalBoardEvaluator(numberOfColors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (o == null || getClass() != o.getClass()) return false;
        return numberOfColors==((BasicTerminalBoardEvaluator)o).numberOfColors;
    }

    @Override
    public Evaluation evalOfTerminalBoard(Board terminalBoard) {
        double[] evalForColors = terminalBoard.getTerminalValues();
        return new Evaluation(evalForColors);
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + numberOfColors;
        return result;
    }

}
