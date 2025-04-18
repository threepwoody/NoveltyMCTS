package ai.noveltymcts;

import ai.BasicBoard;
import ai.Board;
import ai.Evaluation;
import utils.UnknownPropertyException;
import utils.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static utils.Util.charArrayToZobristKey;

public class RawStateThresholdNovelty implements NoveltyFunction {

    private static double initialCount = 0.5;  // should be 1/n for using the Krichevsky-Trofimov (KT) estimator
    private static int initialSamples = 1;  //1 for using the Krichevsky-Trofimov (KT) estimator
    private double frequencyLimit = 0.05;
    private double beta = 1;
    private Map<Long, Double> featureCounts;
    //dimensions to look up keys: [x][y][piece]
    private long[][][] featureKeys;
    private int samples;

    public RawStateThresholdNovelty() {
        featureCounts = new HashMap<>();
        samples = initialSamples;
    }

    private void addObservation(Board board, Evaluation evaluation) {
        samples++;
        for(int x=0; x<board.getWidth(); x++) {
            for(int y=0; y<board.getHeight(); y++) {
                int piece = board.getSquare(x,y);
                if(piece!= BasicBoard.OFF_BOARD) {
                    long featureKey = featureKeys[x][y][piece];
                    if(!featureCounts.containsKey(featureKey)) {
                        featureCounts.put(featureKey, initialCount);
                    }
                    featureCounts.put(featureKey, featureCounts.get(featureKey)+1);
                }
            }
        }
    }

    @Override
    public void clearObservations() {
        featureCounts = new HashMap<>();
        samples = initialSamples;
    }

    @Override
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
        addObservation(board, evaluation);
        double noveltyBonus = 0;
        for(int x=0; x<board.getWidth(); x++) {
            for(int y=0; y<board.getHeight(); y++) {
                int piece = board.getSquare(x,y);
                if(piece!=BasicBoard.OFF_BOARD) {
                    long featureKey = featureKeys[x][y][piece];
                    if(featureCounts.get(featureKey)/samples < frequencyLimit) {
                        noveltyBonus += beta;
                    }
                }
            }
        }
        if(Util.isDebug()) {
            System.out.println(board);
            System.out.println("Novelty bonus for the previous board: "+noveltyBonus);
        }
        return noveltyBonus;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("noveltybeta")) {
//            System.out.println("beta");
            this.beta = Double.parseDouble(value);
        } else if(property.equals("noveltyfrequencylimit")) {
//            System.out.println("novelty");
            this.frequencyLimit = Double.parseDouble(value);
        } else {
            throw new UnknownPropertyException(property+" is not a known property for RawStateThresholdNovelty.");
        }
    }

    @Override
    public void setSearchingColor(int color) {
    }

    @Override
    public String toString() {
        return "RawStateThresholdNovelty with beta: "+beta+" and frequency limit: "+frequencyLimit;
    }

}
