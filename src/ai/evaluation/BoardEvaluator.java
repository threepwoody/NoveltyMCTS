package ai.evaluation;

import ai.Board;
import ai.Evaluation;
import ai.Game;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import utils.Util;

import java.util.Random;

public abstract class BoardEvaluator {

    private double softmaxTemperature = 50;
    private boolean usingSoftmaxEval = false;

    public void endGame() {};

    public abstract Evaluation eval(Game game, Board board, SearchNode currentNode, SimulationLog log, Random random);

    //currentNode and log are optional, random is optional if the Evaluator at hand doesn't need it
    public Evaluation evaluate(Game game, Board board, SearchNode currentNode, SimulationLog log, Random random) {
        Evaluation evaluation = eval(game, board, currentNode, log, random);
        if(usingSoftmaxEval) {
            double[] valueForColor = evaluation.getValueForColor();
            valueForColor = Util.softMax(valueForColor, softmaxTemperature);
            evaluation.setValueForColor(valueForColor);
        }
        return evaluation;
    }

    public void reset(Game game) {
    }

    public void setSoftmaxTemperature(double softmaxTemperature) {
        this.softmaxTemperature = softmaxTemperature;
    }

    public void setUsingSoftmaxEval(boolean usingSoftmaxEval) {
        this.usingSoftmaxEval = usingSoftmaxEval;
    }

}
