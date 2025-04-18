package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;

import static ai.Game.BLACK;
import static ai.Game.WHITE;

public class OthelloEvaluator extends StaticEvaluator {

    private boolean countsMobility = true;
    private double stableDiscValue = 2;

    private double getMaxValue(OthelloNoTranspositionsBoard board) {
        return (board.getHeight()*board.getWidth()-1)*stableDiscValue + (board.getHeight()*board.getWidth()); //trivial upper bound: all squares but one are mine and mobility is all squares
    }

    public void setCountsMobility(boolean countsMobility) {
        this.countsMobility = countsMobility;
    }

    public void setStableDiscValue(double stableDiscValue) {
        this.stableDiscValue = stableDiscValue;
    }

    @Override
    public Evaluation staticEval(Board boardToEvaluate) {
        OthelloNoTranspositionsBoard board = (OthelloNoTranspositionsBoard) boardToEvaluate;
        double evaluation = 0;
        double[] evalForColor = new double[2];
        evaluation += (board.stableDiscs(BLACK) - board.stableDiscs(WHITE)) * stableDiscValue;
        if(countsMobility) {
            evaluation += (board.getLegalMovesFor(BLACK).size() - board.getLegalMovesFor(WHITE).size());
        }
        evaluation = (evaluation + getMaxValue(board)) / (2 * getMaxValue(board));
        evalForColor[BLACK] = evaluation;
        evalForColor[WHITE] = 1 - evaluation;
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        String result = "OthelloEvaluator (static) with counts mobility="+countsMobility+", stable disc value="+stableDiscValue;
        return result;
    }

}
