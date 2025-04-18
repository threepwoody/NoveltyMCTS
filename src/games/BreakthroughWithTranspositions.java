package games;

import ai.Board;

public class BreakthroughWithTranspositions extends Breakthrough {

    public BreakthroughWithTranspositions(int boardSize) {
        super(boardSize);
    }

    public BreakthroughWithTranspositions() {
        super();
    }

    @Override
    public String getNameWithoutProperties() {
        return "BreakthroughWithTranspositions-"+getBoardHeight();
    }

    @Override
    public Board newBoard(String board, boolean isFileName) {
        return new BreakthroughWithTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new BreakthroughWithTranspositionsBoard(this);
    }

    @Override
    public String toString() {
        return "BreakthroughWithTranspositions (board size " + getBoardWidth() + "x" + getBoardHeight() + ")";
    }

}
