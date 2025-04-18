package ai;

public final class Coordinate {

    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate add(Coordinate other) {
        return new Coordinate(x+other.x, y+other.y);
    }

    public Coordinate neighborForCornerType(int direction) {
        switch(direction) {
            case 1:
                return new Coordinate(x+1, y+1);
            case 2:
                return new Coordinate(x+1, y-1);
            case 3:
                return new Coordinate(x-1, y-1);
            case 4:
                return new Coordinate(x-1, y+1);
        }
        return null;
    }

    public Coordinate subtract(Coordinate other) {
        return new Coordinate(x-other.x, y-other.y);
    }

    @Override
    public String toString() {
        return "("+x+","+y+")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x &&
                y == that.y;
    }

}
