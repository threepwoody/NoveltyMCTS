package games;

import ai.Game;
import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

public class GomokuMove implements Move, LocalizedMove {

    private int color;
    private int x;
    private int y;

    public GomokuMove(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public GomokuMove(String input, int colorToPlay) throws MalformedMoveException {
        try {
            this.x = Integer.parseInt(input.substring(0, 1));
            this.y = Integer.parseInt(input.substring(2, 3));
            this.color = colorToPlay;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse coordinates of Gomoku move", e);
        }
    }

    private String colorSymbol(int color) {
        if(color==BLACK) return "X";
        if(color==WHITE) return "O";
        if(color== Game.NO_COLOR) return "-";
        return "error";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GomokuMove that = (GomokuMove) o;
        return x == that.x &&
                y == that.y &&
                color == that.color;
    }

    @Override
    public int getColorOfMove() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() { return y; }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + color;
        return hash;
    }

    @Override
    public boolean isPass() {
        return false;
    }

    public String toString() {
        String result = "";
        result += x + "/" + y + "("+colorSymbol(color)+")";
        return result;
    }

}
