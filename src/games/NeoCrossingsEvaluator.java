package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static ai.OnePieceTypeTwoPlayerBoard.BLACK_PIECE;
import static ai.OnePieceTypeTwoPlayerBoard.WHITE_PIECE;

public class NeoCrossingsEvaluator extends StaticEvaluator {

    //piece-value table from the point of view of black //TODO not optimized at all
    private int[][] locationValues8x8 =
            {
                    {40,40,40,40,40,40,40,40},
                    {50,50,50,50,50,50,50,50},
                    {60,60,60,60,60,60,60,60},
                    {70,70,70,70,70,70,70,70},
                    {80,80,80,80,80,80,80,80},
                    {90,90,90,90,90,90,90,90},
                    {100,100,100,100,100,100,100,100},
                    {200,200,200,200,200,200,200,200},
            };

    //piece-value table from the point of view of black //TODO not optimized at all
    private int[][] locationValues12x14 =
            {
                    {40,40,40,40,40,40,40,40,40,40,40,40,40,40},
                    {50,50,50,50,50,50,50,50,50,50,50,50,50,50},
                    {60,60,60,60,60,60,60,60,60,60,60,60,60,60},
                    {70,70,70,70,70,70,70,70,70,70,70,70,70,70},
                    {80,80,80,80,80,80,80,80,80,80,80,80,80,80},
                    {90,90,90,90,90,90,90,90,90,90,90,90,90,90},
                    {100,100,100,100,100,100,100,100,100,100,100,100,100,100},
                    {110,110,110,110,110,110,110,110,110,110,110,110,110,110},
                    {120,120,120,120,120,120,120,120,120,120,120,120,120,120},
                    {140,140,140,140,140,140,140,140,140,140,140,140,140,140},
                    {170,170,170,170,170,170,170,170,170,170,170,170,170,170},
                    {200,200,200,200,200,200,200,200,200,200,200,200,200,200},
            };

    public int getLocationValue(int x, int y, int player, int boardHeight, int boardWidth) {
        if(boardHeight==8 && boardWidth==8) {
            if (player == BLACK) {
                return locationValues8x8[7 - y][x];
            } else {
                return locationValues8x8[y][x];
            }
        } else {
            if (player == BLACK) {
                return locationValues12x14[11 - y][x];
            } else {
                return locationValues12x14[y][x];
            }
        }
    }

    @Override
    public Evaluation staticEval(Board boardToEvaluate) {
        NeoCrossingsNoTranspositionsBoard board = (NeoCrossingsNoTranspositionsBoard) boardToEvaluate;
        double[] evalForColor = new double[2];
        int boardHeight = board.getHeight();
        int boardWidth = board.getWidth();
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                if (board.getSquare(x, y) == WHITE_PIECE) {
                    evalForColor[WHITE] += getLocationValue(x, y, WHITE, boardHeight, boardWidth);
                } else if (board.getSquare(x, y) == BLACK_PIECE) {
                    evalForColor[BLACK] += getLocationValue(x, y, BLACK, boardHeight, boardWidth);
                }
            }
        }
        //normalizing so all evals sum up to 1
        double sumOfEvals = evalForColor[WHITE]+evalForColor[BLACK];
        for(int color=0; color<2; color++) {
            evalForColor[color] /= sumOfEvals;
        }
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        String result = "NeoCrossingsEvaluator (static)";
        return result;
    }

}
