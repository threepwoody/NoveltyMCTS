package games;

import ai.*;
import ai.evaluation.StaticEvaluator;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class Othello extends BasicRectangularBoardGame implements StaticEvaluatorGame, AlphaZeroGame, MoveSortingGame {

    private static final MoveSorter moveSorter = new OthelloMoveSorter();
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][X][Y] - special X/Y values reserved for a PASS move
    private static long[][][] zobristKeys;

    public Othello(int boardSize) {
        super(2, boardSize, boardSize);
    }

    public Othello() {
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

    public static long[][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        zobristKeys = new long[getMaxTurnsPerMatchForLargestSupportedBoard()][getLargestSupportedBoardWidth()+1][getLargestSupportedBoardHeight()+1];
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
                for(int x=0;x<getLargestSupportedBoardWidth()+1;x++) {
                    for(int y=0;y<getLargestSupportedBoardHeight()+1;y++) {
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
    //passes are allowed in Othello
    public int getMaxTurnsPerMatch() {
        return getBoardWidth() * getBoardHeight() * 2;
    }

    @Override
    public MoveSorter getMoveSorter() {
        return moveSorter;
    }

    @Override
    public String getNameWithoutProperties() {
        return "Othello-"+ getBoardHeight();
    }

    @Override
    public int getStartingColor() {
        return BLACK;
    }

    @Override
    public int[] moveIndex(int symmetry, Move move) {
        OthelloMove othMove = (OthelloMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        if(othMove.isPass()) {
            moveIndices[0] = getBoardHeight() * getBoardWidth();
            return moveIndices;
        }
        int x;
        int y;
        switch(symmetry) {
            case 1:
                x= othMove.getX();
                y= othMove.getY();
                break;
            case 2:
                x= othMove.getY();
                y= othMove.getX();
                break;
            case 3:
                x= getBoardWidth()-othMove.getY()-1;
                y= getBoardHeight()-othMove.getX()-1;
                break;
            case 4:
                x= getBoardWidth()-othMove.getX()-1;
                y= getBoardHeight()-othMove.getY()-1;
                break;
            default:
                x=0;
                y=0;
                System.out.println("error in moveIndex");
        }
        moveIndices[0] = y* getBoardWidth() + x;
        return moveIndices;
    }

    @Override
    //TODO debug
    public Board newBoard(String board, boolean isFileName) {
        return new OthelloNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new OthelloNoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new OthelloEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 4;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
        result[0] = getBoardHeight() * getBoardWidth() +1;
        return result;
    }

    @Override
    public String toString() {
        return "Othello (board size " + getBoardWidth() + "x" + getBoardHeight() + ")";
    }

}
