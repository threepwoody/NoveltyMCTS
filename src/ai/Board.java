package ai;

import utils.MalformedMoveException;

import java.util.List;
import java.util.Set;

public interface Board {

    void copyDataFrom(Board board);

    /*
     * Only defined if isTerminalBoard() has been called and evaluated to true, because then the game winners have
     * been determined.
     * Values have to be between 0 (loss) and 1 (win).
     */
    Evaluation evalOfTerminalBoard();

    int getColorToPlay();

    void setColorToPlay(int color);

    long getHash();

    void setHash(long newhash);

    int getHeight();

    List<Move> getLegalMoves();

    List<Move> getLegalMovesFor(int color);

    int getNumberOfColors();

    default int getNumberOfDifferentBoardElements() {
        return getNumberOfColors();
    }

    /*
     * returns the piece on the board at coordinates (x,y) (NO_PIECE if empty, OFF_BOARD if coordinates are out of bounds, otherwise a number from 1-n where n is the number of colors)
     */
    int getSquare(int x, int y);

    double[] getTerminalValues();

    int getTurn();

    void setTurn(int turn);

    int getWidth();

    int hashCode();

    boolean isLegalMove(Move move);

    /*
     * Currently has to set terminalValues, either directly or by specifying winners (setWinner, setWinners)
     */
    boolean isTerminalBoard();

    //default is for alternating move games
    default int nextColor() {
        return (getColorToPlay()+1)%getNumberOfColors();
    }

    void play(Move m);

    //default is for alternating move games
    default int previousColor() {
        if(getColorToPlay()==0) return getNumberOfColors()-1;
        else return getColorToPlay()-1;
    }

    void setTerminalValue(int color, double value);

    void setWinner(int winningColor);

    void setWinners(Set<Integer> winningColors);

    Move toMove(String moveString) throws MalformedMoveException;

}
