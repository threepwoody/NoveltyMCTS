package games;

import ai.Board;
import ai.Move;
import ai.MoveSorter;

import static ai.Game.WHITE;

public class BreakthroughMoveSorter implements MoveSorter {

    private BreakthroughEvaluator evaluator = new BreakthroughEvaluator();

    @Override
    public int compareMoves(Move move1, Move move2, Board board) {
        BreakthroughMove m1 = (BreakthroughMove) move1;
        BreakthroughMove m2 = (BreakthroughMove) move2;
        int color = m1.getColorOfMove();
        int boardSize = board.getHeight();
        int winningRow = color==WHITE ? boardSize - 1 : 0;
        if(m1.getToY()==winningRow && !(m2.getToY()==winningRow)) {
            return 1;
        } else if(m2.getToY()==winningRow && !(m1.getToY()==winningRow)) {
            return -1;
        } else if(m1.getToY()==winningRow && m2.getToY()==winningRow) {
            return 0;
        }
        if(m1.isCapture() && !m2.isCapture()) {
            return 1;
        } else if(m2.isCapture() && !m1.isCapture()) {
            return -1;
        }
        int progressMadeByMove1 = evaluator.getLocationValue(m1.getToX(),m1.getToY(),color,boardSize)-evaluator.getLocationValue(m1.getFromX(),m1.getFromY(),color,boardSize);
        int progressMadeByMove2 = evaluator.getLocationValue(m2.getToX(),m2.getToY(),color,boardSize)-evaluator.getLocationValue(m2.getFromX(),m2.getFromY(),color,boardSize);
        return Integer.compare(progressMadeByMove1, progressMadeByMove2);
    }

}
