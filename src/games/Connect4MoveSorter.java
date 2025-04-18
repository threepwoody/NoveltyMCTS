package games;

import ai.Board;
import ai.Move;
import ai.MoveSorter;

import static ai.Game.BLACK;

public class Connect4MoveSorter implements MoveSorter {

    private Connect4BetterEvaluator evaluator = new Connect4BetterEvaluator();

    @Override
    public int compareMoves(Move move1, Move move2, Board board) {
        Connect4Move m1 = (Connect4Move) move1;
        Connect4Move m2 = (Connect4Move) move2;
        Connect4NoTranspositionsBoard c4Board = (Connect4NoTranspositionsBoard) board;
        int color = m1.getColorOfMove();
        c4Board.play(m1);
        double eval1 = evaluator.linearCombinationEvalForWhite(c4Board);
        if(color==BLACK) {
            eval1 = 1-eval1;
        }
        c4Board.undo(m1);
        c4Board.play(m2);
        double eval2 = evaluator.linearCombinationEvalForWhite(c4Board);
        if(color==BLACK) {
            eval2 = 1-eval2;
        }
        c4Board.undo(m2);
        return Double.compare(eval1, eval2);
    }

}
