package experiments.AlphaZero;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;

import java.util.Scanner;

//does everything that TrainingDataCreator does, but only for a single row of the csv
public class BasicDataPreparer implements DataPreparer {

    String CSVLine;
    AlphaZeroGame game;
    NDManager manager;
    boolean predictingCombinationOfMCTSValueAndGameResult;
    boolean usingQuantileHeads;

    public BasicDataPreparer(AlphaZeroGame game, NDManager manager, boolean predictingCombinationOfMCTSValueAndGameResult, boolean usingQuantileHeads) {
        this.game = game;
        this.manager = manager;
        this.predictingCombinationOfMCTSValueAndGameResult = predictingCombinationOfMCTSValueAndGameResult;
        this.usingQuantileHeads = usingQuantileHeads;
    }

    @Override
    public NDList datum() {
        Scanner scanner = new Scanner(CSVLine);
        scanner.useDelimiter(",");
        int numberOfBoardStateVariables = game.numberOfBoardStateCSVIndices();
        int numberOfNonBoardStateVariables = game.numberOfNonBoardStateCSVIndices();
        float[] datumArray;
        int sizeOfInputLayer = numberOfBoardStateVariables / game.getNumberOfDifferentBoardElements();
        datumArray = new float[sizeOfInputLayer * (game.getNumberOfDifferentBoardElements()+numberOfNonBoardStateVariables)];
        //creating layers with the players' pieces
        for (int i = 0; i < sizeOfInputLayer * game.getNumberOfDifferentBoardElements(); i++) {
            datumArray[i] = scanner.nextInt();
        }
        //creating non-board state information layers (players to move, turn, etc)
        int index = sizeOfInputLayer * game.getNumberOfDifferentBoardElements();
        for(int nonBoardStateVariable=0; nonBoardStateVariable<numberOfNonBoardStateVariables; nonBoardStateVariable++) {
            float variable = scanner.nextFloat();
            for (; index < sizeOfInputLayer * (game.getNumberOfDifferentBoardElements()+nonBoardStateVariable+1); index++) {
                datumArray[index] = variable;
            }
        }
        //create NDArray, bring into correct shape, and pack into NDList to return
        NDList datum = new NDList();
        NDArray datumNDArray = manager.create(datumArray);
        Shape boardTensorShape = game.getBoardTensorShape();
        datumNDArray = datumNDArray.reshape(boardTensorShape);
        datum.add(datumNDArray);
        return datum;
    }

    @Override
    public NDList label() {
        Scanner scanner = new Scanner(CSVLine);
        scanner.useDelimiter(",");
        int numberOfStateCSVIndices = game.numberOfBoardStateCSVIndices()+game.numberOfNonBoardStateCSVIndices();
        int[] numberOfMoveEntriesPerDimension = game.numberOfMoveCSVIndicesPerDimension();
        int numberOfMoveDimensions = game.numberOfMoveDimensions();
        int colors = game.getNumberOfColors();
        float[] valueArray;
        float[][] policyArrays = new float[numberOfMoveDimensions][];
        for (int i=0;i<numberOfStateCSVIndices; i++) {
            scanner.next();
        }
        valueArray = new float[colors];
        if(predictingCombinationOfMCTSValueAndGameResult) {
            for (int i = 0; i < colors; i++) {
                float x = scanner.nextFloat();
                valueArray[i] = (float) (0.5 * x);
            }
            for (int i = 0; i < colors; i++) {
                float x = scanner.nextFloat();
                valueArray[i] += (float) (0.5 * x);
            }
        } else {
            for (int i = 0; i < colors; i++) {
                valueArray[i] = scanner.nextFloat();
            }
            for (int i = 0; i < colors; i++) {
                scanner.nextFloat();
            }
        }
        for(int dimension=0; dimension<numberOfMoveDimensions; dimension++) {
            policyArrays[dimension] = new float[numberOfMoveEntriesPerDimension[dimension]];
            for (int i = 0; i < policyArrays[dimension].length; i++) {
                policyArrays[dimension][i] += scanner.nextFloat();
            }
        }
        NDList label = new NDList();
        NDArray valueNDArray = manager.create(valueArray);
        label.add(valueNDArray);
        if(usingQuantileHeads) {
            label.add(valueNDArray);
            label.add(valueNDArray);
        }
        for(int dimension=0; dimension<numberOfMoveDimensions; dimension++) {
            NDArray policyNDArray = manager.create(policyArrays[dimension]);
            label.add(policyNDArray);
        }
        return label;
    }

    @Override
    public void setCSVInput(String CSVLine) {
        this.CSVLine = CSVLine;
    }

}
