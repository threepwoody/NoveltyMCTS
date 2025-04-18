package games;

import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;

import static ai.Game.BLACK;
import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

//BLACK is vertical player, WHITE is horizontal player; the stored coordinates are the upper square for vertical, and the left square for horizontal moves.
public class DomineeringMove implements Move, LocalizedMove {

    private int color;
    private int x;
    private int y;

    public DomineeringMove(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public DomineeringMove(String input, int colorToPlay) throws MalformedMoveException {
        try {
            this.x = fileLetterToNumber.get(input.substring(0, 1));
            this.y = Integer.parseInt(input.substring(1, 2))-1;
            color = colorToPlay;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse coordinates of Domineering move", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomineeringMove that = (DomineeringMove) o;
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

    @Override
    public boolean isPass() {
        return false;
    }

    public String toString() {
        String result = "";
        if(color==BLACK) {
            result += fileNumberToLetter.get(x) + (y+1) + fileNumberToLetter.get(x) + y;
        } else {
            result += fileNumberToLetter.get(x) + (y+1) + fileNumberToLetter.get(x+1) + (y+1);
        }
        return result;
    }

}
