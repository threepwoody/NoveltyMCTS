package games;

import ai.*;
import ai.evaluation.StaticEvaluator;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class NeoCrossings extends BasicRectangularBoardGame implements StaticEvaluatorGame, AlphaZeroGame {

    private static long[] colorToPlayKeys;
    private final int maxPushDistance;
    //one distinct key for each possible piece at each possible location. this means transpositions are possible
    //dimensions to look up keys: [x][y][color]
    private static long[][][] zobristKeys;

    public NeoCrossings(int boardSize) {
        super(2, boardSize, boardSize);
        maxPushDistance = boardSize/2;
    }

    public NeoCrossings() {
        super(2, 14, 12);
        maxPushDistance = 4;
    }

    public static long[] getColorToPlayKeys() {
        if(colorToPlayKeys==null) {
            readInZobristKeys();
        }
        return colorToPlayKeys;
    }

    private static int getLargestSupportedBoardHeight() {
        return 12;
    }

    private static int getLargestSupportedBoardWidth() {
        return 14;
    }

    public static long[][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        zobristKeys = new long[getLargestSupportedBoardWidth()][getLargestSupportedBoardHeight()][2];
        colorToPlayKeys = new long[2];
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
            for(int color=0;color<2;color++) {
                for(int x=0;x<getLargestSupportedBoardWidth();x++) {
                    for(int y=0;y<getLargestSupportedBoardHeight();y++) {
                        array = input.readLine().toCharArray();
                        key = charArrayToZobristKey(array);
                        zobristKeys[x][y][color] = key;
                    }
                }
                array = input.readLine().toCharArray();
                key = charArrayToZobristKey(array);
                colorToPlayKeys[color] = key;
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
        return 500;
    }

    @Override
    public String getNameWithoutProperties() {
        return "NeoCrossings-"+getBoardHeight();
    }

    @Override
    //TODO probably, multiple move dimensions that are independently predicted in different policy layers are the better solution
    public int[] moveIndex(int symmetry, Move move) {
        NeoCrossingsMove ncMove = (NeoCrossingsMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int x;
        int y = ncMove.getFromY();
        int pushDirection;
        int pushDistance = ncMove.getPushDistance()-1;
        if(symmetry==1) {
            x = ncMove.getFromX();
            pushDirection = ncMove.getPushDirection();
        } else {
            x = getBoardWidth()-ncMove.getFromX()-1;
            switch(ncMove.getPushDirection()) {
                case 0:
                    pushDirection = 0;
                    break;
                default:
                    pushDirection = 8-ncMove.getPushDirection();
            }
        }
        moveIndices[0] = y*getBoardWidth() + x;
        moveIndices[1] = pushDirection;
        moveIndices[2] = pushDistance;
        return moveIndices;
    }

    @Override
    public Board newBoard(String board, boolean isFileName) {
        return new NeoCrossingsNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new NeoCrossingsNoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new NeoCrossingsEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 2;
    }

    @Override
    public int numberOfMoveDimensions() {
        return 3;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[3];
        result[0] = getBoardHeight() * getBoardWidth();
        result[1] = 8;
        result[2] = maxPushDistance;
        return result;
    }

    @Override
    public String toString() {
        return "NeoCrossings";
    }

}
