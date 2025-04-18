package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;

import static ai.BasicBoard.NO_PIECE;
import static ai.Game.BLACK;

public class DomineeringEvaluator extends StaticEvaluator {

    private double weightForRealMoves = 1;
    private double weightForSafeMoves = 2;

    private int realMoves(DomineeringNoTranspositionsBoard board, int color) {
        int realMoves = 0;
        int[][] rawboard = board.getBoard();
        if(color==BLACK) {
            for (int x = 0; x < rawboard.length; x++) {
                for (int y = 0; y < rawboard[0].length-1; y++) {
                    if (rawboard[x][y] == NO_PIECE && rawboard[x][y + 1] == NO_PIECE) {
                        realMoves++;
                        y++;
                    }
                }
            }
        } else {
            for (int y = 0; y < rawboard[0].length; y++) {
                for (int x = 0; x < rawboard.length - 1; x++) {
                    if (rawboard[x][y] == NO_PIECE && rawboard[x + 1][y] == NO_PIECE) {
                        realMoves++;
                        x++;
                    }
                }
            }
        }
        return realMoves;
    }

    private int safeMoves(DomineeringNoTranspositionsBoard board, int color) {
        int safeMoves = 0;
        int[][] rawboard = board.getBoard();
        if(color==BLACK) {
            for (int x = 0; x < rawboard.length; x++) {
                for (int y = 0; y < rawboard[0].length-1; y++) {
                    if (rawboard[x][y] == NO_PIECE && rawboard[x][y+1] == NO_PIECE
                    && board.getSquare(x-1,y)!=NO_PIECE && board.getSquare(x-1,y+1)!=NO_PIECE
                    && board.getSquare(x+1,y)!=NO_PIECE && board.getSquare(x+1,y+1)!=NO_PIECE ) {
                        safeMoves++;
                        y++;
                    }
                }
            }
        } else {
            for (int y = 0; y < rawboard[0].length; y++) {
                for (int x = 0; x < rawboard.length - 1; x++) {
                    if (rawboard[x][y] == NO_PIECE && rawboard[x+1][y] == NO_PIECE
                        && board.getSquare(x,y-1)!=NO_PIECE && board.getSquare(x,y+1)!=NO_PIECE
                        && board.getSquare(x+1,y-1)!=NO_PIECE && board.getSquare(x+1,y+1)!=NO_PIECE ) {
                        safeMoves++;
                        x++;
                    }
                }
            }
        }
        return safeMoves;
    }

    public void setWeightForSafeMoves(double weightForSafeMoves) {
        this.weightForSafeMoves = weightForSafeMoves;
    }

    @Override
    public Evaluation staticEval(Board boardToEvaluate) {
        DomineeringNoTranspositionsBoard board = (DomineeringNoTranspositionsBoard) boardToEvaluate;
        double[] evalForColor = new double[2];
        for(int color=0; color<2; color++) {
            evalForColor[color] += weightForRealMoves*realMoves(board, color);
        }
        //normalizing so all evals sum up to 1
        double sumOfEvals = evalForColor[0]+evalForColor[1];
        for(int color=0; color<2; color++) {
            evalForColor[color] /= sumOfEvals;
        }
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        return "DomineeringEvaluator (static) with real move weight="+weightForRealMoves;
    }

}