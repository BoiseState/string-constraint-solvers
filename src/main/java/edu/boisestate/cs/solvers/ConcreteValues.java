package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Special class to hold the values
//of concrete strings together
//with the feasibility
public class ConcreteValues {

    private final Alphabet alphabet;
    private final int initialBoundLength;
    private final List<String> values;
    private long exceptionCount;

    public List<String> getValues() {
        return this.values;
    }

    //always feasible in the root nodes
    //of the graph
    public ConcreteValues(Alphabet alphabet,
                          int initialBoundLength,
                          String value) {

        this.alphabet = alphabet;
        this.initialBoundLength = initialBoundLength;

        this.values = new ArrayList<>();
        this.values.add(value);
        exceptionCount = 0;
    }

    //creates an infeasible
    //concrete value
    public ConcreteValues(Alphabet alphabet, int initialBoundLength) {

        this.alphabet = alphabet;
        this.initialBoundLength = initialBoundLength;

        this.values = new ArrayList<>();
        exceptionCount = 0;
    }

    public ConcreteValues(Alphabet alphabet,
                          int initialBoundLength,
                          Collection<String> values) {

        this.alphabet = alphabet;
        this.initialBoundLength = initialBoundLength;

        this.values = new ArrayList<>();
        this.values.addAll(values);
        exceptionCount = 0;
    }

    @Override
    public boolean equals(Object o) {
        boolean areEqual = true;
        if (o instanceof ConcreteValues) {
            ConcreteValues other = (ConcreteValues) o;
            List<String> otherValues = other.values;

            if (this.values.size() != otherValues.size()) {
                areEqual = false;
            }

            for (String str : this.values) {
                if (otherValues.contains(str)) {
                    areEqual = false;
                }
            }
        }
        return areEqual;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("( ");
        for (String str : this.values) {
            output.append(str).append(" | ");
        }
        output.delete(output.length() - 2, output.length());
        output.append(") ");

        return output.toString();
    }

    public void addValue(String string) {
        this.values.add(string);
    }

    public ConcreteValues assertContainedInEnding(ConcreteValues containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each suffix in values
        for (String suffix : this.values) {

            // for each possible string
            for (String string : containing.values) {
                // if the string ends with the suffix
                if (string.endsWith(suffix)) {
                    // add suffix to result list
                    results.add(suffix);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertContainedInOther(ConcreteValues containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each substring in values
        for (String substr : this.values) {

            // for each possible string
            for (String string : containing.values) {
                // if the string contains the substring
                if (string.contains(substr)) {
                    // add substring to result list
                    results.add(substr);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertContainedInStart(ConcreteValues containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each prefix in values
        for (String prefix : this.values) {

            // for each possible string
            for (String string : containing.values) {
                // if the string starts with the prefix
                if (string.startsWith(prefix)) {
                    // add prefix to result list
                    results.add(prefix);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertContainsOther(ConcreteValues substring) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // for each possible substring
            for (String substr : substring.values) {
                // if the string contains the substring
                if (string.contains(substr)) {
                    // add string to result list
                    results.add(string);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertEndsWith(ConcreteValues suffix) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // for each possible suffix
            for (String suf : suffix.values) {
                // if the string ends with the suffix
                if (string.endsWith(suf)) {
                    // add string to result list
                    results.add(string);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertEqual(ConcreteValues other) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // for each possible other string
            for (String otherString : other.values) {
                // if the string equals the other
                if (string.equals(otherString)) {
                    // add string to result list
                    results.add(string);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertEqualIgnoreCase(ConcreteValues other) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // for each possible other string
            for (String otherString : other.values) {
                // if the string equals the other ignoring case
                if (string.equalsIgnoreCase(otherString)) {
                    // add string to result list
                    results.add(string);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertIsEmpty() {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // if string is empty
            if (string.isEmpty()) {
                // add string to results
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    @SuppressWarnings("Duplicates")
    public ConcreteValues assertNotContainedInEnding(ConcreteValues
                                                             containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each suffix in values
        for (String suffix : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible string
            for (String string : containing.values) {
                // if the string does end with the suffix
                if (string.endsWith(suffix)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(suffix);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertNotContainedInOther(ConcreteValues containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each substring in values
        for (String substr : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible string
            for (String string : containing.values) {
                // if the string does contain the substring
                if (!string.contains(substr)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not contain the substring
            if (flag) {
                results.add(substr);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    @SuppressWarnings("Duplicates")
    public ConcreteValues assertNotContainedInStart(ConcreteValues containing) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each prefix in values
        for (String prefix : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible string
            for (String string : containing.values) {
                // if the string does start with the prefix
                if (string.startsWith(prefix)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the prefix
            if (flag) {
                results.add(prefix);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertNotContainsOther(ConcreteValues substring) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible substring
            for (String substr : substring.values) {
                // if the string does contain the substring
                if (string.contains(substr)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    @SuppressWarnings("Duplicates")
    public ConcreteValues assertNotEndsWith(ConcreteValues suffix) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible suffix
            for (String suf : suffix.values) {
                // if the string does end with the suffix
                if (string.endsWith(suf)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertNotEqual(ConcreteValues other) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible other string
            for (String otherString : other.values) {
                // if the string does equal the other
                if (string.equals(otherString)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertNotEqualIgnoreCase(ConcreteValues other) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible other string
            for (String otherString : other.values) {
                // if the string does equal the other ignoring case
                if (string.equalsIgnoreCase(otherString)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertNotIsEmpty() {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // if string is empty
            if (!string.isEmpty()) {
                // add string to results
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    @SuppressWarnings("Duplicates")
    public ConcreteValues assertNotStartsWith(ConcreteValues prefix) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {
            // initialize flag
            boolean flag = true;
            // for each possible prefix
            for (String pre : prefix.values) {
                // if the string does start with the prefix
                if (string.startsWith(pre)) {
                    // unset flag
                    flag = false;

                    // no need to keep iterating, break the loop
                    break;
                }
            }
            // if all containing values do not have the suffix
            if (flag) {
                results.add(string);
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues assertStartsWith(ConcreteValues prefix) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String string : this.values) {

            // for each possible prefix
            for (String pre : prefix.values) {
                // if the string starts with the prefix
                if (string.startsWith(pre)) {
                    // add string to result list
                    results.add(string);

                    // no need to keep iterating, break the loop
                    break;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues concat(ConcreteValues arg) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in both base and arg values
        for (String baseStr : this.values) {
            for (String argStr : arg.values) {
                // add concatenation of strings to result list
                results.add(baseStr.concat(argStr));
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    /**
     * return@ the copy of itself
     **/
    public ConcreteValues copy() {
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  this.values);
    }

    public ConcreteValues delete(int start, int end) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            try {
                // add deleted string to result list
                StringBuilder strBuilder = new StringBuilder(str);
                strBuilder.delete(start, end);
                results.add(strBuilder.toString());
            } catch (Exception e) {
                this.exceptionCount += 1;
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues deleteCharAt(int loc) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            try {
                // add deleted string to result list
                StringBuilder strBuilder = new StringBuilder(str);
                strBuilder.deleteCharAt(loc);
                results.add(strBuilder.toString());
            } catch (Exception e) {
                this.exceptionCount += 1;
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues insert(int offset, ConcreteValues arg) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in both base and arg values
        for (String baseStr : this.values) {
            for (String argStr : arg.values) {
                try {
                    // add result of string insertion to result list
                    StringBuilder strBuilder = new StringBuilder(baseStr);
                    strBuilder.insert(offset, argStr);
                    results.add(strBuilder.toString());
                } catch (Exception e) {
                    this.exceptionCount += 1;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues replace(char find, char replace) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            // add replaced string to result list
            results.add(str.replace(find, replace));
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues replace(ConcreteValues find, ConcreteValues replace) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in both base and arg values
        for (String baseStr : this.values) {
            for (String findStr : find.values) {
                for (String replaceStr : replace.values) {
                    // add replaced string to result list
                    results.add(baseStr.replace(findStr, replaceStr));
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues replaceChar() {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            for (char find : this.alphabet.getSymbolSet()) {
                for (char replace : this.alphabet.getSymbolSet()) {
                    // add replaced string to result list
                    results.add(str.replace(find, replace));
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues replaceFindKnown(char find) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            for (char replace : this.alphabet.getSymbolSet()) {
                // add replaced string to result list
                results.add(str.replace(find, replace));
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues replaceReplaceKnown(char replace) {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            for (char find : this.alphabet.getSymbolSet()) {
                // add replaced string to result list
                results.add(str.replace(find, replace));
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues reverse() {
        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            // add replaced string to result list
            StringBuilder strBuilder = new StringBuilder(str);
            strBuilder.reverse();
            results.add(strBuilder.toString());
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues setCharAt(int offset, ConcreteValues arg) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in both base and arg values
        for (String baseStr : this.values) {
            for (String argStr : arg.values) {
                try {
                    // add result of setting character to result list
                    StringBuilder strBuilder = new StringBuilder(baseStr);
                    strBuilder.setCharAt(offset, argStr.charAt(0));
                    results.add(strBuilder.toString());
                } catch (Exception e) {
                    this.exceptionCount += 1;
                }
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }


    public ConcreteValues substring(int start, int end) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            try {
                // add substring to result list
                results.add(str.substring(start, end));
            } catch (Exception e) {
                this.exceptionCount += 1;
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues substring(int start) {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            try {
            // add substring to result list
            results.add(str.substring(start));
            } catch (Exception e) {
                this.exceptionCount += 1;
            }
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues toLowerCase() {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            // add lowercase string to result list
            results.add(str.toLowerCase());
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues toUpperCase() {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            // add uppercase string to result list
            results.add(str.toUpperCase());
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }

    public ConcreteValues trim() {

        // initialize result list
        List<String> results = new ArrayList<>();

        // for each string in values
        for (String str : this.values) {
            // add trimmed string to result list
            results.add(str.trim());
        }

        // return new concrete values from result list
        return new ConcreteValues(this.alphabet,
                                  this.initialBoundLength,
                                  results);
    }
}
