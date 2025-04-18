package experiments.AlphaZero;

import ai.RectangularIntBoard;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.TranslatorContext;
import utils.Util;

/*
When using this, also set in the game class:
    @Override
    public int numberOfNonBoardStateCSVIndices() {
        return getNumberOfColors()+1;
    }
And in the board class:
        double turn = Util.round(Math.min(getTurn()/((double)getHeight()*getWidth()),1),3);
        as last element of CSV representation
 */

public class TurnIntBoardTranslator extends RectangularIntBoardTranslator {

    int boardSize;
    int numberOfNonBoardStateCSVIndices;

    public TurnIntBoardTranslator(Shape shape, boolean networkHasPolicyHead, boolean usingPolicyHead, boolean usingQuantileHeads, int numberOfMoveDimensions, int numberOfColors) {
        super(shape, networkHasPolicyHead, usingPolicyHead, usingQuantileHeads, numberOfMoveDimensions, numberOfColors);
        this.boardSize = boardSize;
        this.numberOfNonBoardStateCSVIndices = numberOfNonBoardStateCSVIndices;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, RectangularIntBoard azBoard) {
        int[][] board = azBoard.getBoard();
        int boardSize = board.length*board[0].length;
        int boardElements = azBoard.getNumberOfDifferentBoardElements();
        //build a single 1D int[] from white, black pieces, and player to move, then construct NDArray directly from that
        float[] cnnData = new float[boardSize*(numberOfNonBoardStateCSVIndices+boardElements)];
        for(int x=0; x<board.length; x++) {
            for(int y=0; y<board[0].length; y++) {
                for(int boardElement=1; boardElement<=boardElements; boardElement++) {
                    if (board[x][y]==boardElement) {
                        int offsetForElement = (boardElement-1)*boardSize;
                        int coordinateOfPiece = (board[0].length - 1 - y) * board.length + x;
                        cnnData[offsetForElement + coordinateOfPiece] = 1f;
                        break;
                    }
                }
            }
        }
        int colorToPlay = azBoard.getColorToPlay();
        for(int color = 0; color< getNumberOfColors(); color++) {
            if(color==colorToPlay) {
                int offsetForColor = (boardElements+color)*boardSize;
                for(int x=offsetForColor; x<offsetForColor+boardSize; x++) {
                    cnnData[x] = 1f;
                }
            }
        }
        int offsetForTurn = (boardElements+ getNumberOfColors())*boardSize;
        float normalizedTurn = (float) Util.round(Math.min(azBoard.getTurn()/boardSize,1),3);
        for(int x=offsetForTurn; x<offsetForTurn+boardSize; x++) {
            cnnData[x] = normalizedTurn;
        }
        NDArray boardArray = ctx.getNDManager().create(getShape());
        boardArray.set(cnnData);
        return new ai.djl.ndarray.NDList(boardArray);
    }

}
