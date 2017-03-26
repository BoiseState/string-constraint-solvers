package edu.boisestate.cs;

import dk.brics.automaton.Automaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Alphabet {

    static private Alphabet instance = null;

    static public void setInstance(Alphabet alphabet) {
        instance = alphabet;
    }

    static public Alphabet getInstance() {
        return instance;
    }

    private final Set<MinMaxPair> charRanges;
    private final Set<Character> symbolSet;
    private final Random random;

    public Set<MinMaxPair> getCharRanges() {

        // lazy access pattern
        if (charRanges.size() > 0) {
            return charRanges;
        }

        this.createCharRanges();
        return charRanges;
    }

    public List<String> allStrings(int minLength, int maxLength) {

        // initialize return list of strings
        List<String> returnList = new ArrayList<>();

        // initialize string tracking list with empty string element
        List<String> strList = new ArrayList<>();
        strList.add("");

        // add empty string to string domain if min is 0
        if (minLength == 0) {
            returnList.add("");
        }

        // for strings of length min to length max
        for (int i = 1; i <= maxLength; i++) {

            // save strings to temporary list before clearing string list
            List<String> tempList = new ArrayList<>(strList.size());
            tempList.addAll(strList);
            strList.clear();

            // loop for each character in alphabet
            for (char c : this.symbolSet) {

                // for each previous string of length n, append the current
                // character to create instances of strings with length n+1
                for (String str : tempList) {
                    String strInstance = str + c;
                    strList.add(strInstance);
                }
            }

            // add to return list if above min length
            if (i >= minLength) {
                returnList.addAll(strList);
            }
        }

        return returnList;
    }

    public String getWhitespaceCharSet() {

        // get char set as string from symbol set
        StringBuilder charSet = new StringBuilder();
        for (char c : this.symbolSet) {
            if (c <= '\u0020') {
                charSet.append(c);
            }
        }

        // return char set string
        return charSet.toString();
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

        // initialize random
        random = new Random(System.nanoTime());
    }

    private static Set<Character> getSymbolsFromDeclaration(String string) {

        // initialize symbol set to empty tree set for automatic sorting
        Set<Character> symbolSet = new TreeSet<>();

        // detect commas character
        if (string.contains(",,")) {

            // detect single comma in symbol string
            if (string.contains(",,,") ||
                string.startsWith(",,") ||
                string.endsWith(",,")) {

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

    public Alphabet(Set<Character> symbolSet) {
        this();
        this.symbolSet.addAll(symbolSet);
        createCharRanges();
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

    public String getCharSet() {

        // get char set as string from symbol set
        StringBuilder charSet = new StringBuilder();
        for (char c : this.symbolSet) {
            charSet.append(c);
        }

        // return char set string
        return charSet.toString();
    }

    public char randomChar() {
        return new ArrayList<Character>(this.symbolSet).get(this.random.nextInt() % this.symbolSet.size());
    }
}
