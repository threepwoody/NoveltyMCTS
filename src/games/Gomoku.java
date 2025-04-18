package games;

import ai.BasicRectangularBoardGame;
import ai.Board;
import ai.Move;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class Gomoku extends BasicRectangularBoardGame implements AlphaZeroGame {

    //first dimension: all types of 4 in a row that one square can potentially secure
    //second dimension: all other squares that would have to be taken by the same player for that move to win
    //third dimension: x,y offsets for that square, relative to original move
    //always starting from squares with biggest offsets, because that leads to quicker rejections if OFF_BOARD
    public static final int[][][] FIVE_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE = {
            //horizontals
            {{-4,0},{-3,0},{-2,0},{-1,0}},
            {{-3,0},{1,0},{-2,0},{-1,0}},
            {{-2,0},{2,0},{-1,0},{1,0}},
            {{3,0},{-1,0},{1,0},{2,0}},
            {{4,0},{1,0},{2,0},{3,0}},
            //vertical
            {{0,-4},{0,-3},{0,-2},{0,-1}},
            {{0,-3},{0,1},{0,-2},{0,-1}},
            {{0,-2},{0,2},{0,-1},{0,1}},
            {{0,3},{0,-1},{0,1},{0,2}},
            {{0,4},{0,1},{0,2},{0,3}},
            //diagonal to top right
            {{-4,-4},{-3,-3},{-2,-2},{-1,-1}},
            {{-3,-3},{1,1},{-2,-2},{-1,-1}},
            {{-2,-2},{2,2},{1,1},{-1,-1}},
            {{3,3},{-1,-1},{2,2},{1,1}},
            {{4,4},{3,3},{2,2},{1,1}},
            //diagonal to top left
            {{4,-4},{3,-3},{2,-2},{1,-1}},
            {{3,-3},{-1,1},{2,-2},{1,-1}},
            {{2,-2},{-2,2},{1,-1},{-1,1}},
            {{-3,3},{1,-1},{-2,2},{-1,1}},
            {{-4,4},{-3,3},{-2,2},{-1,1}},
    };
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private static long[][][] zobristKeys;

    public Gomoku(int boardWidth) {
        super(2, boardWidth, boardWidth);
    }

    public Gomoku() {
        super(2, 15, 15);
    }

    private static int getLargestSupportedBoardWidth() {
        return 15;
    }

    private static int getMaxTurnsPerMatchForLargestSupportedBoard() {
        return 225;
    }

    public static long[][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        zobristKeys = new long[getMaxTurnsPerMatchForLargestSupportedBoard()][getLargestSupportedBoardWidth()][getLargestSupportedBoardWidth()];
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
                    for(int y=0;y<getLargestSupportedBoardWidth();y++) {
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
        return "Gomoku-"+ getBoardWidth();
    }

    @Override
    public int getStartingColor() {
        return BLACK;
    }

    @Override
    public int[] moveIndex(int symmetry, Move m) {
        GomokuMove gMove = (GomokuMove) m;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int x;
        int y;
        switch(symmetry) {
            case 1:
                x= gMove.getX();
                y= gMove.getY();
                break;
            case 2:
                x= gMove.getX();
                y= getBoardHeight()-gMove.getY()-1;
                break;
            case 3:
                x= getBoardWidth()-gMove.getX()-1;
                y= gMove.getY();
                break;
            case 4:
                x= gMove.getY();
                y= gMove.getX();
                break;
            case 5:
                x= getBoardWidth()-gMove.getY()-1;
                y= getBoardHeight()-gMove.getX()-1;
                break;
            case 6:
                x= getBoardWidth()-gMove.getX()-1;
                y= getBoardHeight()-gMove.getY()-1;
                break;
            case 7:
                x= getBoardWidth()-gMove.getY()-1;
                y= gMove.getX();
                break;
            case 8:
                x= gMove.getY();
                y= getBoardHeight()-gMove.getX()-1;
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
    public Board newBoard(String board, boolean isFileName) {
        return new GomokuNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new GomokuNoTranspositionsBoard(this);
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 8; //TODO 8, if not too expensive... otherwise 1
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
        result[0] = getBoardWidth()*getBoardHeight();
        return result;
    }

    @Override
    public String toString() {
        return "Gomoku";
    }

}
