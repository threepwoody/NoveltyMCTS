package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;
import org.apache.http.conn.routing.RouteInfo;

import static ai.BasicBoard.NO_PIECE;
import static ai.BasicBoard.OFF_BOARD;
import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static ai.OnePieceTypeBoard.colorOfPiece;

public class AtariGoEvaluator extends StaticEvaluator {

    boolean[][] checked;
    boolean[][] checkedGroups;

    private int getLibertyCount(AtariGoNoTranspositionsBoard atariBoard, int x, int y, int piece) {
        checked = new boolean[atariBoard.getWidth()][atariBoard.getHeight()];
        return getLibertyCountRecursive(atariBoard, x, y, piece);
    }

    private int getLibertyCountRecursive(AtariGoNoTranspositionsBoard atariBoard, int x, int y, int piece) {
        int[][] board = atariBoard.getBoard();
        int square = atariBoard.getSquare(x, y);
        if(square==OFF_BOARD || checked[x][y]) { //square is off board or has been checked before
            return 0;
        } else { //square has not been checked before
            checked[x][y] = true;
            if (square == NO_PIECE) { //new liberty found
                return 1;
            } else if (square == piece) { //stone belongs to group, check neighbors recursively
                checkedGroups[x][y] = true;
                int libertyCount = 0;
                libertyCount += getLibertyCountRecursive(atariBoard, x + 1, y, piece);
                libertyCount += getLibertyCountRecursive(atariBoard, x - 1, y, piece);
                libertyCount += getLibertyCountRecursive(atariBoard, x, y + 1, piece);
                libertyCount += getLibertyCountRecursive(atariBoard, x, y - 1, piece);
                return libertyCount;
            } else { //opponent stone
                return 0;
            }
        }
    }

    private double getMaxEvaluation(int boardSize) {
        return boardSize;
    }

    @Override
    public Evaluation staticEval(Board boardToEvaluate) {
        AtariGoNoTranspositionsBoard atariBoard = (AtariGoNoTranspositionsBoard) boardToEvaluate;
//        System.out.println(atariBoard);
        int boardSize = atariBoard.getWidth()*atariBoard.getHeight();
        double[] evalForColor = new double[2];
        int[][] board = atariBoard.getBoard();
        int[] lowestLibertyCount = new int[2];
        lowestLibertyCount[WHITE] = boardSize;
        lowestLibertyCount[BLACK] = boardSize;
        checkedGroups = new boolean[atariBoard.getWidth()][atariBoard.getHeight()];
        for(int x=0;x<atariBoard.getWidth();x++) {
            for(int y=0;y<atariBoard.getHeight();y++) {
                int piece = board[x][y];
                if(piece!=NO_PIECE && !checkedGroups[x][y]) {
                    int color = colorOfPiece(piece);
                    int libertyCount = getLibertyCount(atariBoard, x, y, piece);
                    if(libertyCount<lowestLibertyCount[color]) {
                        lowestLibertyCount[color] = libertyCount;
                    }
                }
            }
        }
        double evaluation = lowestLibertyCount[WHITE]-lowestLibertyCount[BLACK];
        if(evaluation==0) {
            if (atariBoard.getColorToPlay() == WHITE) {
                evaluation += 1;
            } else {
                evaluation -= 1;
            }
        }
        evaluation = (evaluation + getMaxEvaluation(boardSize)) / (2 * getMaxEvaluation(boardSize));
        evalForColor[WHITE] = evaluation;
        evalForColor[BLACK] = 1 - evaluation;
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        return "AtariGoEvaluator (static)";
    }

}
