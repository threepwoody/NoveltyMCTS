package games;

import ai.*;
import ai.evaluation.StaticEvaluator;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class Connect4 extends BasicRectangularBoardGame implements StaticEvaluatorGame, AlphaZeroGame, MoveSortingGame {

    //first dimension: all types of 4 in a row that one square can potentially secure
    //second dimension: all other squares that would have to be taken by the same player for that move to win
    //third dimension: x,y offsets for that square, relative to original move
    //always starting from square with biggest offset, because that leads to quicker rejections if OFF_BOARD
    public static final int[][][] FOUR_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE = {
            //horizontals
            {{-3,0},{-2,0},{-1,0}},
            {{-2,0},{-1,0},{1,0}},
            {{-1,0},{1,0},{2,0}},
            {{1,0},{2,0},{3,0}},
            //vertical
            {{0,-3},{0,-2},{0,-1}},
            //diagonal to top right
            {{-3,-3},{-2,-2},{-1,-1}},
            {{-2,-2},{-1,-1},{1,1}},
            {{2,2},{1,1},{-1,-1}},
            {{3,3},{2,2},{1,1}},
            //diagonal to top left
            {{3,-3},{2,-2},{1,-1}},
            {{2,-2},{1,-1},{-1,1}},
            {{-2,2},{-1,1},{1,-1}},
            {{-3,3},{-2,2},{-1,1}},
    };
    private static final MoveSorter moveSorter = new Connect4MoveSorter();
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private static long[][][] zobristKeys;

    public Connect4(int boardWidth) {
        super(2, boardWidth, boardWidth-1);
    }

    public Connect4() {
        super(2, 7, 6);
    }

    private static int getLargestSupportedBoardHeight() {
        return 8;
    }

    private static int getLargestSupportedBoardWidth() {
        return 9;
    }

    private static int getMaxTurnsPerMatchForLargestSupportedBoard() {
        return 72;
    }

    public static long[][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        zobristKeys = new long[getMaxTurnsPerMatchForLargestSupportedBoard()][getLargestSupportedBoardWidth()][getLargestSupportedBoardHeight()];
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
            for(int timestep=0;timestep<getMaxTurnsPerMatchForLargestSupportedBoard();timestep++) {
                for(int x=0;x<getLargestSupportedBoardWidth();x++) {
                    for(int y=0;y<getLargestSupportedBoardHeight();y++) {
                        array = input.readLine().toCharArray();
                        key = charArrayToZobristKey(array);
                        zobristKeys[timestep][x][y] = key;
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
    public int getMaxTurnsPerMatch() {
        return getBoardWidth() * getBoardHeight();
    }

    @Override
    public MoveSorter getMoveSorter() {
        return moveSorter;
    }

    @Override
    public String getNameWithoutProperties() {
        return "Connect4-"+ getBoardWidth();
    }

    @Override
    public int[] moveIndex(int symmetry, Move m) {
        Connect4Move c4Move = (Connect4Move) m;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        if(symmetry==1) {
            moveIndices[0] = c4Move.getX();
        } else {
            moveIndices[0] = getBoardWidth()-c4Move.getX()-1;
        }
        return moveIndices;
//      alternative:
//        Connect4Move c4Move = (Connect4Move) m;
//        int[] moveIndices = new int[numberOfMoveDimensions()];
//        if(symmetry==1) {
//            moveIndices[0] = c4Move.getY()*7+c4Move.getX();
//        } else {
//            moveIndices[0] = c4Move.getY()*7+(getBoardWidth()-c4Move.getX()-1);
//        }
//        return moveIndices;
    }

    @Override
    public Board newBoard(String board, boolean isFileName) {
        return new Connect4NoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new Connect4NoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new Connect4BetterEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 2;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
//        result[0] = getBoardWidth()*getBoardHeight();
        result[0] = getBoardWidth();
        return result;
    }

    @Override
    public String toString() {
        return "Connect4";
    }

}
