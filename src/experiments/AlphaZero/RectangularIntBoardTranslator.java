package experiments.AlphaZero;

import ai.Evaluation;
import ai.RectangularIntBoard;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.ArrayList;
import java.util.List;

import static utils.Util.convertFloatArrayToDoubleArray;

public class RectangularIntBoardTranslator implements Translator<RectangularIntBoard, Evaluation> {

    boolean networkHasPolicyHead;
    int numberOfColors;
    int numberOfMoveDimensions;
    Shape shape;
    boolean usingPolicyHead;
    boolean usingQuantileHeads; //TODO remove option

    public RectangularIntBoardTranslator(Shape shape, boolean networkHasPolicyHead, boolean usingPolicyHead, boolean usingQuantileHeads, int numberOfMoveDimensions, int numberOfColors) {
        this.shape = shape;
        this.networkHasPolicyHead = networkHasPolicyHead;
        this.usingPolicyHead = usingPolicyHead;
        this.numberOfMoveDimensions = numberOfMoveDimensions;
        this.numberOfColors = numberOfColors;
        this.usingQuantileHeads = usingQuantileHeads;
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    public Shape getShape() {
        return shape;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, RectangularIntBoard azBoard) {
        int[][] board = azBoard.getBoard();
        int boardSize = board.length*board[0].length;
        int colors = azBoard.getNumberOfColors();
        int boardElements = azBoard.getNumberOfDifferentBoardElements();
        //build a single 1D int[] from white, black pieces, and player to move, then construct NDArray directly from that
        float[] cnnData = new float[boardSize*(colors+boardElements)];
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
        for(int color=0; color<colors; color++) {
            if(color==colorToPlay) {
                int offsetForColor = (boardElements+color)*boardSize;
                for(int x=offsetForColor; x<offsetForColor+boardSize; x++) {
                    cnnData[x] = 1f;
                }
            }
        }
        NDArray boardArray = ctx.getNDManager().create(shape);
        boardArray.set(cnnData);
        return new ai.djl.ndarray.NDList(boardArray);
    }

    @Override
    public Evaluation processOutput(TranslatorContext ctx, NDList list) {
        double[] evalForColor = new double[numberOfColors];
        Evaluation result;
        if(networkHasPolicyHead) {
            NDArray valueOutput = list.get(0);
            valueOutput = valueOutput.softmax(0);
            for(int color=0; color<numberOfColors; color++) {
                evalForColor[color] = valueOutput.getFloat(color);
            }
            if(usingPolicyHead) {
                List<Double[]> movePriors = new ArrayList<>();
                int policyHeadOffset = usingQuantileHeads ? 3 : 1;
                for(int dim=0; dim<numberOfMoveDimensions; dim++) {
                    NDArray policyOutput = list.get(policyHeadOffset+dim);
                    policyOutput = policyOutput.softmax(0);
                    float[] floatMovePriors = policyOutput.toFloatArray();
                    Double[] movePriorsForDimension = convertFloatArrayToDoubleArray(floatMovePriors);
                    movePriors.add(movePriorsForDimension);
                }
                result = new BasicMovePriorEvaluation(evalForColor, movePriors);
            } else {
                result = new Evaluation(evalForColor);
            }
        } else {
            NDArray valueOutput = list.get(0);
            valueOutput = valueOutput.softmax(0);
            for(int color=0; color<numberOfColors; color++) {
                evalForColor[color] = valueOutput.getFloat(color);
            }
            result = new Evaluation(evalForColor);
        }
        return result;
    }

}
