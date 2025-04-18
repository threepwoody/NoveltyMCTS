package games;

import ai.BasicRectangularBoardGame;
import ai.Board;
import ai.Move;
import ai.terminalboardevaluation.MaxLegalMovesGame;
import ai.terminalboardevaluation.MobilityTerminalBoardEvaluator;
import experiments.AlphaZero.AlphaZeroGame;
import utils.UnknownPropertyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class NoGo extends BasicRectangularBoardGame implements AlphaZeroGame, MaxLegalMovesGame {

    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private static long[][][] zobristKeys;

    public NoGo(int boardSize) {
        super(2, boardSize, boardSize);
    }

    public NoGo() {
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
        return "NoGo-"+ getBoardHeight();
    }

    @Override
    public int getStartingColor() {
        return BLACK;
    }

    @Override
    public int maxLegalMovesPerPosition() {
        return getBoardWidth() * getBoardHeight();
    }

    @Override
    public int[] moveIndex(int symmetry, Move move) {
        NoGoMove ngMove = (NoGoMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int x;
        int y;
        switch(symmetry) {
            case 1:
                x= ngMove.getX();
                y= ngMove.getY();
                break;
            case 2:
                x= ngMove.getX();
                y= getBoardHeight()-ngMove.getY()-1;
                break;
            case 3:
                x= getBoardWidth()-ngMove.getX()-1;
                y= ngMove.getY();
                break;
            case 4:
                x= ngMove.getY();
                y= ngMove.getX();
                break;
            case 5:
                x= getBoardWidth()-ngMove.getY()-1;
                y= getBoardHeight()-ngMove.getX()-1;
                break;
            case 6:
                x= getBoardWidth()-ngMove.getX()-1;
                y= getBoardHeight()-ngMove.getY()-1;
                break;
            case 7:
                x= getBoardWidth()-ngMove.getY()-1;
                y= ngMove.getX();
                break;
            case 8:
                x= ngMove.getY();
                y= getBoardHeight()-ngMove.getX()-1;
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
        return new NoGoNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new NoGoNoTranspositionsBoard(this);
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
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("m")) {
            MobilityTerminalBoardEvaluator newEvaluator = new MobilityTerminalBoardEvaluator(getNumberOfColors(), maxLegalMovesPerPosition());
            setTerminalBoardEvaluator(newEvaluator);
            addProperty("m");
        } else {
            super.setProperty(property, value);
        }
    }

    @Override
    public String toString() {
        return "NoGo (board size " + getBoardWidth() + "x" + getBoardHeight() + ")";
    }

}
