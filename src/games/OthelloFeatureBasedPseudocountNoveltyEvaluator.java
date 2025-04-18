package games;

import ai.Board;
import ai.noveltymcts.FeatureBasedPseudocountNoveltyEvaluator;
import utils.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static utils.Util.charArrayToZobristKey;

public class OthelloFeatureBasedPseudocountNoveltyEvaluator extends FeatureBasedPseudocountNoveltyEvaluator {

    private static double initialCount = 1.0/65;  // should be 1/n for using the Krichevsky-Trofimov (KT) estimator, so it depends on the feature's number of possible settings! //TODO try dependence on feature
    //keys for the 2 colors (feature types x 2)
    private long[] colorKeys;
    private boolean countsMobility = true;
    //keys for the 0-64 possible values of the feature types
    //the three types of keys XOR'd together result in a complete "feature" to be counted during the search in featureCounts.
    //probabilities of board can be computed by looking up the probabilities of the observed values of all 14x2 feature types.
    private long[] featureCountKeys;
    //keys for the 2 feature types: stable discs (0) and mobility (1)
    private long[] featureTypeKeys;
    private double stableDiscValue = 2;

    @Override
    public double evaluateAndCountFeatures(Board inputBoard) {
        OthelloNoTranspositionsBoard board = (OthelloNoTranspositionsBoard) inputBoard;
        double evaluation = 0;
        getFeatureKeysOfEvaluatedBoard().clear();
        int whiteStableDiscs = board.stableDiscs(WHITE);
        int blackStableDiscs = board.stableDiscs(BLACK);
        evaluation += (blackStableDiscs-whiteStableDiscs)*stableDiscValue;
        long featureKey = featureTypeKeys[0] ^ colorKeys[WHITE] ^ featureCountKeys[whiteStableDiscs];
        getFeatureKeysOfEvaluatedBoard().add(featureKey);
        featureKey = featureTypeKeys[0] ^ colorKeys[BLACK] ^ featureCountKeys[blackStableDiscs];
        getFeatureKeysOfEvaluatedBoard().add(featureKey);
        if(countsMobility) {
            int blackLegalMoves = board.getLegalMovesFor(BLACK).size();
            int whiteLegalMoves = board.getLegalMovesFor(WHITE).size();
            evaluation += (blackLegalMoves - whiteLegalMoves);
            featureKey = featureTypeKeys[1] ^ colorKeys[WHITE] ^ featureCountKeys[whiteLegalMoves];
            getFeatureKeysOfEvaluatedBoard().add(featureKey);
            featureKey = featureTypeKeys[1] ^ colorKeys[BLACK] ^ featureCountKeys[blackLegalMoves];
            getFeatureKeysOfEvaluatedBoard().add(featureKey);
        }
        evaluation = (evaluation+getMaxValue(board))/(2*getMaxValue(board));
        if(Util.isDebug()) {
            System.out.println("Board evaluated, value for player BLACK: "+evaluation);
        }
        return 1-evaluation;
    }

    @Override
    public double getInitialCount() {
        return initialCount;
    }

    private double getMaxValue(OthelloNoTranspositionsBoard board) {
        return (board.getHeight()*board.getWidth()-1)*stableDiscValue + (board.getHeight()*board.getWidth()); //trivial upper bound: all squares but one are mine & stable and mobility is all squares
    }

    @Override
    public void initialize(int width, int height, int numberOfColors) {
        featureTypeKeys = new long[2];
        colorKeys = new long[2];
        featureCountKeys = new long[65];
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
            for(int i=0;i<featureTypeKeys.length;i++) {
                array = input.readLine().toCharArray();
                key = charArrayToZobristKey(array);
                featureTypeKeys[i] = key;
            }
            for(int i=0;i<colorKeys.length;i++) {
                array = input.readLine().toCharArray();
                key = charArrayToZobristKey(array);
                colorKeys[i] = key;
            }
            for(int i = 0; i< featureCountKeys.length; i++) {
                array = input.readLine().toCharArray();
                key = charArrayToZobristKey(array);
                featureCountKeys[i] = key;
            }
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot read in Zobrist keys from file: breakthroughzobristkeys.txt");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public String toString() {
        return "OthelloFeatureBasedPseudocountNoveltyEvaluator (static) with beta: "+ getBeta();
    }

}
