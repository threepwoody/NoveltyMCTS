package ai;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.nn.SequentialBlock;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import experiments.AlphaZero.AlphaZeroGame;
import experiments.AlphaZero.BasicMovePriorEvaluation;
import experiments.AlphaZero.NetworkFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.List;

public class NetworkMoveSorter implements MoveSorter {

    AlphaZeroGame game;
    Model model;
    private List<Double[]> movePriorsPerDimension;
    String parameterFile;
    Translator translator;

    public NetworkMoveSorter(String parameterFile, AlphaZeroGame game) {
        loadNetwork(parameterFile, game.numberOfMoveCSVIndicesPerDimension(), game.getNumberOfColors());
        this.parameterFile = parameterFile;
        this.game = game;
        this.translator = game.newBoardTranslator(true, true, false);
        Board initializationBoard = game.newBoard();
        //network call on initializationBoard - this is needed because the first use of this class otherwise takes a while (during a timed experiment)
        prepareSort(initializationBoard.getLegalMoves(), initializationBoard);
    }

    public int compareMoves(Move move1, Move move2, Board board) {
        int[] moveIndices1 = game.moveIndex(1, move1);
        int[] moveIndices2 = game.moveIndex(1, move2);
        double movePrior1 = getMovePrior(moveIndices1);
        double movePrior2 = getMovePrior(moveIndices2);
        return Double.compare(movePrior1, movePrior2);
    }

    private double getMovePrior(int[] moveIndex) {
        double result = 1;
        for(int dim=0;dim<moveIndex.length;dim++) {
            result *= movePriorsPerDimension.get(dim)[moveIndex[dim]];
        }
        return result;
    }

    private void loadNetwork(String parameterFile, int[] sizeOfPolicyOutputPerDimension, int numberOfColors) {
        model = Model.newInstance("alphazero-network", Device.cpu());
        SequentialBlock block;
        block = (SequentialBlock) NetworkFactory.residualEvaluationAndPolicyBlock(sizeOfPolicyOutputPerDimension, numberOfColors);
        try {
            block.loadParameters(model.getNDManager(), new DataInputStream(new FileInputStream(parameterFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.setBlock(block);
    }

    public void prepareSort(List<Move> legalMoves, Board board) {
        Predictor predictor = model.newPredictor(translator);
        BasicMovePriorEvaluation evaluation = null;
        try {
            evaluation = (BasicMovePriorEvaluation) predictor.predict(board);
        } catch (TranslateException e) {
            e.printStackTrace();
        }
        movePriorsPerDimension = evaluation.getMovePriors();
    }

}
