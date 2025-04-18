package utils;

/**
 * Set of Zobrist hashes (longs) from previous board positions. This is a hash
 * table, but without all of the overhead of java.util.HashSet. It only supports
 * insertion, search, and copying. Collisions are resolved by linear probing.
 */
public class GoSuperKoTable {

    /** Special value for an empty slot in the table. */
    public static final long EMPTY = 0;

    /**
     * Bit mask to make hash codes positive. Math.abs() won't work because
     * abs(Integer.minValue()) < 0.
     */
    public static final int IGNORE_SIGN_BIT = 0x7fffffff;

    /**
     * True if the special value 0 has been stored. Because this value is used
     * to mark an empty slot, we can't look it up in the usual way. Instead, we
     * just check this field.
     */
    private boolean containsZero;

    /** The table proper. */
    private long[] data;

    public GoSuperKoTable(int size) {
        data = new long[size];
    }

    /** Adds key to this table. */
    public void add(long key) {
        if (key == 0) {
            containsZero = true;
        } else {
            int slot = (((int) key) & IGNORE_SIGN_BIT) % data.length;
            while (data[slot] != EMPTY) {
                if (data[slot] == key) {
                    return;
                }
                slot = (slot + 1) % data.length;
            }
            data[slot] = key;
        }
    }

    /** Returns true if key is in this table. */
    public boolean contains(long key) {
        if (key == 0) {
            return containsZero;
        } else {
            int slot = (((int) key) & IGNORE_SIGN_BIT) % data.length;
            while (data[slot] != EMPTY) {
                if (data[slot] == key) {
                    return true;
                }
                slot = (slot + 1) % data.length;
            }
            return false;
        }
    }

    /**
     * Makes this into a copy of that, without the overhead of creating a new
     * object.
     */
    public void copyDataFrom(GoSuperKoTable that) {
        System.arraycopy(that.data, 0, data, 0, data.length);
        containsZero = that.containsZero;
    }

}

