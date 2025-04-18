package ai;

import utils.UnknownPropertyException;

public interface Game {

    int BLACK = 1;
    int NO_COLOR = -1;
    int WHITE = 0;

    static int oppositeColor(int color) {
        return color==WHITE ? BLACK : WHITE;
    }

    void addProperty(String property);

    int getMaxTurnsPerMatch();

    String getName();

    int getNumberOfColors();

    default int getNumberOfDifferentBoardElements() {
        return getNumberOfColors();
    }

    default int getStartingColor() {
        return WHITE;
    }

    Board newBoard();

    Board newBoard(String board, boolean isFileName);

    TerminalBoardEvaluator newTerminalBoardEvaluator();

    void setProperty(String property, String value) throws UnknownPropertyException;

    void setTerminalBoardEvaluator(TerminalBoardEvaluator evaluator);

}