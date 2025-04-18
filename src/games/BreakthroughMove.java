package games;

import ai.LocalizedMove;
import ai.Move;
import utils.MalformedMoveException;

import java.io.Serializable;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

//note: the human-readable form of BreakthroughMove has y-coordinates starting at 1, like in chess notation. internally, they start at 0
public class BreakthroughMove implements Move, LocalizedMove, Serializable {

    private static final long serialVersionUID = 7526472222776147L;
    private boolean capture;
    private int fromX;
    private int fromY;
    //0 for left, 1 for left capture, 2 for straight, 3 for right, 4 for right capture
    private int moveType;
    private int toX;
    private int toY;

    public BreakthroughMove(int fromX, int fromY, int toX, int toY, boolean capture) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.capture = capture;
        moveType = determineMoveType(fromX, fromY, toX, toY, capture);
    }

    public BreakthroughMove(String input) throws MalformedMoveException {
        try {
            this.fromX = fileLetterToNumber.get(input.substring(0, 1));
            this.fromY = Integer.parseInt(input.substring(1, 2))-1;
            this.toX = fileLetterToNumber.get(input.substring(3, 4));
            this.toY = Integer.parseInt(input.substring(4, 5))-1;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse coordinates of Breakthrough move", e);
        }
        if(input.substring(2,3).equals("x")) {
            capture = true;
        } else if(input.substring(2,3).equals("-")) {
            capture = false;
        } else {
            throw new MalformedMoveException("Could not parse whether Breakthrough move is a capture (x) or not (-)");
        }
        moveType = determineMoveType(fromX, fromY, toX, toY, capture);
    }

    private static int determineMoveType(int fromX, int fromY, int toX, int toY, boolean capture) {
        if(fromX==toX) { //straight
            return 2;
        } else if((toX<fromX && toY>fromY)||(toX>fromX && toY<fromY)) { //left
            if(capture) {
                return 1;
            } else {
                return 0;
            }
        } else { //right
            if(capture) {
                return 4;
            } else {
                return 3;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BreakthroughMove that = (BreakthroughMove) o;
        return capture == that.capture &&
                fromX == that.fromX &&
                fromY == that.fromY &&
                toX == that.toX &&
                toY == that.toY &&
                moveType == that.moveType;
    }

    @Override
    public int getColorOfMove() {
        if(toY>fromY) return WHITE;
        return BLACK;
    }

    @Override
    public boolean isPass() {
        return false;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getMoveType() {
        return moveType;
    }

    public int getToX() { return toX; }

    public int getToY() {
        return toY;
    }

    public int getX() {
        return toX;
    }

    public int getY() {
        return toY;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + fromX;
        hash = hash * 31 + fromY;
        hash = hash * 31 + toX;
        hash = hash * 31 + toY;
        hash = hash * 31 + moveType;
//        if(capture) hash = hash*7;
        return hash;
    }

    public boolean isCapture() {
        return capture;
    }

    public String toString() {
        String result = "";
        String separator = capture ? "x" : "-";
        result += fileNumberToLetter.get(fromX) + (fromY+1) + separator + fileNumberToLetter.get(toX) + (toY+1);
        return result;
    }

}
