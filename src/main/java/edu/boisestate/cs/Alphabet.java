package edu.boisestate.cs;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Alphabet {

    private final Set<MinMaxPair> charRanges;
    private final Set<Character> symbolSet;

    public Set<MinMaxPair> getCharRanges() {

        // lazy access pattern
        if (charRanges.size() > 0) {
            return charRanges;
        }

        this.createCharRanges();
        return charRanges;
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

    public Alphabet(String symbolString) {
        this();

        // get set of symbol from string declaration
        Set<Character> processedSymbolSet =
                getSymbolsFromDeclaration(symbolString);

        // load all symbols from string into symbol set
        this.symbolSet.addAll(processedSymbolSet);
    }

    public Alphabet() {

        // initialize fields
        this.symbolSet = new TreeSet<>();
        this.charRanges = new TreeSet<>();
    }

    private static Set<Character> getSymbolsFromDeclaration(String string) {

        // initialize symbol set to empty tree set for automatic sorting
        Set<Character> symbolSet = new TreeSet<>();

        // detect commas character
        if (string.contains(",,")) {

            // detect single comma in symbol string
            if (string.contains(",,,")) {

                // add to symbol set
                symbolSet.add(',');

                // remove comma from string
                string = string.replace(",,,", ",");

            } else if (string.startsWith(",,") || string.endsWith(",,")) {

                // add to symbol set
                symbolSet.add(',');

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
                addRangeToSymbolSet(symbolSet, start, end);

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
                symbolSet.add(symbol);

            }
            // process character range declaration
            else if (symbolDeclaration.length() == 3 &&
                     symbolDeclaration.charAt(1) == '-') {

                char start = symbolDeclaration.charAt(0);
                char end = symbolDeclaration.charAt(2);
                addRangeToSymbolSet(symbolSet, start, end);

            }
        }

        // return symbol set
        return symbolSet;
    }

    private static void addRangeToSymbolSet(Set<Character> symbolSet,
                                            char start,
                                            char end) {
        for (char c = start; c <= end; c++) {
            symbolSet.add(c);
        }
    }

    Alphabet(Set<Character> symbolSet, Set<MinMaxPair> charRanges) {
        this.symbolSet = symbolSet;
        this.charRanges = charRanges;
    }

    public boolean isSuperset(String minAlphabetDeclaration) {

        // get all symbols from minimum alphabet declaration
        Set<Character> minSet = getSymbolsFromDeclaration(minAlphabetDeclaration);

        // return if all symbols from min set are in alphabet
        return this.symbolSet.containsAll(minSet);
    }

    public Set<Character> getSymbolSet() {
        return symbolSet;
    }

    public int size() {
        return this.symbolSet.size();
    }
}
