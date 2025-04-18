package games;

import ai.Board;
import ai.noveltymcts.FeatureBasedPseudocountNoveltyEvaluator;
import utils.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ai.BasicBoard.OFF_BOARD;
import static ai.Game.BLACK;
import static ai.OnePieceTypeTwoPlayerBoard.BLACK_PIECE;
import static ai.OnePieceTypeTwoPlayerBoard.WHITE_PIECE;
import static utils.Util.charArrayToZobristKey;

public class Connect4FeatureBasedPseudocountNoveltyEvaluator extends FeatureBasedPseudocountNoveltyEvaluator {

    private static double initialCount = 1.0/72;  // should be 1/n for using the Krichevsky-Trofimov (KT) estimator, so it depends on the feature's number of possible settings //TODO try dependence on individual feature
    private Map<Integer, Integer> blackFeatureTypeCounts;
    //keys for the 2 colors (feature types x 2)
    private long[] colorKeys;
    private double[] comboValues = {2,6,30};
    //keys for the counts each color can have of any of the 3 combo values (feature type values)
    //the three types of keys XOR'd together result in a complete "feature" to be counted during the search in featureCounts.
    //probabilities of board can be computed by looking up the probabilities of the observed values of all 3x2 feature types.
    private long[] featureCountKeys;
    //keys for the 3 possible combo values (feature types)
    private long[] featureTypeKeys;
    private double logisticGrowthRate = 400;
    private Map<Integer, Integer> whiteFeatureTypeCounts;

    public Connect4FeatureBasedPseudocountNoveltyEvaluator() {
        blackFeatureTypeCounts = new HashMap<>();
        whiteFeatureTypeCounts = new HashMap<>();
    }

    @Override
    public double evaluateAndCountFeatures(Board boardToEvaluate) {
        Connect4NoTranspositionsBoard board = (Connect4NoTranspositionsBoard) boardToEvaluate;
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();
        double evaluation = 0;
        getFeatureKeysOfEvaluatedBoard().clear();
        blackFeatureTypeCounts.clear();
        whiteFeatureTypeCounts.clear();
        //check all possible 4 in a row on the entire board - give no points for empty ones or impossible ones, give X points for 1 piece, Y for 2 pieces, Z for 3 pieces
        //horizontals
        for (int y = 0; y < boardHeight; y++) {
            int[] whitePiecesIn4 = new int[boardWidth-3];
            int[] blackPiecesIn4 = new int[boardWidth-3];
            for(int x = 0; x < boardWidth; x++) {
                int piece = board.getSquare(x,y);
                if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,x-3);i<=Math.min(x,boardWidth-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,x-3);i<=Math.min(x,boardWidth-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    evaluation += comboValues[whitePiecesIn4[i]-1];
                    if(!whiteFeatureTypeCounts.containsKey(whitePiecesIn4[i]-1)) {
                        whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, 0);
                    }
                    whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, whiteFeatureTypeCounts.get(whitePiecesIn4[i]-1)+1);
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    evaluation -= comboValues[blackPiecesIn4[i]-1];
                    if(!blackFeatureTypeCounts.containsKey(blackPiecesIn4[i]-1)) {
                        blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, 0);
                    }
                    blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, blackFeatureTypeCounts.get(blackPiecesIn4[i]-1)+1);
                }
            }
        }
        //verticals
        for (int x = 0; x < boardWidth; x++) {
            int[] whitePiecesIn4 = new int[boardHeight - 3];
            int[] blackPiecesIn4 = new int[boardHeight - 3];
            for (int y = 0; y < boardHeight; y++) {
                int piece = board.getSquare(x, y);
                if (piece == WHITE_PIECE) {
                    for (int i = Math.max(0, y - 3); i <= Math.min(y, boardHeight - 4); i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if (piece == BLACK_PIECE) {
                    for (int i = Math.max(0, y - 3); i <= Math.min(y, boardHeight - 4); i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for (int i = 0; i < whitePiecesIn4.length; i++) {
                if (whitePiecesIn4[i] > 0 && blackPiecesIn4[i] == 0) {
                    evaluation += comboValues[whitePiecesIn4[i] - 1];
                    if(!whiteFeatureTypeCounts.containsKey(whitePiecesIn4[i]-1)) {
                        whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, 0);
                    }
                    whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, whiteFeatureTypeCounts.get(whitePiecesIn4[i]-1)+1);
                } else if (whitePiecesIn4[i] == 0 && blackPiecesIn4[i] > 0) {
                    evaluation -= comboValues[blackPiecesIn4[i] - 1];
                    if(!blackFeatureTypeCounts.containsKey(blackPiecesIn4[i]-1)) {
                        blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, 0);
                    }
                    blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, blackFeatureTypeCounts.get(blackPiecesIn4[i]-1)+1);
                }
            }
        }
        //diagonal to top right
        for (int x = 4-boardHeight; x <= boardWidth-4; x++) {
            int[] whitePiecesIn4 = new int[boardHeight-3];
            int[] blackPiecesIn4 = new int[boardHeight-3];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x+offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i] = -10;
                        blackPiecesIn4[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    evaluation += comboValues[whitePiecesIn4[i]-1];
                    if(!whiteFeatureTypeCounts.containsKey(whitePiecesIn4[i]-1)) {
                        whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, 0);
                    }
                    whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, whiteFeatureTypeCounts.get(whitePiecesIn4[i]-1)+1);
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    evaluation -= comboValues[blackPiecesIn4[i]-1];
                    if(!blackFeatureTypeCounts.containsKey(blackPiecesIn4[i]-1)) {
                        blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, 0);
                    }
                    blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, blackFeatureTypeCounts.get(blackPiecesIn4[i]-1)+1);
                }
            }
        }
        //diagonal to top left
        for (int x = (boardWidth-1)-(4-boardHeight); x >= 3; x--) {
            int[] whitePiecesIn4 = new int[boardHeight-3];
            int[] blackPiecesIn4 = new int[boardHeight-3];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x-offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i] = -10;
                        blackPiecesIn4[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    evaluation += comboValues[whitePiecesIn4[i]-1];
                    if(!whiteFeatureTypeCounts.containsKey(whitePiecesIn4[i]-1)) {
                        whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, 0);
                    }
                    whiteFeatureTypeCounts.put(whitePiecesIn4[i]-1, whiteFeatureTypeCounts.get(whitePiecesIn4[i]-1)+1);
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    evaluation -= comboValues[blackPiecesIn4[i]-1];
                    if(!blackFeatureTypeCounts.containsKey(blackPiecesIn4[i]-1)) {
                        blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, 0);
                    }
                    blackFeatureTypeCounts.put(blackPiecesIn4[i]-1, blackFeatureTypeCounts.get(blackPiecesIn4[i]-1)+1);
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
        evaluation = (evaluation+getMaxValue(board))/(2*getMaxValue(board));
        evaluation =  Util.logisticFunction(evaluation, logisticGrowthRate,0.5);
        return evaluation;
    }

    @Override
    public double getInitialCount() {
        return initialCount;
    }

    private double getMaxValue(Connect4NoTranspositionsBoard board) {
        //very loose upper bound: opponent has no pieces on board, i have three pieces in every possible row of four.
        return comboValues[2]*((board.getWidth()-3)*board.getHeight()+(board.getHeight()-3)*board.getWidth()+2*(board.getHeight()*board.getWidth()-7)*(board.getHeight()-3));
    }

    @Override
    public void initialize(int width, int height, int numberOfColors) {
        featureTypeKeys = new long[3];
        colorKeys = new long[2];
        featureCountKeys = new long[100];
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
        return "Connect4FeatureBasedPseudocountNoveltyEvaluator (static) with beta: "+ getBeta();
    }


}
