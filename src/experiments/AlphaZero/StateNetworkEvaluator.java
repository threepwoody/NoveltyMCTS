package experiments.AlphaZero;

import ai.Board;
import ai.Evaluation;
import ai.djl.Device;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.nn.BlockList;
import ai.djl.nn.ParallelBlock;
import ai.djl.nn.SequentialBlock;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.evaluation.StaticEvaluator;

import java.io.DataInputStream;
import java.io.FileInputStream;

public class StateNetworkEvaluator extends StaticEvaluator {

    Model model;
    private boolean networkHasPolicyHead = true;
    String parameterFile;
    Translator translator;

    public StateNetworkEvaluator(String parameterFile, AlphaZeroGame game, boolean usingPolicyHead, boolean usingQuantileHeads) {
        loadNetwork(parameterFile, game.numberOfMoveCSVIndicesPerDimension(), game.getNumberOfColors(), usingPolicyHead, usingQuantileHeads);
        this.parameterFile = parameterFile;
        if(networkHasPolicyHead) {
            this.translator = game.newBoardTranslator(true, usingPolicyHead, usingQuantileHeads);
        } else {
            this.translator = game.newBoardTranslator(false, false, usingQuantileHeads);
        }
        Board initializationBoard = game.newBoard();
        staticEval(initializationBoard); //this is needed because the first use of this class otherwise takes a while (during a timed experiment)
    }

    @Override
    public void endGame() {
        model.close();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            model.close();
        } finally {
            super.finalize();
        }
    }

    public String getParameterFile() {
        return parameterFile;
    }

    private void loadNetwork(String parameterFile, int[] sizeOfPolicyOutputPerDimension, int numberOfColors, boolean usingPolicyHead, boolean usingQuantileHeads) {
        model = Model.newInstance("alphazero-network", Device.cpu());
        SequentialBlock block;
        if(usingQuantileHeads) {
            block = (SequentialBlock) NetworkFactory.quantileBlock(sizeOfPolicyOutputPerDimension, numberOfColors);
        } else {
            block = (SequentialBlock) NetworkFactory.residualEvaluationAndPolicyBlock(sizeOfPolicyOutputPerDimension, numberOfColors);
        }
        try {
            block.loadParameters(model.getNDManager(), new DataInputStream(new FileInputStream(parameterFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!usingPolicyHead) {
            BlockList children = block.getChildren();
            ParallelBlock parallelBlock = (ParallelBlock) children.get(children.size() - 1).getValue();
            SequentialBlock valueBlock = (SequentialBlock) parallelBlock.getChildren().get(0).getValue();
            block.removeLastBlock();
            block.add(valueBlock);
        }
        model.setBlock(block);
    }

    @Override
    public Evaluation staticEval(Board board) {
        //do forward pass, get output with predictor
        Predictor predictor = model.newPredictor(translator);
        Evaluation evaluation = null;
        try {
            evaluation = (Evaluation) predictor.predict(board);
        } catch (TranslateException e) {
            e.printStackTrace();
        }
        predictor.close();
        return evaluation;
    }

    @Override
    public String toString() {
        String result = "StateNetworkEvaluator (parameter file: "+ getParameterFile() +", translator: "+translator+")";
        return result;
    }

}
