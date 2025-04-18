package ai;

import utils.MalformedMoveException;

import java.util.*;

public abstract class BasicBoard implements Board {

    public static final int NO_PIECE = 0;
    public static final int OFF_BOARD = -1;
    private int colorToPlay;
    /** Hash for the current position. */
    private long hash;
    private Map<Integer, List<Move>> legalMovesPerColor;
    private int numberOfColors;
    private int numberOfDifferentBoardElements;
    private TerminalBoardEvaluator terminalBoardEvaluator;
    private double[] terminalValues;
    /** The current turn (starting from zero.) */
    private int turn;

    public BasicBoard(BasicGame game) {
        setTurn(0);
        this.numberOfColors = game.getNumberOfColors();
        this.numberOfDifferentBoardElements = game.getNumberOfDifferentBoardElements();
        terminalValues = new double[numberOfColors];
        legalMovesPerColor = new HashMap<>();
        for(int color=0; color<numberOfColors; color++) {
            legalMovesPerColor.put(color, new ArrayList<>());
        }
        this.terminalBoardEvaluator = game.newTerminalBoardEvaluator();
    }

    /**
     * Copies all data from that into this. Similar to cloning that, but without
     * the overhead of creating a new object.
     */
    @Override
    public void copyDataFrom(Board thatBoard) {
        BasicBoard that = (BasicBoard) thatBoard;
        hash = that.hash;
        turn = that.turn;
        colorToPlay = that.colorToPlay;
        System.arraycopy(that.terminalValues, 0, terminalValues, 0, terminalValues.length);
        for(int color=0; color<numberOfColors; color++) {
            getLegalMovesFor(color).clear();
            getLegalMovesFor(color).addAll(that.getLegalMovesFor(color));
        }
        terminalBoardEvaluator = that.terminalBoardEvaluator.copy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicBoard that = (BasicBoard) o;
        return colorToPlay== that.colorToPlay &&
                hash == that.hash &&
                numberOfColors == that.numberOfColors &&
                turn == that.turn &&
                Arrays.equals(terminalValues, that.terminalValues) &&
                terminalBoardEvaluator.equals(that.terminalBoardEvaluator);
    }

    /*
     * Only defined if isTerminalBoard() has been called and evaluated to true, because then the game winners have
     * been determined.
     * Values have to be between 0 (loss) and 1 (win).
     */
    public Evaluation evalOfTerminalBoard() {
        return terminalBoardEvaluator.evalOfTerminalBoard(this);
    }

    public int getColorToPlay() { return colorToPlay; }

    public void setColorToPlay(int color) {
        colorToPlay = color;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long newhash) {
        hash = newhash;
    }

    abstract public int getHeight();

    //can calculate the legal moves each time it is called,
    //or just return a set of legal moves that is kept up to date incrementally when making/undoing moves
    public List<Move> getLegalMoves() {
        return legalMovesPerColor.get(getColorToPlay());
    }

    public List<Move> getLegalMovesFor(int color) {
        return legalMovesPerColor.get(color);
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    @Override
    public int getNumberOfDifferentBoardElements() {
        return numberOfDifferentBoardElements;
    }

    /*
     * returns the piece on the board at coordinates (x,y) (NO_PIECE if empty, OFF_BOARD if coordinates are out of bounds, otherwise a number from 1-n where n is the number of colors)
     */
    abstract public int getSquare(int x, int y);

    @Override
    public double[] getTerminalValues() {
        return terminalValues;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    abstract public int getWidth();

    @Override
    public int hashCode() {
        int result = Objects.hash(colorToPlay, hash, numberOfColors, turn);
        return result;
    }

    public boolean isLegalMove(Move move) {
        return getLegalMoves().contains(move);
    }

    abstract public boolean isTerminalBoard();

    abstract public void play(Move m);

    @Override
    public void setTerminalValue(int color, double value) {
        terminalValues[color] = value;
    }

    @Override
    public void setWinner(int winningColor) {
        for(int color = 0; color < getNumberOfColors(); color++) {
            if(color==winningColor) {
                setTerminalValue(color, 1);
            } else {
                setTerminalValue(color, 0);
            }
        }
    }

    @Override
    public void setWinners(Set<Integer> winningColors) {
        for(int color = 0; color < getNumberOfColors(); color++) {
            if(winningColors.contains(color)) {
                setTerminalValue(color, 1.0/winningColors.size());
            } else {
                setTerminalValue(color, 0);
            }
        }
    }

    abstract public Move toMove(String moveString) throws MalformedMoveException;

};



