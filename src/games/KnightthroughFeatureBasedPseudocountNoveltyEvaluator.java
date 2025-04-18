package games;

import ai.Board;
import ai.noveltymcts.FeatureBasedPseudocountNoveltyEvaluator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static ai.OnePieceTypeTwoPlayerBoard.BLACK_PIECE;
import static ai.OnePieceTypeTwoPlayerBoard.WHITE_PIECE;
import static utils.Util.charArrayToZobristKey;

//currently only works for 8x8 boards
//uses actual evaluation features for novelty computation, as suggested in "Count-Based Exploration in Feature Space for Reinforcement Learning"
public class KnightthroughFeatureBasedPseudocountNoveltyEvaluator extends FeatureBasedPseudocountNoveltyEvaluator {

    private static double initialCount = 1.0/11;  // should be 1/n for using the Krichevsky-Trofimov (KT) estimator, so it depends on the feature's number of possible settings! //TODO try dependence on feature
    //piece-value table from the point of view of black
    private static int[][] locationValues8x8 =
            {
                    {45,55,55,45,45,55,55,45},
                    {42,43,43,43,43,43,43,42},
                    {44,46,46,46,46,46,46,44},
                    {47,50,50,50,50,50,50,47},
                    {51,55,55,55,55,55,55,51},
                    {56,61,61,61,61,61,61,56},
                    {60,68,68,68,68,68,68,60},
                    {76,76,76,76,76,76,76,76}
            };
    private static Map<Integer, Integer> pieceValueToFeatureType;

    static {
        pieceValueToFeatureType = new HashMap<>();
        pieceValueToFeatureType.put(42, 0);
        pieceValueToFeatureType.put(43, 1);
        pieceValueToFeatureType.put(44, 2);
        pieceValueToFeatureType.put(45, 3);
        pieceValueToFeatureType.put(46, 4);
        pieceValueToFeatureType.put(47, 5);
        pieceValueToFeatureType.put(50, 6);
        pieceValueToFeatureType.put(51, 7);
        pieceValueToFeatureType.put(55, 8);
        pieceValueToFeatureType.put(56, 9);
        pieceValueToFeatureType.put(60, 10);
        pieceValueToFeatureType.put(61, 11);
        pieceValueToFeatureType.put(68, 12);
        pieceValueToFeatureType.put(76, 13);
    }

    private Map<Integer, Integer> blackFeatureTypeCounts;
    //keys for the 2 colors (feature types x 2)
    private long[] colorKeys;
    //keys for the 0-10 pieces each color can have at most of any of the 14 piece values (feature type values)
    //the three types of keys XOR'd together result in a complete "feature" to be counted during the search in featureCounts.
    //probabilities of board can be computed by looking up the probabilities of the observed values of all 14x2 feature types.
    private long[] featureCountKeys;
    //keys for the 14 possible piece values (feature types)
    private long[] featureTypeKeys;
    private Map<Integer, Integer> whiteFeatureTypeCounts;

    public KnightthroughFeatureBasedPseudocountNoveltyEvaluator() {
        blackFeatureTypeCounts = new HashMap<>();
        whiteFeatureTypeCounts = new HashMap<>();
    }

    @Override
    public double evaluateAndCountFeatures(Board inputBoard) {
        KnightthroughNoTranspositionsBoard board = (KnightthroughNoTranspositionsBoard) inputBoard;
        double evaluation = 0;
        getFeatureKeysOfEvaluatedBoard().clear();
        blackFeatureTypeCounts.clear();
        whiteFeatureTypeCounts.clear();
        for(int x = 0; x<board.getWidth(); x++) {
            for(int y=0;y<board.getHeight();y++) {
                if(board.getSquare(x,y)==WHITE_PIECE) {
                    int value = getLocationValue(x, y, WHITE);
                    evaluation += value;
                    int featureType = pieceValueToFeatureType.get(value);
                    if(!whiteFeatureTypeCounts.containsKey(featureType)) {
                        whiteFeatureTypeCounts.put(featureType, 0);
                    }
                    whiteFeatureTypeCounts.put(featureType, whiteFeatureTypeCounts.get(featureType)+1);
                } else if(board.getSquare(x,y)==BLACK_PIECE) {
                    int value = getLocationValue(x, y, BLACK);
                    evaluation -= value;
                    int featureType = pieceValueToFeatureType.get(value);
                    if(!blackFeatureTypeCounts.containsKey(featureType)) {
                        blackFeatureTypeCounts.put(featureType, 0);
                    }
                    blackFeatureTypeCounts.put(featureType, blackFeatureTypeCounts.get(featureType)+1);
                }
            }
        }
        for(int featureType=0; featureType<featureTypeKeys.length; featureType++) {
            for(int color=0; color<colorKeys.length; color++) {
                Map<Integer, Integer> featureTypeCounts = color==BLACK ? blackFeatureTypeCounts : whiteFeatureTypeCounts;
                int count = featureTypeCounts.containsKey(featureType) ? featureTypeCounts.get(featureType) : 0;
                long featureKey = featureTypeKeys[featureType] ^ colorKeys[color] ^ featureCountKeys[count];
                getFeatureKeysOfEvaluatedBoard().add(featureKey);
            }
        }
        evaluation = (evaluation+getMaxValue())/(2*getMaxValue());
        return evaluation;
    }

    @Override
    public double getInitialCount() {
        return initialCount;
    }

    public int getLocationValue(int x, int y, int player) {
        if(player==BLACK) {
            return locationValues8x8[7-y][x];
        } else {
            return locationValues8x8[y][x];
        }
    }

    protected double getMaxValue() {
        double maxValue = 15*40; //all pieces, opponent only one
        maxValue += 16*28-2; //highest possible position values, lowest possible for opponent
        return maxValue;
    }

    @Override
    public void initialize(int width, int height, int numberOfColors) {
        featureTypeKeys = new long[14];
        colorKeys = new long[2];
        featureCountKeys = new long[11];
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
        return "KnightthroughFeatureBasedPseudocountNoveltyEvaluator (static) with beta: "+ getBeta();
    }

}
