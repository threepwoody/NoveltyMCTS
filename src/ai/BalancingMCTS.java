package ai;

import ai.movechoice.MaxSamples;
import utils.UnknownPropertyException;
import utils.Util;

import static utils.Util.printVector;

public class BalancingMCTS extends BasicMCTS {

    private int ownColor;
    private double goalBalance = 0.5;
    private double maxDistanceFromBalance = 0.5;

    public BalancingMCTS() {
        super();
        setFinalMoveChooser(new MaxSamples());
    }

    @Override
    public void beforeSearch() {
        super.beforeSearch();
        ownColor = getBoard().getColorToPlay();
    }

    private Evaluation perceivedEvaluation(Evaluation evaluation) {
        if(Util.DEBUG) {
            System.out.println("actual eval: " + printVector(evaluation.getValueForColor()));
        }
        double[] perceivedEval = balance(evaluation.getValueForColor());
        if(Util.DEBUG) {
            System.out.println("perceived eval: "+printVector(perceivedEval));
        }
        return new Evaluation(perceivedEval);
    }

    private double[] balance(double[] evaluation) {
        double[] result = new double[evaluation.length];
        for(int color=0; color<getGame().getNumberOfColors(); color++) {
            if(color==ownColor) {
                if(evaluation[color]==0 || evaluation[color]==1) {
                    result[color] = evaluation[color];
                } else {
                    result[color] = 1 - normalizedDistanceFromGoalBalance(evaluation[color]);
                }
            } else {
                result[color] = evaluation[color];
            }
        }
        return result;
    }

    private double normalizedDistanceFromGoalBalance(double v) {
        double distance = Math.abs(goalBalance - v);
        return distance/maxDistanceFromBalance;
    }

    @Override
    public Evaluation evaluate() {
        Evaluation evaluation;
        if (getSimulationBoard().isTerminalBoard()) {
            getSimulationLog().add(getSimulationBoard(), getCurrentNode(), null);
            evaluation = getSimulationBoard().evalOfTerminalBoard();
        } else {
            evaluation = getBoardEvaluator().evaluate(getGame(), getSimulationBoard(), getCurrentNode(), getSimulationLog(), getRandom());
        }
        return perceivedEvaluation(evaluation);
    }


    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if (property.equals("goalbalance")) {
            goalBalance = Double.parseDouble(value);
            maxDistanceFromBalance = Math.max(goalBalance, 1-goalBalance);
        } else {
            super.setProperty(property, value);
        }
    }

}
