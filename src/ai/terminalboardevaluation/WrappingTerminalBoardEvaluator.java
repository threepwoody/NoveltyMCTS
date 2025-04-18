package ai.terminalboardevaluation;

import ai.Board;
import ai.Evaluation;
import ai.TerminalBoardEvaluator;
import ai.evaluation.StaticEvaluator;

import java.util.HashSet;
import java.util.Set;

//uses a static evaluator to shape the rewards *exclusively* in final positions that would otherwise be uninformative, i.e. draws. TODO many evaluators still assume that positions are non-terminal, and will not work here
public class WrappingTerminalBoardEvaluator extends BasicTerminalBoardEvaluator {

    private double wrappedEvalExponent = 1; //determines how important the static evaluation is in case of drawn positions without winners.
    private StaticEvaluator wrappedEvaluator;

    public WrappingTerminalBoardEvaluator(int numberOfColors, StaticEvaluator wrappedEvaluator) {
        super(numberOfColors);
        this.wrappedEvaluator = wrappedEvaluator;
    }

    @Override
    public TerminalBoardEvaluator copy() {
        WrappingTerminalBoardEvaluator copy = new WrappingTerminalBoardEvaluator(getNumberOfColors(), wrappedEvaluator);
        copy.setWrappedEvalExponent(wrappedEvalExponent);
        return copy;
    }

    @Override
    public Evaluation evalOfTerminalBoard(Board terminalBoard) { //TODO debug
        int numberOfColors = getNumberOfColors();
        double[] evalForColor = terminalBoard.getTerminalValues();
        boolean allEvaluationsEqual = true;
        for(int color = 0; color<numberOfColors; color++) {
            if(evalForColor[color] != 1.0/numberOfColors) {
                allEvaluationsEqual = false;
            }
        }
        if(!allEvaluationsEqual) {
            return new Evaluation(evalForColor);
        }
        Evaluation staticEvaluation = wrappedEvaluator.staticEval(terminalBoard);
        double sumOfEvals = 0;
        for(int color = 0; color<numberOfColors; color++) {
            evalForColor[color] = Math.pow(staticEvaluation.getValueForColor(color), wrappedEvalExponent);
            sumOfEvals += evalForColor[color];
        }
        for(int color=0; color<evalForColor.length; color++) {
            evalForColor[color] /= sumOfEvals;
        }
        return new Evaluation(evalForColor);
    }

    public void setWrappedEvalExponent(double wrappedEvalExponent) {
        this.wrappedEvalExponent = wrappedEvalExponent;
    }

}
