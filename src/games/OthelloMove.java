package games;

import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;
import utils.Util;

//note: the human-readable form of OthelloMove has y-coordinates starting at 1, like in chess notation. internally, they start at 0
public class OthelloMove implements Move, LocalizedMove {

    private final int color;
    private final boolean pass;
    private final int x;
    private final int y;

    public OthelloMove(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.pass = false;
    }

    public OthelloMove(String input, int colorToPlay) throws MalformedMoveException {
        if(input==null || input.length()==0) {
            this.pass = true;
            color = 0;
            x = 0;
            y = 0;
        } else {
            try {
                this.x = Util.fileLetterToNumber.get(input.substring(0, 1));
                this.y = Integer.parseInt(input.substring(1, 2)) - 1;
                this.color = colorToPlay;
                this.pass = false;
            } catch (Exception e) {
                throw new MalformedMoveException("Could not parse coordinates of Othello move", e);
            }
        }
    }

    public OthelloMove() {
        this.pass = true;
        color = 0;
        x = 0;
        y = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OthelloMove that = (OthelloMove) o;
        return x == that.x &&
                y == that.y &&
                color == that.color &&
                pass == that.pass;
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
        if(pass) hash = hash*7;
        return hash;
    }

    @Override
    public boolean isPass() { return pass; }

    public String toString() {
        String result = "";
        if(pass) {
            result += "pass";
        } else {
            result += Util.fileNumberToLetter.get(x) + (y + 1);
        }
        return result;
    }

}
