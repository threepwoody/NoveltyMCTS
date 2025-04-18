package games;

import ai.*;
import ai.evaluation.StaticEvaluator;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class Knightthrough extends BasicRectangularBoardGame implements StaticEvaluatorGame, AlphaZeroGame, MoveSortingGame {

    private static final MoveSorter moveSorter = new KnightthroughMoveSorter();
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][fromX][fromY][movetype 0-7]
    private static long[][][][] zobristKeys;
    private final int rowsOfPieces = 2;

    public Knightthrough(int boardSize) {
        super(2, boardSize, boardSize);
    }

    public Knightthrough() {
        super(2, 8, 8);
    }

    private static int getLargestSupportedBoardHeight() {
        return 8;
    }

    private static int getLargestSupportedBoardWidth() {
        return 8;
    }

    private static int getMaxTurnsPerMatchForLargestSupportedBoard() {
        return 256;
    }

    public static long[][][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        zobristKeys = new long[getMaxTurnsPerMatchForLargestSupportedBoard()][getLargestSupportedBoardWidth()][getLargestSupportedBoardHeight()][8];
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
                        for(int moveType=0;moveType<8;moveType++) {
                            array = input.readLine().toCharArray();
                            key = charArrayToZobristKey(array);
                            zobristKeys[timestep][x][y][moveType] = key;
                        }
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
        return getBoardWidth() * rowsOfPieces *2*8;
    }

    @Override
    public MoveSorter getMoveSorter() {
        return moveSorter;
    }

    @Override
    public String getNameWithoutProperties() {
        return "Knightthrough-"+ getBoardWidth();
    }

    public int getRowsOfPieces() {
        return rowsOfPieces;
    }

    @Override
    public int[] moveIndex(int symmetry, Move move) {
        KnightthroughMove ktMove = (KnightthroughMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int compressedMoveType;
        switch(ktMove.getMoveType()) {
            case 0:
            case 1:
                compressedMoveType = 0;
                break;
            case 2:
            case 3:
                compressedMoveType = 1;
                break;
            case 4:
            case 5:
                compressedMoveType = 2;
                break;
            case 6:
            case 7:
                compressedMoveType = 3;
                break;
            default:
                compressedMoveType = 0;
                System.out.println("error in moveIndex");
        }
        int x;
        int y = ktMove.getFromY();
        if(symmetry==1) {
            x = ktMove.getFromX();
        } else {
            x = getBoardWidth()-ktMove.getFromX()-1;
            switch(compressedMoveType) {
                case 0:
                    compressedMoveType = 3;
                    break;
                case 1:
                    compressedMoveType = 2;
                    break;
                case 2:
                    compressedMoveType = 1;
                    break;
                case 3:
                    compressedMoveType = 0;
                    break;
                default:
                    System.out.println("error in moveIndex");
            }
        }
        moveIndices[0] = y* getBoardWidth() *4 + x*4 + compressedMoveType;
        return moveIndices;
    }

    @Override
    //TODO debug
    public Board newBoard(String board, boolean isFileName) {
        return new KnightthroughNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new KnightthroughNoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new KnightthroughEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 2;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
        result[0] = getBoardHeight() * getBoardWidth() * 4;
        return result;
    }

    @Override
    public String toString() {
        return "Knightthrough (board size " + getBoardWidth() + "x" + getBoardHeight() + ")";
    }

}
