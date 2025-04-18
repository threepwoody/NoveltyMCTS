package utils;

public class Pair<C> {

    public final C first, second;

    public Pair(C first, C second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash = hash * 31 + first.hashCode();
        hash = hash * 31 + second.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair that = (Pair) o;
        return first.equals(that.first) &&
                second.equals(that.second);
    }

    @Override
    public String toString() {
        return "["+first+","+second+"]";
    }

}
