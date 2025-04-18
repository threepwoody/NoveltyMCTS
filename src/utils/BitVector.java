package utils;

import java.io.Serializable;

import static java.util.Arrays.fill;

/**
 * Equivalent to an array of booleans, but much smaller. Handy for representing
 * a set when insertion, deletion, clearing, and search are the only operations;
 * the universe is small; and space is of the essence. If it is necessary to
 * quickly compute the size of the set, or to traverse the elements, or if the
 * set is very sparse, IntSet may be preferable.
 * @see IntSet
 */
public class BitVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The bits themselves. */
    private long[] data;

    /** Elements must be in [0, capacity). */
    public BitVector(int capacity) {
        if(Util.isDebug()) System.out.println("creating bitvector with capacity of "+capacity);
        int longs = capacity / 64;
        if (longs * 64 < capacity) {
            longs++;
        }
        data = new long[longs];
    }

    /** Removes all elements from this set. */
    public void clear() {
        fill(data, 0L);
    }

    /** Returns true if i is in this set. */
    public boolean get(int i) {
        try {
            return (data[i / 64] & (1L << (i % 64))) != 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("trying to access "+i);
            System.out.println("leading to index "+(i/64));
            System.out.println("data length: "+data.length);
            throw(e);
        }
    }

    /** Sets whether i is in this set. */
    public void set(int i, boolean value) {
        if (value) {
            data[i / 64] |= (1L << (i % 64));
        } else {
            data[i / 64] &= ~(1L << (i % 64));
        }
    }

}

