package games;

import ai.*;
import ai.evaluation.StaticEvaluator;
import ai.terminalboardevaluation.MaxLegalMovesGame;
import ai.terminalboardevaluation.MobilityTerminalBoardEvaluator;
import experiments.AlphaZero.AlphaZeroGame;
import utils.UnknownPropertyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static utils.Util.charArrayToZobristKey;

public class Breakthrough extends BasicRectangularBoardGame implements MoveSortingGame, StaticEvaluatorGame, AlphaZeroGame, MaxLegalMovesGame {

    private static final MoveSorter moveSorter = new BreakthroughMoveSorter();
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][fromX][fromY][movetype 0-4]
    private static long[][][][] zobristKeys;
    private final int rowsOfPieces = 2;

    public Breakthrough(int boardSize) {
        super(2, boardSize, boardSize);
    }

    public Breakthrough() {
        super(2, 8, 8);
    }

    private static int getLargestSupportedBoardHeight() {
        return 8;
    }

    private static int getLargestSupportedBoardWidth() {
        return 8;
    }

    private static int getMaxMovesPerMatchForLargestSupportedBoard() {
        return 256;
    }

    public static long[][][][] getZobristKeys() {
        if(zobristKeys==null) {
            readInZobristKeys();
        }
        return zobristKeys;
    }

    static void readInZobristKeys() {
        //assumes that 8x8 boards are the largest we will use - otherwise adapt here!
        zobristKeys = new long[getMaxMovesPerMatchForLargestSupportedBoard()][getLargestSupportedBoardWidth()][getLargestSupportedBoardHeight()][5];
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
            for(int timestep = 0; timestep< getMaxMovesPerMatchForLargestSupportedBoard(); timestep++) {
                for(int x=0;x<getLargestSupportedBoardWidth();x++) {
                    for(int y=0;y<getLargestSupportedBoardHeight();y++) {
                        for(int moveType=0;moveType<5;moveType++) {
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
        return getBoardWidth() *rowsOfPieces*2*(getBoardHeight() -2)+1;
    }

    @Override
    public MoveSorter getMoveSorter() {
        return moveSorter;
    }

    @Override
    public String getNameWithoutProperties() {
        return "Breakthrough-"+getBoardHeight();
    }

    public int getRowsOfPieces() {
        return rowsOfPieces;
    }

    @Override
    public int maxLegalMovesPerPosition() {
        return getBoardWidth()*getRowsOfPieces()*3;
    }

    @Override
    public int[] moveIndex(int symmetry, Move move) {
        BreakthroughMove btMove = (BreakthroughMove) move;
        int[] moveIndices = new int[numberOfMoveDimensions()];
        int compressedMoveType;
        switch(btMove.getMoveType()) {
            case 0:
            case 1:
                compressedMoveType = 0;
                break;
            case 2:
                compressedMoveType = 1;
                break;
            case 3:
            case 4:
                compressedMoveType = 2;
                break;
            default:
                compressedMoveType = 0;
                System.out.println("error in moveIndex");
        }
        int x;
        int y = btMove.getFromY();
        if(symmetry==1) {
            x = btMove.getFromX();
        } else {
            x = getBoardWidth()-btMove.getFromX()-1;
            switch(compressedMoveType) {
                case 0:
                    compressedMoveType = 2;
                    break;
                case 1:
                    break;
                case 2:
                    compressedMoveType = 0;
                    break;
                default:
                    System.out.println("error in moveIndex");
            }
        }
        moveIndices[0] = y* getBoardWidth() *3 + x*3 + compressedMoveType;
        return moveIndices;
    }

    @Override
    public Board newBoard(String board, boolean isFileName) {
        return new BreakthroughNoTranspositionsBoard(this, board, isFileName);
    }

    @Override
    public Board newBoard() {
        return new BreakthroughNoTranspositionsBoard(this);
    }

    @Override
    public StaticEvaluator newStaticEvaluator() {
        return new BreakthroughEvaluator();
    }

    @Override
    public int numberOfBoardSymmetries() {
        return 2;
    }

    @Override
    public int[] numberOfMoveCSVIndicesPerDimension() {
        int[] result = new int[1];
        result[0] = getBoardHeight() * getBoardWidth() *3;
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
        return "Breakthrough";
    }

}
