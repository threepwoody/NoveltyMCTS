package ai.noveltymcts;

import ai.BasicBoard;
import ai.Board;
import ai.Evaluation;
import utils.UnknownPropertyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static utils.Util.charArrayToZobristKey;

public class RawStateEvaluationNovelty implements NoveltyFunction {

    private double beta = 1; //TODO tune
    //dimensions to look up keys: [x][y][piece]
    private long[][][] featureKeys;
    private Map<Long, Double> maxEvaluationsPerFeature;
    private int searchingColor;

    public RawStateEvaluationNovelty() {
        maxEvaluationsPerFeature = new HashMap<>();
    }

    private void addObservation(Board board, Evaluation evaluation) {
        for(int x=0; x<board.getWidth(); x++) {
            for(int y=0; y<board.getHeight(); y++) {
                int piece = board.getSquare(x,y);
                if(piece!=BasicBoard.OFF_BOARD) {
                    long featureKey = featureKeys[x][y][piece];
                    if(!maxEvaluationsPerFeature.containsKey(featureKey)) {
                        maxEvaluationsPerFeature.put(featureKey, Double.NEGATIVE_INFINITY);
                    }
                    if(evaluation.getValueForColor(searchingColor) > maxEvaluationsPerFeature.get(featureKey)) {
                        maxEvaluationsPerFeature.put(featureKey, evaluation.getValueForColor(searchingColor));
                    }
                }
            }
        }
    }

    public void clearObservations() {
        maxEvaluationsPerFeature = new HashMap<>();
    }

    public void initialize(int width, int height, int numberOfColors) {
        featureKeys = new long[width][height][numberOfColors+2];
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader("breakthroughzobristkeys.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find breakthroughzobristkeys.txt");
            e.printStackTrace();
            System.exit(1);
        }
        char[] array;
        long key;
        try {
            for(int x=0;x<width;x++) {
                for(int y=0;y<height;y++) {
                    for(int piece=0;piece<numberOfColors+1;piece++) {
                        array = input.readLine().toCharArray();
                        key = charArrayToZobristKey(array);
                        featureKeys[x][y][piece] = key;
                    }
                }
            }
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot read in Zobrist keys from file: breakthroughzobristkeys.txt");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public double novelty(Board board, Evaluation evaluation) {
        double result = computeNovelty(board, evaluation);
        addObservation(board, evaluation);
        return result;
    }

    private double computeNovelty(Board board, Evaluation evaluation) {
        double reward = evaluation.getValueForColor(searchingColor);
        for(int x=0; x<board.getWidth(); x++) {
            for(int y=0; y<board.getHeight(); y++) {
                int piece = board.getSquare(x,y);
                if(piece!= BasicBoard.OFF_BOARD) {
                    long featureKey = featureKeys[x][y][piece];
                    if(!maxEvaluationsPerFeature.containsKey(featureKey)) {
                        maxEvaluationsPerFeature.put(featureKey, Double.NEGATIVE_INFINITY);
                    }
                    if(reward > maxEvaluationsPerFeature.get(featureKey)) {
                        return beta;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("noveltybeta")) {
            this.beta = Double.parseDouble(value);
        } else {
            throw new UnknownPropertyException(property+" is not a known property for RawStateEvaluationNovelty.");
        }
    }

    @Override
    public void setSearchingColor(int color) {
        this.searchingColor = color;
    }

    public String toString() {
        return "RawStateEvaluationNovelty with beta: "+beta;
    }

}
