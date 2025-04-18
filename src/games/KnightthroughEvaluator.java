package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static games.KnightthroughNoTranspositionsBoard.BLACK_PIECE;
import static games.KnightthroughNoTranspositionsBoard.WHITE_PIECE;

//currently only works for 8x8 boards
public class KnightthroughEvaluator extends StaticEvaluator {

    //piece-value table from the point of view of black
    private int[][] locationValues8x8 =
            {
                    {45,55,55,45,45,55,55,45},
                    {42,43,43,43,43,43,43,42},
                    {44,46,46,46,46,46,46,44},
                    {47,50,50,50,50,50,50,47},
                    {51,55,55,55,55,55,55,51},
                    {56,61,61,61,61,61,61,56},
                    {60,68,68,68,68,68,68,60},
                    {76,76,76,76,76,76,76,76}
            };

    //piece-value table from the point of view of black
    private int[][] locationValues6x6 =
            {
                    {45,50,50,50,50,45},
                    {42,43,43,43,43,42},
                    {44,46,46,46,46,44},
                    {47,50,50,50,50,47},
                    {51,55,55,55,55,51},
                    {61,61,61,61,61,61},
            };

    public int getLocationValue(int x, int y, int player, int boardSize) {
        if(boardSize==8) {
            if (player == BLACK) {
                return locationValues8x8[7 - y][x];
            } else {
                return locationValues8x8[y][x];
            }
        } else { //assuming 6x6
            if (player == BLACK) {
                return locationValues6x6[5 - y][x];
            } else {
                return locationValues6x6[y][x];
            }
        }
    }

    private double getMaxValue(int boardSize) {
        double maxValue = 0;
        if(boardSize==8) {
            maxValue = 15 * 40; //all pieces, opponent only one
            maxValue += 16 * 28 - 2; //highest possible position values, lowest possible for opponent
        } else { //assuming 6x6
            maxValue = 11 * 40; //all pieces, opponent only one
            maxValue += 12 * 15 - 2; //highest possible position values, lowest possible for opponent
        }
        return maxValue;
    }

    public void setSquareValue(int x, int y, int squarevalue) {
        locationValues8x8[x][y] = squarevalue;
    }

    @Override
    public Evaluation staticEval(Board boardToEvaluate) {
        KnightthroughNoTranspositionsBoard board = (KnightthroughNoTranspositionsBoard) boardToEvaluate;
        double evaluation = 0;
        double[] evalForColor = new double[2];
        int boardSize = board.getHeight();
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                if (board.getSquare(x, y) == WHITE_PIECE) {
                    evaluation += getLocationValue(x, y, WHITE, boardSize);
                } else if (board.getSquare(x, y) == BLACK_PIECE) {
                    evaluation -= getLocationValue(x, y, BLACK, boardSize);
                }
            }
        }
        evaluation = (evaluation + getMaxValue(boardSize)) / (2 * getMaxValue(boardSize));
        evalForColor[WHITE] = evaluation;
        evalForColor[BLACK] = 1 - evaluation;
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        String result = "KnightthroughEvaluator (static";
        result += ")";
        return result;
    }

}
