package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.MinMaxPair;
import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.BinaryWeightedAutomatonOp;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.UnaryWeightedAutomatonOp;
import edu.boisestate.cs.automatonModel.operations.weighted.*;
import edu.boisestate.cs.util.DotToGraph;

import java.math.BigInteger;
import java.util.*;

import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeCharSet;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmpty;
import static edu.boisestate.cs.automaton.BasicWeightedAutomata.makeEmptyString;
import static edu.boisestate.cs.automaton.WeightedMinimizationOperations
        .minimizeBrzozowski;
import static edu.boisestate.cs.automaton.WeightedMinimizationOperations
        .minimizeHopcroft;

public class WeightedAutomatonModel extends AutomatonModel {

    private static BinaryWeightedAutomatonOp intersectOp = new BinaryWeightedAutomatonOp() {
        @Override
        public WeightedAutomaton op(WeightedAutomaton a1, WeightedAutomaton a2) {
            return a1.intersection(a2);
        }
    };

    private static BinaryWeightedAutomatonOp minusOp = new BinaryWeightedAutomatonOp() {
        @Override
        public WeightedAutomaton op(WeightedAutomaton a1, WeightedAutomaton a2) {
            return a1.minus(a2);
        }
    };

    private static UnaryWeightedAutomatonOp getUnaryOp(final UnaryWeightedOperation operation) {
        return new UnaryWeightedAutomatonOp() {
            @Override
            public WeightedAutomaton op(WeightedAutomaton a1) {
                return operation.op(a1);
            }
        };
    }

    private WeightedAutomaton[] automata;

    private void setAutomata(WeightedAutomaton[] automataArray) {
        for (int i = 0; i < automataArray.length; i++) {
            WeightedAutomaton clone = automataArray[i].clone();
            this.automata[i] = clone;
        }
    }

    WeightedAutomatonModel(WeightedAutomaton[] automata, Alphabet alphabet, int initialBoundLength) {
        super(alphabet, initialBoundLength);

        this.automata = new WeightedAutomaton[automata.length];
        setAutomata(automata);

        this.modelManager = new WeightedAutomatonModelManager(alphabet, initialBoundLength);
    }

    private static WeightedAutomaton[] getAutomataFromWeightedModel(AutomatonModel model) {
        return ((WeightedAutomatonModel)model).automata;
    }

    private static WeightedAutomaton performUnaryOperation(WeightedAutomaton automaton,
                                                           UnaryWeightedOperation operation,
                                                           Alphabet alphabet) {
        // use operation
        WeightedAutomaton result = operation.op(automaton);

        // bound resulting automaton to alphabet
        String charSet = alphabet.getCharSet();
        WeightedAutomaton anyChar = makeCharSet(charSet).repeat();
        result = result.intersection(anyChar);

        // return resulting automaton
        return result;
    }

    @Override
    public String getAcceptedStringExample() {
        // cycle through each automaton until an example is found
        for (WeightedAutomaton automaton : this.automata) {
            // get shortest example from automaton
            String example = automaton.getShortestExample(true);

            // if example found, return it
            if (example != null) {
                return example;
            }
        }

        // if none found, return null;
        return null;
    }

    @Override
    public Set<String> getFiniteStrings() {
        // initialize set of strings
        Set<String> strings = new HashSet<>();

        // for each automaton
        for (WeightedAutomaton automaton : automata) {
            // get finite strings from automaton
            Set<String> automatonStrings = automaton.getFiniteStrings();

            // if set is not null, add automaton strings to strings set
            if (automatonStrings != null) {
                strings.addAll(automatonStrings);
            }
        }

        // return string set
        return strings;
    }

    @Override
    public boolean isEmpty() {
        // for each automaton in automata
        for (WeightedAutomaton automaton : this.automata) {
            // if automaton is not empty string
            if (!automaton.isEmptyString()) {
                return false;
            }
        }

        // all automata are empty strings, return true
        return true;
    }

    @Override
    public boolean isSingleton() {
        // initialize found singleton flag
        boolean foundSingleton = false;

        // for each automaton in automata
        for (WeightedAutomaton automaton : this.automata) {
            // get on finite string, null if more
            Set<String> strings = automaton.getFiniteStrings(1);

            // if strings are null, not singleton
            if (strings == null) {
                return false;
            }

            // if string is singleton
            if (strings.size() == 1 &&
                strings.iterator().next() != null) {

                // check if a singleton has already been found
                if (foundSingleton) {
                    return false;
                }

                // singleton has not already been found, set flag
                foundSingleton = true;
            }
        }

        // return found singleton flag value
        return foundSingleton;
    }

    @Override
    public AutomatonModel assertContainedInOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton[] containingArray = getAutomataFromWeightedModel(containingModel);
        WeightedAutomaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {makeEmpty()};
            return new WeightedAutomatonModel(a, alphabet, 0);
        }

        // get all substrings
        WeightedAutomaton substrings = performUnaryOperation(containing, new WeightedAllSubstrings(), this.alphabet);
        minimizeHopcroft(substrings);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, substrings, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        ensureWeightedModel(containedModel);

        // create any string automata
        WeightedAutomaton anyString1 = makeCharSet(this.alphabet.getCharSet()).repeat();
        WeightedAutomaton anyString2 = makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton[] containedAutomata = getAutomataFromWeightedModel(containedModel);
        WeightedAutomaton contained = mergeAutomata(containedAutomata);
        WeightedAutomaton x = anyString1.concatenate(contained).concatenate(anyString2);
        minimizeHopcroft(x);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, makeEmptyString(), intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, 0);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton[] containingArray = getAutomataFromWeightedModel(containingModel);
        WeightedAutomaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {makeEmpty()};
            return new WeightedAutomatonModel(a, alphabet, 0);
        }

        // get all suffixes
        WeightedAutomaton suffixes = performUnaryOperation(containing, new WeightedAllSuffixes(), this.alphabet);
        minimizeHopcroft(suffixes);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, suffixes, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        ensureWeightedModel(endingModel);

        // create any string automata
        WeightedAutomaton anyString = makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton[] endingAutomata = getAutomataFromWeightedModel(endingModel);
        WeightedAutomaton ending = mergeAutomata(endingAutomata);
        WeightedAutomaton x = anyString.concatenate(ending);
        minimizeHopcroft(x);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureWeightedModel(equalModel);

        // get equal automaton
        WeightedAutomaton[] equalAutomata = getAutomataFromWeightedModel(equalModel);
        WeightedAutomaton equal = mergeAutomata(equalAutomata);
        minimizeHopcroft(equal);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, equal, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        ensureWeightedModel(equalModel);

        // get equal automaton
        WeightedAutomaton[] equalAutomata = getAutomataFromWeightedModel(equalModel);
        WeightedAutomaton equal = mergeAutomata(equalAutomata);
        WeightedAutomaton equalIgnoreCase = performUnaryOperation(equal, new WeightedIgnoreCase(), this.alphabet);
        minimizeHopcroft(equalIgnoreCase);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, equalIgnoreCase, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        // check min and max
        if (min > max) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {makeEmpty()};
            return new WeightedAutomatonModel(a, alphabet, 0);
        }

        // get any string with length between min and max
        WeightedAutomaton minMax = makeCharSet(this.alphabet.getCharSet()).repeat(min, max);
        minimizeHopcroft(minMax);

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, minMax, intersectOp, newBoundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        ensureWeightedModel(notContainingModel);

        // get containing automaton
        WeightedAutomaton[] notContainingArray = getAutomataFromWeightedModel(notContainingModel);
        WeightedAutomaton notContaining = mergeAutomata(notContainingArray);

        // if not containing automaton is empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notContaining.isEmpty()) {
            // get all substrings
            WeightedAutomaton substrings = performUnaryOperation(notContaining, new WeightedAllSubstrings(), this.alphabet);
            minimizeHopcroft(substrings);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, substrings, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureWeightedModel(notContainedModel);

        WeightedAutomaton[] notContainedAutomata = getAutomataFromWeightedModel(notContainedModel);
        WeightedAutomaton notContained = mergeAutomata(notContainedAutomata);

        // if not containing automaton is empty
        if (notContained.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notContained = getRequiredCharAutomaton(notContained, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notContained.isEmpty()) {
            // create any string automata
            WeightedAutomaton anyString1 =
                    makeCharSet(this.alphabet.getCharSet()).repeat();
            WeightedAutomaton anyString2 =
                    makeCharSet(this.alphabet.getCharSet()).repeat();

            // concatenate with contained automaton
            WeightedAutomaton x = anyString1.concatenate(notContained)
                                            .concatenate(anyString2);
            minimizeHopcroft(x);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, makeEmptyString(), minusOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, 0);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notContainingModel) {
        ensureWeightedModel(notContainingModel);

        // get containing automaton
        WeightedAutomaton[] notContainingArray = getAutomataFromWeightedModel(notContainingModel);
        WeightedAutomaton notContaining = mergeAutomata(notContainingArray);

        // if not containing automaton is empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notContaining.isEmpty()) {
            // get all suffixes
            WeightedAutomaton suffixes = performUnaryOperation(notContaining, new WeightedAllSuffixes(), this.alphabet);
            minimizeHopcroft(suffixes);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, suffixes, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureWeightedModel(notEndingModel);

        WeightedAutomaton[] notEndingAutomata = getAutomataFromWeightedModel(notEndingModel);
        WeightedAutomaton notEnding = mergeAutomata(notEndingAutomata);

        // if not containing automaton is empty
        if (notEnding.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notEnding = getRequiredCharAutomaton(notEnding, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notEnding.isEmpty()) {
            // create any string automata
            WeightedAutomaton anyString =
                    makeCharSet(this.alphabet.getCharSet()).repeat();

            // concatenate with contained automaton
            WeightedAutomaton x = anyString.concatenate(notEnding);
            minimizeHopcroft(x);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureWeightedModel(notEqualModel);

        // get not equal automaton
        WeightedAutomaton[] notEqualAutomata = getAutomataFromWeightedModel(notEqualModel);
        WeightedAutomaton notEqual = mergeAutomata(notEqualAutomata);

        // if not containing automaton is empty
        if (notEqual.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        // if not equal automaton is a singleton
        WeightedAutomaton[] results = automata;
        if (notEqual.getFiniteStrings(1) != null) {
            minimizeHopcroft(notEqual);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, notEqual, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureWeightedModel(notEqualModel);

        // get not equal automaton
        WeightedAutomaton[] notEqualAutomata = getAutomataFromWeightedModel(notEqualModel);
        WeightedAutomaton notEqual = mergeAutomata(notEqualAutomata);

        // if not containing automaton is empty
        if (notEqual.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        // if not equal automaton is a singleton
        WeightedAutomaton[] results = automata;
        if (notEqual.getFiniteStrings(1) != null) {
            WeightedAutomaton notEqualIgnoreCase = performUnaryOperation( notEqual, new WeightedIgnoreCase(), this.alphabet);
            minimizeHopcroft(notEqualIgnoreCase);

            // get resulting automata
            results = performBinaryAutomatonOperation( automata, notEqualIgnoreCase, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel) {
        ensureWeightedModel(notStartingModel);

        // get containing automaton
        WeightedAutomaton[] notContainingArray = getAutomataFromWeightedModel(notStartingModel);
        WeightedAutomaton notContaining = mergeAutomata(notContainingArray);

        // if not containing automaton is empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notContaining.isEmpty()) {
            // get all prefixes
            WeightedAutomaton prefixes = performUnaryOperation(notContaining, new WeightedAllPrefixes(), this.alphabet);
            minimizeHopcroft(prefixes);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, prefixes, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
        ensureWeightedModel(notStartsModel);

        WeightedAutomaton[] notStartingAutomata = getAutomataFromWeightedModel(notStartsModel);
        WeightedAutomaton notStarting = mergeAutomata(notStartingAutomata);

        // if not containing automaton is empty
        if (notStarting.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {BasicWeightedAutomata.makeEmpty()};
            return new WeightedAutomatonModel(a, this.alphabet, 0);
        }

        notStarting = getRequiredCharAutomaton(notStarting, alphabet, boundLength);

        WeightedAutomaton[] results = automata;
        if(!notStarting.isEmpty()) {
            // create any string automata
            WeightedAutomaton anyString =
                    makeCharSet(this.alphabet.getCharSet()).repeat();

            // concatenate with contained automaton
            WeightedAutomaton x = notStarting.concatenate(anyString);
            minimizeHopcroft(x);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton[] containingArray = getAutomataFromWeightedModel(containingModel);
        WeightedAutomaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            WeightedAutomaton[] a = new WeightedAutomaton[] {makeEmpty()};
            return new WeightedAutomatonModel(a, alphabet, 0);
        }

        // get all prefixes
        WeightedAutomaton prefixes = performUnaryOperation(containing, new WeightedAllPrefixes(), this.alphabet);
        minimizeHopcroft(prefixes);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, prefixes, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        ensureWeightedModel(startingModel);

        // create any string automata
        WeightedAutomaton anyString = makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton[] startingAutomata = getAutomataFromWeightedModel(startingModel);
        WeightedAutomaton starting = mergeAutomata(startingAutomata);
        WeightedAutomaton x = starting.concatenate(anyString);
        minimizeHopcroft(x);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton[] argAutomata = getAutomataFromWeightedModel(argModel);
        WeightedAutomaton arg = mergeAutomata(argAutomata);

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // get resulting automata
        BinaryWeightedAutomatonOp op = new BinaryWeightedAutomatonOp() {
            @Override
            public WeightedAutomaton op(WeightedAutomaton a1, WeightedAutomaton a2) {
                return a1.concatenate(a2);
            }
        };
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, arg, op, newBoundLength);
//        for (int i = 0; i < results.length; i++) {
//            minimizeBrzozowski(results[i]);
//        }

        // return weighted model from automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public boolean containsString(String actualValue) {
        // check automata
        for (WeightedAutomaton automaton : this.automata) {
            // return true if string is contained
            if (automaton.run(actualValue)) {
                return true;
            }
        }

        // no string found, return false
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel delete(int start, int end) {
        // get resulting automata
        WeightedPreciseDelete operation = new WeightedPreciseDelete(start, end);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // determine new bound length
        int newBoundLength;
        if (this.boundLength < start) {
            newBoundLength = 0;
        } else if (this.boundLength < end) {
            newBoundLength = start;
        } else {
            int charsDeleted = end - start;
            newBoundLength = this.boundLength - charsDeleted;
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public boolean equals(AutomatonModel arg) {
        // check if arg model is weighted model
        if (arg instanceof WeightedAutomatonModel) {
            // cast arg model
            WeightedAutomatonModel argModel = (WeightedAutomatonModel) arg;

            // check if automata arrays are equal
            WeightedAutomaton[] argAutomata = argModel.automata;
            if (this.automata.length == argAutomata.length) {
                // check each automata index
                for (int i = 0; i < this.automata.length; i++) {

                    // if any automaton is not equal to the corresponding
                    // automaton, return false
                    if (!this.automata[i].equals(argAutomata[i])) {
                        return false;
                    }
                }

                // all automata matched, return true
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel intersect(AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton[] argAutomata = getAutomataFromWeightedModel(argModel);
        WeightedAutomaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        WeightedAutomaton[] results = performBinaryAutomatonOperation(automata, arg, intersectOp, boundLength);

        // return weighted model from automata
        return new WeightedAutomatonModel(results, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton[] argAutomata = getAutomataFromWeightedModel(argModel);
        WeightedAutomaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        WeightedPreciseInsert insert = new WeightedPreciseInsert(offset);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = insert.op(this.automata[i], arg);
            minimizeBrzozowski(results[i]);
        }

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength < this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return weighted model from automata
        return new WeightedAutomatonModel(results, this.alphabet, boundLength);
    }

    @Override
    public BigInteger modelCount() {
        // initialize total model count as big integer
        BigInteger totalModelCount = BigInteger.ZERO;

        // for each automaton in automata array
        for (WeightedAutomaton automaton : this.automata) {
            // get automaton model count
            BigInteger modelCount = StringModelCounter.ModelCount(automaton);

            // add automaton model count to total model count
            totalModelCount = totalModelCount.add(modelCount);
        }

        // return final model count
        return totalModelCount;
    }

    @Override
    public AutomatonModel replace(char find, char replace) {
        // get resulting automata
        WeightedReplaceChar operation = new WeightedReplaceChar(find, replace);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return weighted model from automata
        return new WeightedAutomatonModel(results, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {
        // get resulting automata
        WeightedReplaceString operation = new WeightedReplaceString(find, replace);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int newBoundLength = this.boundLength - boundDiff;

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {
        // get resulting automata
        WeightedReplaceCharUnknown operation = new WeightedReplaceCharUnknown();
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {
        // get resulting automata
        WeightedReplaceCharFindKnown operation = new WeightedReplaceCharFindKnown(find);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {
        // get resulting automata
        WeightedReplaceCharReplaceKnown operation = new WeightedReplaceCharReplaceKnown(replace);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel reverse() {
        // get resulting automata
        WeightedReverse operation = new WeightedReverse();
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel substring(int start, int end) {
        // get resulting automata
        WeightedPreciseSubstring operation = new WeightedPreciseSubstring(start, end);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // determine new bound length
        int newBoundLength = end - start;

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton[] argAutomata = getAutomataFromWeightedModel(argModel);
        WeightedAutomaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        WeightedPreciseSetCharAt insert = new WeightedPreciseSetCharAt(offset);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = insert.op(this.automata[i], arg);
            minimizeBrzozowski(results[i]);
        }

        // return weighted model from automata
        return new WeightedAutomatonModel(results, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel setLength(int length) {
        // get resulting automata
        WeightedPreciseSetLength operation = new WeightedPreciseSetLength(length);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, length);
    }

    @Override
    public AutomatonModel suffix(int start) {
        // get resulting automata
        WeightedPreciseSuffix operation = new WeightedPreciseSuffix(start);
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // determine new bound length
        int newBoundLength = this.boundLength - start;

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel toLowercase() {
        // get resulting automata
        WeightedToLowerCase operation = new WeightedToLowerCase();
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel toUppercase() {
        // get resulting automata
        WeightedToUpperCase operation = new WeightedToUpperCase();
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel trim() {
        // get resulting automata
        WeightedPreciseTrim operation = new WeightedPreciseTrim();
        WeightedAutomaton[] results = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
            minimizeBrzozowski(results[i]);
        }

        // return new model from resulting automata
        return new WeightedAutomatonModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {
        WeightedAutomaton[] clones = new WeightedAutomaton[this.automata.length];
        for (int i = 0; i < clones.length; i++) {
            clones[i] = this.automata[i].clone();
        }

        return new WeightedAutomatonModel(clones, alphabet, boundLength);
    }

    static WeightedAutomaton[] splitAutomatonByLength(WeightedAutomaton automaton, int maxLength, Alphabet alphabet) {
        WeightedAutomaton[] returnAutomata = new WeightedAutomaton[maxLength+1];
        WeightedAutomaton anyChar = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet());
        for (int i = 0; i < returnAutomata.length; i++) {
            WeightedAutomaton bounding = anyChar.repeat(i, i);
            returnAutomata[i] = automaton.intersection(bounding);
        }
        return returnAutomata;
    }

    private void ensureWeightedModel(AutomatonModel arg) {
        // check if automaton model is bounded
        if (!(arg instanceof WeightedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "The WeightedAutomatonModel only supports binary " +
                    "operations with other WeightedAutomatonModel.");
        }
    }

    private WeightedAutomaton mergeAutomata(WeightedAutomaton[] automata) {
        WeightedAutomaton result = makeEmpty();
        for (WeightedAutomaton automaton : automata) {
            result = result.union(automaton);
        }
        return result;
    }

    private WeightedAutomaton[] performUnaryAutomatonOperations(WeightedAutomaton[] automata, UnaryWeightedAutomatonOp op, int maxLength) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        WeightedAutomaton[][] results = new WeightedAutomaton[automata.length][maxLength];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            WeightedAutomaton result = op.op(automata[i]);

            // bound result
            result = result.intersection(bounding);

            // minimize result
            minimizeBrzozowski(result);

            // set appropriate index in results array
            results[i] = splitAutomatonByLength(result, maxLength, this.alphabet);
        }

        // initialize return automaton array
        WeightedAutomaton[] returnAutomata = new WeightedAutomaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicWeightedAutomata.makeEmpty();
        }

        // merge result automata into return array
        for (WeightedAutomaton[] result : results) {
            for (int j = 0; j < returnAutomata.length; j++) {
                returnAutomata[j] = returnAutomata[j].union(result[j]);
            }
        }

        // return results array
        return returnAutomata;
    }

    private WeightedAutomaton[] performBinaryAutomatonOperation(WeightedAutomaton[] automata, WeightedAutomaton arg, BinaryWeightedAutomatonOp op, int maxLength) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        WeightedAutomaton[][] results = new WeightedAutomaton[automata.length][maxLength];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            WeightedAutomaton result = op.op(automata[i], arg);

            // bound result
            result = result.intersection(bounding);

            // minimize result
//            minimizeBrzozowski(result);

            // set appropriate index in results array
            results[i] = splitAutomatonByLength(result, maxLength, this.alphabet);
        }

        // initialize return automaton array
        WeightedAutomaton[] returnAutomata = new WeightedAutomaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicWeightedAutomata.makeEmpty();
        }

        // merge result automata into return array
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < returnAutomata.length; j++) {
                returnAutomata[j] = returnAutomata[j].union(results[i][j]);
                minimizeBrzozowski(returnAutomata[j]);
            }
        }

        // return results array
        return returnAutomata;
    }

    static WeightedAutomaton getRequiredCharAutomaton(WeightedAutomaton a, Alphabet alphabet, int boundLength) {
        // if initial state is accepting
        WeightedState initialState = a.getInitialState();
        if (initialState.isAccept() && initialState.getTransitions().isEmpty()) {
            return BasicWeightedAutomata.makeEmptyString();
        }

        // initialize required char map
        Map<Integer, Character> requiredCharMap = new HashMap<>();

        // initialize state set
        Set<WeightedState> states = new TreeSet<>();
        states.add(initialState);

        // walk automaton up to bound length
        int accept = -1;
        for (int i = 0; i < boundLength && accept < 0; i++) {
            // initialize flag as true
            boolean isSame = true;

            // initialize current char to unused value
            char c = Character.MAX_VALUE;
            Set<WeightedState> newStates = new TreeSet<>();
            for (WeightedState s : states) {
                // if no transitions
                if (s.getTransitions().size() == 0) {
                    isSame = false;
                    continue;
                }
                // check if transition destination is an accepting state
                for (WeightedTransition t : s.getTransitions()) {
                    newStates.add(t.getDest());
                    if (t.getDest().isAccept()) {
                        accept = i;
                    }
                    // if transitions allow more than one character at length i
                    if (t.getMin() != t.getMax() ||
                        (c != Character.MAX_VALUE && c != t.getMin())) {
                        isSame = false;
                        continue;
                    }

                    // set current char to single char from transition
                    c = t.getMin();
                }
            }

            // if single char for transition at length i
            if (isSame && c != Character.MAX_VALUE) {
                requiredCharMap.put(i, c);
            }

            // update state set
            states = newStates;
        }

        // if no required single characters
        if (requiredCharMap.isEmpty()) {
            return BasicWeightedAutomata.makeEmpty();
        }

        // initialize initial state and current state variable
        WeightedState initial = new WeightedState();
        WeightedState s = initial;

        // create required char automaton
        int length = boundLength;
        if (accept >= 0) {
            length = accept + 1;
        }
        for (int i = 0; i < length; i ++) {
            // create new destination state
            WeightedState dest = new WeightedState();

            // if single character at length i
            if (requiredCharMap.containsKey(i)) {
                // add single char transition
                s.addTransition(new WeightedTransition(requiredCharMap.get(i), dest));
            } else {
                // add transition for all chars in alphabet
                for (MinMaxPair pair : alphabet.getCharRanges()) {
                    s.addTransition(new WeightedTransition(pair.getMin(), pair.getMax(), dest));
                }
            }

            // update current state
            s = dest;
        }

        // initialize return automaton and set initial and accepting states
        WeightedAutomaton returnAutomaton = new WeightedAutomaton();
        returnAutomaton.setInitialState(initial);
        s.setAccept(true);

        // return automaton
        return returnAutomaton;
    }

	@Override
	public String getAutomaton() {
		
		return automata.toString();
	}
}
