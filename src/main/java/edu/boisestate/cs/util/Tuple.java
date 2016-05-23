package edu.boisestate.cs.util;

/**
 *
 */
public class Tuple<TElement1, TElement2> {

    private final TElement1 first;
    private final TElement2 second;

    public TElement1 getFirst() {
        return first;
    }

    public TElement2 getSecond() {
        return second;
    }

    public Tuple(TElement1 first, TElement2 second) {
        this.first = first;
        this.second = second;
    }
}
