package games;

import ai.Move;
import utils.MalformedMoveException;

import java.util.Scanner;

import static utils.Util.fileLetterToNumber;
import static utils.Util.fileNumberToLetter;

public class NeoCrossingsMove implements Move {

    private int color;
    private int fromX;
    private int fromY;
    //0 for up, 1 for up right, 2 for right, 3 for down right, 4 for down, 5 for down left, 6 for left, 7 for up left
    private int pushDirection;
    private int pushDistance;

    public NeoCrossingsMove(int fromX, int fromY, int direction, int distance, int color) {
        this.fromX = fromX;
        this.fromY = fromY;
        pushDirection = direction;
        pushDistance = distance;
        this.color = color;
    }

    public NeoCrossingsMove(String input, int color) throws MalformedMoveException {
        try {
            Scanner s = new Scanner(input).useDelimiter("/");
            String x = s.next();
            this.fromX = fileLetterToNumber.get(x.substring(0, 1));
            this.fromY = Integer.parseInt(input.substring(1))-1;
            String dir = s.next();
            this.pushDirection = Integer.parseInt(dir);
            String dis = s.next();
            this.pushDistance = Integer.parseInt(dis);
            this.color = color;
        } catch (Exception e) {
            throw new MalformedMoveException("Could not parse NeoCrossings move", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeoCrossingsMove that = (NeoCrossingsMove) o;
        return fromX == that.fromX &&
                fromY == that.fromY &&
                pushDistance == that.pushDistance &&
                pushDirection == that.pushDirection &&
                color == that.color;
    }

    @Override
    public int getColorOfMove() {
        return color;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getPushDirection() {
        return pushDirection;
    }

    public int getPushDistance() {
        return pushDistance;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + fromX;
        hash = hash * 31 + fromY;
        hash = hash * 31 + pushDistance;
        hash = hash * 31 + pushDirection;
        hash = hash * 31 + color;
        return hash;
    }

    @Override
    public boolean isPass() {
        return false;
    }

    public String toString() {
        String result = "";
        String separator = "/";
        result += fileNumberToLetter.get(fromX) + (fromY+1) + separator + pushDirection + separator + pushDistance;
        return result;
    }

}
