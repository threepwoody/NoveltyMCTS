package experiments.AlphaZero;

import ai.Move;
import ai.MoveSorter;
import ai.NetworkMoveSorter;
import ai.RectangularBoardGame;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;

public interface AlphaZeroGame extends SymmetricGame, RectangularBoardGame {

    default Shape getBoardTensorShape() {
        return new Shape(/*1,*/getNumberOfDifferentBoardElements()+numberOfNonBoardStateCSVIndices(),getBoardWidth(),getBoardHeight());
    }

    /**
     * Move indices by move dimension. Symmetry 1 has to be for the board as-is. (Irrelevant for training, but symmetry 1 is used for inference during play.)
     **/
    int[] moveIndex(int symmetry, Move move);

    default Translator newBoardTranslator(boolean networkHasPolicyHead, boolean usingPolicyHead, boolean usingQuantileHeads) {
        return new RectangularIntBoardTranslator(getBoardTensorShape(), networkHasPolicyHead, usingPolicyHead, usingQuantileHeads, numberOfMoveDimensions(), getNumberOfColors());
    }

    default DataPreparer newDataPreparer(NDManager manager, boolean predictingCombinationOfMCTSValueAndGameResult, boolean usingQuantileHeads) {
        return new BasicDataPreparer(this, manager, predictingCombinationOfMCTSValueAndGameResult, usingQuantileHeads);
    }

    default MoveSorter newNetworkMoveSorter(String parameterFile) {
        return new NetworkMoveSorter(parameterFile,this);
    }

    default StateNetworkEvaluator newStateNetworkEvaluator(String parameterFile, boolean usingPolicyHead, boolean usingQuantileHeads) {
        return new StateNetworkEvaluator(parameterFile, this, usingPolicyHead, usingQuantileHeads);
    }

/**
     * Number of indices needed to represent a given board.
     * State information that is not directly represented on the board, such as the
 * color to move or the current turn, is not counted here but in numberOfNonBoardStateCSVIndices().
     **/
    default int numberOfBoardStateCSVIndices() {
        return getBoardHeight()*getBoardWidth()*getNumberOfDifferentBoardElements();
    }

    /**
     * Number of indices needed to represent all possible legal moves (one-hot), by move dimension.
     **/
    int[] numberOfMoveCSVIndicesPerDimension();

    default int numberOfMoveDimensions() {
        return 1;
    }

        /**
         * Per default, only the color-to-move entries are non-board state information
         * (and will be expanded to a full layer in the standard implementation).
         * Depending on the game, there might be more, following the color-to-move
         * entries. State information that is directly represented on the board, such as
         * player pieces, is not counted here but in numberOfBoardCSVIndices().
     **/
    default int numberOfNonBoardStateCSVIndices() {
        return getNumberOfColors();
    }

}
