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

public class RawStatePseudocountNovelty implements NoveltyFunction {

    private static double initialCount = 0.5;  // should be 1/n for using the Krichevsky-Trofimov (KT) estimator
    private static int initialSamples = 1;  //1 for using the Krichevsky-Trofimov (KT) estimator
    private double beta = 0.05;
    private Map<Long, Double> featureCounts;
    //dimensions to look up keys: [x][y][piece]
    private long[][][] featureKeys;
    private int samples;

    public RawStatePseudocountNovelty() {
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
        //TODO might also try the "naive pseudocount" instead, because it's cheaper
        double rho_t = probabilityOf(board);
        addObservation(board, evaluation);
        double rho_t1 = probabilityOf(board); //TODO change this - should use stored keys from rho_t
        double pseudoCount = (rho_t*(1-rho_t1))/(rho_t1-rho_t);
        double noveltyBonus = beta/Math.sqrt(pseudoCount);
        if(Util.isDebug()) {
            System.out.println("Prior probability of seeing this board: "+rho_t);
            System.out.println("Posterior probability of seeing this board: "+rho_t1);
            System.out.println("Pseudocount of this board: "+pseudoCount);
            System.out.println("Novelty bonus for this board: "+noveltyBonus);
        }
        return noveltyBonus;
    }

    private double probabilityOf(Board board) {
        double result = 0;
        for(int x=0; x<board.getWidth(); x++) {
            for(int y=0; y<board.getHeight(); y++) {
                int piece = board.getSquare(x,y);
                if(piece!=BasicBoard.OFF_BOARD) {
                    long featureKey = featureKeys[x][y][piece];
                    if(!featureCounts.containsKey(featureKey)) {
                        featureCounts.put(featureKey, initialCount);
                    }
                    result += Math.log(featureCounts.get(featureKey)/samples);
                }
            }
        }
        return Math.exp(result);
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("noveltybeta")) {
            this.beta = Double.parseDouble(value);
        } else {
            throw new UnknownPropertyException(property+" is not a known property for RawStatePseudocountNovelty.");
        }
    }

    @Override
    public void setSearchingColor(int color) {
    }

    @Override
    public String toString() {
        return "RawStatePseudocountNovelty with beta: "+beta;
    }

}
