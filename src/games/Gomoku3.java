package games;

import ai.Board;
import ai.evaluation.StaticEvaluator;

public class Gomoku3 extends Gomoku implements StaticEvaluatorGame {

    public Gomoku3(int boardWidth) {
        super(boardWidth);
    }

    public Gomoku3() {
        super();
    }

    @Override
    public String getNameWithoutProperties() {
        return "Gomoku3-"+ getBoardWidth();
    }

    @Override
    public Board newBoard() {
        return new Gomoku3Board(this);
    }

    @Override
    public Board newBoard(String board, boolean isFileName) {
        return new Gomoku3Board(this, board, isFileName);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new GomokuEvaluator();
    }

    @Override
    public String toString() {
        return "Gomoku3";
    }


}
