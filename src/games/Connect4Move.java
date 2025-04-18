package games;

import ai.Game;
import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;

import static ai.Game.*;
import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

//note: the human-readable form of Connect4Move has y-coordinates starting at 1, like in chess notation. internally, they start at 0
public class Connect4Move implements Move, LocalizedMove {

    private int color;
    private int x;
    private int y;

    public Connect4Move(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Connect4Move(String input, int colorToPlay) throws MalformedMoveException {
        try {
            this.x = fileLetterToNumber.get(input.substring(0, 1));
            this.y = Integer.parseInt(input.substring(1, 2))-1;
            this.color = colorToPlay;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse coordinates of Connect 4 move", e);
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
        Connect4Move that = (Connect4Move) o;
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
        result += fileNumberToLetter.get(x) + (y+1) + "("+colorSymbol(color)+")";
        return result;
    }

}
