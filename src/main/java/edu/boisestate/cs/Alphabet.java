package edu.boisestate.cs;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Alphabet {

    private final Set<MinMaxPair> charRanges;
    private final Set<Character> symbolSet;
    private boolean rangeUpdateNeeded;

    public Set<Character> getSymbolSet() {
        return symbolSet;
    }

    public Alphabet() {

        // initialize fields
        this.symbolSet = new TreeSet<>();
        this.charRanges = new TreeSet<>();
        this.rangeUpdateNeeded = false;
    }

    public Alphabet(String symbolString) {
        this();

        this.addSymbols(symbolString);
    }

    Alphabet(Set<Character> symbolSet, Set<MinMaxPair> charRanges) {
        this.symbolSet = symbolSet;
        this.charRanges = charRanges;
    }

    private void addRangeToSymbolSet(char start, char end) {
        for (char c = start; c <= end; c++) {
            this.symbolSet.add(c);
        }
    }

    private void addSymbols(String string) {

        // detect commas character
        if (string.contains(",,")) {

            // detect single comma in symbol string
            if (string.contains(",,,")) {

                // add to symbol set
                this.symbolSet.add(',');

                // remove comma from string
                string = string.replace(",,,", ",");

            } else if (string.startsWith(",,") || string.endsWith(",,")) {

                // add to symbol set
                this.symbolSet.add(',');

                // remove comma from string
                string = string.replace(",,", "");

            }

            // detect commas as part of range in symbol string
            Pattern regex = Pattern.compile("^.*,?(,-.|.-,),?.*$");
            Matcher matcher = regex.matcher(string);
            if (matcher.matches()) {

                // add char range to symbol set
                char start = matcher.group(1).charAt(0);
                char end = matcher.group(1).charAt(2);
                this.addRangeToSymbolSet(start, end);

                // remove range from string
                string = string.replace(matcher.group(1), "");
            }
        }

        // split string
        String[] symbolDeclarations = string.split(",");

        // process symbol declarations
        for (String symbolDeclaration : symbolDeclarations) {

            // process single char declaration
            if (symbolDeclaration.length() == 1) {

                char symbol = symbolDeclaration.charAt(0);
                this.symbolSet.add(symbol);

            }
            // process character range declaration
            else if (symbolDeclaration.length() == 3 &&
                     symbolDeclaration.charAt(1) == '-') {

                char start = symbolDeclaration.charAt(0);
                char end = symbolDeclaration.charAt(2);
                this.addRangeToSymbolSet(start, end);

            }
        }

    }

    private void createCharRanges() {

        // initialize return set
        this.charRanges.clear();

        char prev = 0;
        char min = 0;
        for (char c : symbolSet) {

            // if starting new min max pair
            if (Character.compare(c, prev) != 1) {

                // ignore first iteration
                if (prev != 0) {

                    // create pair and add to set
                    MinMaxPair pair = new MinMaxPair(min, prev);
                    this.charRanges.add(pair);
                }

                // set new min value in range
                min = c;
            }

            // save current char for use in next iteration
            prev = c;
        }

        // add final pair
        MinMaxPair pair = new MinMaxPair(min, prev);
        this.charRanges.add(pair);
    }
}
