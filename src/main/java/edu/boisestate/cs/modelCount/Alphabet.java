package edu.boisestate.cs.modelCount;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Alphabet {

    private final Set<Character> symbolSet;

    public Alphabet() {

        // initialize fields
        this.symbolSet = new TreeSet<>();
    }

    public Alphabet(Collection<Character> symbolSet) {
        this();

        // add all symbols to symbol set
        this.symbolSet.addAll(symbolSet);
    }

    public Alphabet(char[] symbolArray) {
        this();

        // add all symbols to symbol set
        for (char symbol : symbolArray) {
            this.symbolSet.add(symbol);
        }
    }

    public Alphabet(String symbolString) {
        this(symbolString.toCharArray());
    }

    public String getCharSetString() {
        StringBuilder sb = new StringBuilder();
        for (char c : this.symbolSet) {
            sb.append(c);
        }
        return sb.toString();
    }

    public Set<Character> getSymbolSet() {
        return symbolSet;
    }

    public Set<MinMaxPair> getCharRanges() {

        // initialize return set
        Set<MinMaxPair> rangeSet = new TreeSet<>();

        char prev = 0;
        char min = 0;
        for (char c : symbolSet) {

            // if starting new min max pair
            if (Character.compare(c, prev) != 1) {

                // ignore first iteration
                if (prev != 0) {

                    // create pair and add to set
                    MinMaxPair pair = new MinMaxPair(min, prev);
                    rangeSet.add(pair);
                }

                // set new min value in range
                min = c;
            }

            // save current char for use in next iteration
            prev = c;
        }

        // add final pair
        MinMaxPair pair = new MinMaxPair(min, prev);
        rangeSet.add(pair);

        // return set of char ranges
        return rangeSet;
    }
}
