package games;

import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;

import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

public class AtariGoMove implements Move, LocalizedMove {

    private int color;
    private int x;
    private int y;

    public AtariGoMove(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public AtariGoMove(String input, int colorToMove) throws MalformedMoveException {
        try {
            this.x = fileLetterToNumber.get(input.substring(0, 1));
            this.y = Integer.parseInt(input.substring(1, 2))-1;
            color = colorToMove;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse coordinates of AtariGo move", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtariGoMove that = (AtariGoMove) o;
        return x == that.x &&
                y == that.y &&
                color == that.color;
    }

    @Override
    public int getColorOfMove() {
        return color;
    }

    @Override
    public boolean isPass() {
        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + color;
        return hash;
    }

    public String toString() {
        String result = "";
        result += fileNumberToLetter.get(x) + (y+1);
        return result;
    }

}
