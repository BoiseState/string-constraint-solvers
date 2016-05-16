package edu.boisestate.cs;

public class MinMaxPair
        implements Comparable<MinMaxPair> {
    private final char max;
    private final char min;

    public MinMaxPair(char min, char max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int compareTo(MinMaxPair pair) {

        // get difference between minimums
        int minDiff = this.min - pair.getMin();

        if (minDiff != 0) {
            return minDiff;
        }

        return this.max - pair.getMax();
    }

    public char getMax() {
        return max;
    }

    public char getMin() {
        return min;
    }
}
