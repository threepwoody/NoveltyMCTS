package games;

import ai.BasicRectangularBoardGame;
import ai.Board;
import ai.Move;
import ai.evaluation.StaticEvaluator;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class AtariGo extends BasicRectangularBoardGame implements AlphaZeroGame, StaticEvaluatorGame {

    public static final int[][] KNIGHT_NEIGHBORHOOD_OFFSETS = {
            {-2,-1}, {-2,0}, {-2,1}, {-1,-2}, {-1,-1}, {-1,0}, {-1,1}, {-1,2}, {0,-2}, {0,-1}, {0,1}, {0,2}, {1,-2}, {1,-1}, {1,0}, {1,1}, {1,2}, {2,-1}, {2,0}, {2,1},
    };
    public static final int[][] SMALL_NEIGHBORHOOD_OFFSETS = {
            {-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1},
    };
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible
    //dimensions to look up keys: [turn][x][y]
    private static long[][][] zobristKeys;

    public AtariGo(int boardSize) {
        super(2, boardSize, boardSize);
    }

    public AtariGo() {
        super(2, 9, 9);
    }

    private static int getLargestSupportedBoardHeight() {
        return 9;
    }

    private static int getLargestSupportedBoardWidth() {
        return 9;
    }

    private static int getMaxTurnsPerMatchForLargestSupportedBoard() {
        return 81;
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
    public String getNameWithoutProperties() {
        return "AtariGo-"+ getBoardWidth();
    }

    @Override
    public int getStartingColor() {
        return BLACK;
    }

    @Override
    public int[] moveIndex(int symmetry, Move move) {
        AtariGoMove agMove = (AtariGoMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int x;
        int y;
        switch(symmetry) {
            case 1:
                x= agMove.getX();
                y= agMove.getY();
                break;
            case 2:
                x= agMove.getX();
                y= getBoardHeight()-agMove.getY()-1;
                break;
            case 3:
                x= getBoardWidth()-agMove.getX()-1;
                y= agMove.getY();
                break;
            case 4:
                x= agMove.getY();
                y= agMove.getX();
                break;
            case 5:
                x= getBoardWidth()-agMove.getY()-1;
                y= getBoardHeight()-agMove.getX()-1;
                break;
            case 6:
                x= getBoardWidth()-agMove.getX()-1;
                y= getBoardHeight()-agMove.getY()-1;
                break;
            case 7:
                x= getBoardWidth()-agMove.getY()-1;
                y= agMove.getX();
                break;
            case 8:
                x= agMove.getY();
                y= getBoardHeight()-agMove.getX()-1;
                break;
            default:
                x=0;
                y=0;
                System.out.println("error in moveIndex");
        }
        moveIndices[0] = y*getBoardWidth() + x;
        return moveIndices;
    }

    @Override
    //TODO debug
    public Board newBoard(String board, boolean isFileName) {
        return new AtariGoNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new AtariGoNoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new AtariGoEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 8;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
        result[0] = getBoardHeight() * getBoardWidth();
        return result;
    }

    @Override
    public String toString() {
        return "AtariGo";
    }

}
