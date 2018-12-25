package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;
import edu.boisestate.cs.util.Tuple;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AggregateAutomataModel
        extends AutomatonModel {

    private static BinaryAutomatonOp intersectOp = new BinaryAutomatonOp() {
        @Override
        public Automaton op(Automaton a1, Automaton a2) {
            return a1.intersection(a2);
        }
    };

    private static BinaryAutomatonOp minusOp = new BinaryAutomatonOp() {
        @Override
        public Automaton op(Automaton a1, Automaton a2) {
            return a1.minus(a2);
        }
    };

    private Automaton[] automata;
    private int[] factors;

    private void setAutomata(Automaton[] automatonArray) {

        // fill automata array with automaton clones
        for (int i = 0; i < automatonArray.length; i++) {
            Automaton clone = automatonArray[i].clone();
            this.automata[i] = clone;
        }
    }

    AggregateAutomataModel(Automaton[] automata,
                           Alphabet alphabet,
                           int initialBoundLength) {
        super(alphabet, initialBoundLength);

        // create arrays from parameter
        this.automata = new Automaton[automata.length];
        this.factors = new int[automata.length];

        setAutomata(automata);

        for (int i = 0; i < automata.length; i++) {
            this.factors[i] = 1;
        }

        this.modelManager = new AggregateAutomatonModelManager(alphabet, initialBoundLength);
    }

    AggregateAutomataModel(Automaton[] automata,
                           Alphabet alphabet,
                           int initialBoundLength,
                           int[] factors) {
        super(alphabet, initialBoundLength);

        // create arrays from parameter
        this.automata = new Automaton[automata.length];

        setAutomata(automata);

        this.factors = Arrays.copyOf(factors, automata.length);

        this.modelManager = new AggregateAutomatonModelManager(alphabet, initialBoundLength);
    }

    AggregateAutomataModel(Automaton automaton, Alphabet alphabet) {
        super(alphabet, 0);

        this.automata = new Automaton[]{automaton};
        this.factors = new int[] {1};

        this.modelManager = new AggregateAutomatonModelManager(alphabet, 0);
    }

    private static UnaryAutomatonOp getUnaryOp(final UnaryOperation operation) {
        return new UnaryAutomatonOp() {
            @Override
            public Automaton op(Automaton a1) {
                return operation.op(a1);
            }
        };
    }

    static Automaton[] splitAutomatonByLength(Automaton automaton, int maxLength, Alphabet alphabet) {
        Automaton[] returnAutomata = new Automaton[maxLength + 1];
        Automaton anyChar = BasicAutomata.makeCharSet(alphabet.getCharSet());
        for (int i = 0; i < returnAutomata.length; i++) {
            Automaton bounding = anyChar.repeat(i, i);
            returnAutomata[i] = automaton.intersection(bounding);
        }
        return returnAutomata;
    }

    @Override
    public String getAcceptedStringExample() {
        // cycle through each automaton until an example is found
        for (Automaton automaton : this.automata) {
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
        for (Automaton automaton : this.automata) {
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
        for (Automaton automaton : this.automata) {
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
        for (Automaton automaton : this.automata) {
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
        ensureAggregateModel(containingModel);

        // get containing automata
        Automaton[] containingArray = getAutomataFromAggregateModel(containingModel);
        Automaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get all substrings
        Automaton substrings = performUnaryOperation(containing, new Substring(), this.alphabet);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(substrings, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        ensureAggregateModel(containedModel);

        // create any string automata
        Automaton anyString1 = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        Automaton anyString2 = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] containedAutomata = getAutomataFromAggregateModel(containedModel);
        Automaton contained = mergeAutomata(containedAutomata);
        Automaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(BasicAutomata.makeEmptyString(), intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, 0);
    }

    @Override
    public AutomatonModel assertEndsOther(AutomatonModel containingModel) {
        ensureAggregateModel(containingModel);

        // get containing automata
        Automaton[] containingArray = getAutomataFromAggregateModel(containingModel);
        Automaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get all suffixes
        Automaton suffixes = performUnaryOperation(containing, new Postfix(), this.alphabet);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(suffixes, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        ensureAggregateModel(endingModel);

        // create any string automata
        Automaton anyString = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] endingAutomata = getAutomataFromAggregateModel(endingModel);
        Automaton ending = mergeAutomata(endingAutomata);
        Automaton x = anyString.concatenate(ending);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureAggregateModel(equalModel);

        // get equal automaton
        Automaton[] equalAutomata = getAutomataFromAggregateModel(equalModel);
        Automaton equal = mergeAutomata(equalAutomata);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(equal, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        ensureAggregateModel(equalModel);

        // get equal automaton
        Automaton[] equalAutomata = getAutomataFromAggregateModel(equalModel);
        Automaton equal = mergeAutomata(equalAutomata);
        Automaton equalIgnoreCase = performUnaryOperation(equal, new IgnoreCase(), this.alphabet);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(equalIgnoreCase, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        // check min and max
        if (min > max) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get any string with length between min and max
        Automaton minMax = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat(min, max);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(minMax, intersectOp, boundLength);

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notContaining.isEmpty()) {

            // get all substrings
            Automaton substrings = performUnaryOperation(notContaining, new Substring(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(substrings, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureAggregateModel(notContainedModel);

        Automaton[] notContainedAutomata = getAutomataFromAggregateModel(notContainedModel);
        Automaton notContained = mergeAutomata(notContainedAutomata);

        // if arg automata are  empty
        if (notContained.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContained = getRequiredCharAutomaton(notContained, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notContained.isEmpty()) {
            // create any string automata
            Automaton anyString1 =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();
            Automaton anyString2 =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();

            // concatenate with contained automaton
            Automaton x = anyString1.concatenate(notContained)
                                    .concatenate(anyString2);

            // get resulting automata
            results = performBinaryAutomatonOperation(x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(BasicAutomata.makeEmptyString(), minusOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notContaining.isEmpty()) {

            // get all suffixes
            Automaton suffixes = performUnaryOperation(notContaining, new Postfix(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(suffixes, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureAggregateModel(notEndingModel);

        Automaton[] notEndingAutomata = getAutomataFromAggregateModel(notEndingModel);
        Automaton notEnding = mergeAutomata(notEndingAutomata);

        // if arg automata are  empty
        if (notEnding.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notEnding = getRequiredCharAutomaton(notEnding, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notEnding.isEmpty()) {
            // create any string automata
            Automaton anyString =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();

            // concatenate with contained automaton
            Automaton x = anyString.concatenate(notEnding);

            // get resulting automata
            results = performBinaryAutomatonOperation(x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // get not equal automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);

        // if arg automata are  empty
        if (notEqual.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // if not equal automaton is a singleton
        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (notEqual.getFiniteStrings(1) != null) {
            // get resulting automata
            results = performBinaryAutomatonOperation(notEqual, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // get not equal automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);

        // if arg automata are  empty
        if (notEqual.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // if not equal automaton is a singleton
        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (notEqual.getFiniteStrings(1) != null) {
            Automaton notEqualIgnoreCase = performUnaryOperation(notEqual, new IgnoreCase(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(notEqualIgnoreCase, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty() || this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notContaining.isEmpty()) {

            // get all prefixes
            Automaton prefixes = performUnaryOperation(notContaining, new Prefix(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(prefixes, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartingModel) {
        ensureAggregateModel(notStartingModel);

        Automaton[] notStartingAutomata = getAutomataFromAggregateModel(notStartingModel);
        Automaton notStarting = mergeAutomata(notStartingAutomata);

        // if arg automata are  empty
        if (notStarting.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notStarting = getRequiredCharAutomaton(notStarting, alphabet, boundLength);

        Tuple<Automaton[], int[]> results = new Tuple<>(automata, factors);
        if (!notStarting.isEmpty()) {
            // create any string automata
            Automaton anyString =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();

            // concatenate with contained automaton
            Automaton x = notStarting.concatenate(anyString);

            // get resulting automata
            results = performBinaryAutomatonOperation(x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel assertStartsOther(AutomatonModel containingModel) {
        ensureAggregateModel(containingModel);

        // get containing automata
        Automaton[] containingArray = getAutomataFromAggregateModel(containingModel);
        Automaton containing = mergeAutomata(containingArray);

        // if either automata is  empty
        if (containing.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get all prefixes
        Automaton prefixes = performUnaryOperation(containing, new Prefix(), this.alphabet);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(prefixes, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        ensureAggregateModel(startingModel);

        // create any string automata
        Automaton anyString = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] startingAutomata = getAutomataFromAggregateModel(startingModel);
        Automaton starting = mergeAutomata(startingAutomata);
        Automaton x = starting.concatenate(anyString);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get arg automaton
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // get resulting automata
        BinaryAutomatonOp op = new BinaryAutomatonOp() {
            @Override
            public Automaton op(Automaton a1, Automaton a2) {
                return a1.concatenate(a2);
            }
        };
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(arg, op, newBoundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public boolean containsString(String actualValue) {
        // check automata
        for (Automaton automaton : this.automata) {
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

        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new PreciseDelete(start, end)), newBoundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results.get1(), alphabet, newBoundLength, results.get2());
    }

    @Override
    public boolean equals(AutomatonModel arg) {
        // check if arg model is aggregate automata model
        if (arg instanceof AggregateAutomataModel) {
            // cast arg model
            AggregateAutomataModel argModel = (AggregateAutomataModel) arg;

            // check if automata arrays are equal
            Automaton[] argAutomata = argModel.automata;
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
        ensureAggregateModel(argModel);

        // get arg automaton
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(arg, intersectOp, boundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get all suffixes
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // get resulting automata
        final PreciseInsert operation = new PreciseInsert(offset);
        BinaryAutomatonOp binaryOp = new BinaryAutomatonOp() {
            @Override
            public Automaton op(Automaton a1, Automaton a2) {
                return operation.op(a1,a2);
            }
        };
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(arg, binaryOp, newBoundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public BigInteger modelCount() {
        // initialize total model count as big integer
        BigInteger totalModelCount = BigInteger.ZERO;

        // for each automaton in automata array
        for (int i = 0; i < automata.length; i++) {
            // get automaton model count
            BigInteger modelCount = StringModelCounter.ModelCount(automata[i]);
            BigInteger factor = BigInteger.valueOf(factors[i]);
            BigInteger factoredMC = modelCount.multiply(factor);

            // add automaton model count to total model count
            totalModelCount = totalModelCount.add(factoredMC);
        }

        // return final model count
        return totalModelCount;
    }

    @Override
    public AutomatonModel replace(char find, char replace) {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Replace1(find, replace)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        int newBoundLength = boundLength;
        if (find != null && replace != null && (replace.length() - find.length()) > 0) {
            newBoundLength = boundLength + replace.length() - find.length();
        }

        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Replace6(find, replace)), newBoundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public AutomatonModel replaceChar() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Replace4()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Replace2(find)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Replace3(replace)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel reverse() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new Reverse()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel substring(int start, int end) {

        // calculate new bound length
        int newBoundLength = end - start;

        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new PreciseSubstring(start, end)), newBoundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get all suffixes
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        final PreciseSetCharAt operation = new PreciseSetCharAt(offset);
        BinaryAutomatonOp binaryOp = new BinaryAutomatonOp() {
            @Override
            public Automaton op(Automaton a1, Automaton a2) {
                return operation.op(a1,a2);
            }
        };
        Tuple<Automaton[], int[]> results = performBinaryAutomatonOperation(arg, binaryOp, boundLength);

        // calculate new bound length
        int newBoundLength = this.boundLength + 1;

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public AutomatonModel setLength(int length) {
        // add null to new alphabet
        Set<Character> symbolSet = alphabet.getSymbolSet();
        symbolSet.add('\u0000');
        Alphabet newAlphabet = new Alphabet(symbolSet);

        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new PreciseSetLength(length)), length);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results.get1(), newAlphabet, length, results.get2());
    }

    @Override
    public AutomatonModel suffix(int start) {

        // calculate new bound length
        int newBoundLength = boundLength;
        if (boundLength >= start){
            newBoundLength = boundLength - start;
        }

        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new PreciseSuffix(start)), newBoundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, newBoundLength, results.get2());
    }

    @Override
    public AutomatonModel toLowercase() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new ToLowerCase()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel toUppercase() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new ToUpperCase()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @Override
    public AutomatonModel trim() {
        // get resulting automata
        Tuple<Automaton[], int[]> results = performUnaryAutomatonOperations(getUnaryOp(new PreciseTrim()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results.get1(), this.alphabet, this.boundLength, results.get2());
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {
        Automaton[] cloneAutomata = new Automaton[this.automata.length];
        int[] cloneFactors = new int[this.automata.length];
        for (int i = 0; i < cloneAutomata.length; i++) {
            cloneAutomata[i] = this.automata[i].clone();
            cloneFactors[i] = this.factors[i];
        }

        // create new model from existing automata
        return new AggregateAutomataModel(cloneAutomata, this.alphabet, this.boundLength, cloneFactors);
    }

    private void ensureAggregateModel(AutomatonModel arg) {
        // check if automaton model is unbounded
        if (!(arg instanceof AggregateAutomataModel)) {

            throw new UnsupportedOperationException(
                    "The AggregateAutomataModel only supports binary " +
                    "operations with other AggregateAutomataModels.");
        }
    }

    private Automaton[] getAutomataFromAggregateModel(AutomatonModel model) {
            return ((AggregateAutomataModel) model).automata;
    }

    private Automaton mergeAutomata(Automaton[] automata) {
        Automaton result = BasicAutomata.makeEmpty();
        for (Automaton automaton : automata) {
            result = result.union(automaton);
        }
        result.minimize();
        return result;
    }

    @SuppressWarnings("Duplicates")
    private Tuple<Automaton[], int[]> performBinaryAutomatonOperation(Automaton arg, BinaryAutomatonOp op, int maxLength) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton bounding = BasicAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        Automaton[][] results = new Automaton[automata.length][maxLength];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            Automaton result = op.op(automata[i], arg);

            // bound result
            result = result.intersection(bounding);

            // minimize result
            result.minimize();

            // set appropriate index in results array
            results[i] = splitAutomatonByLength(result, maxLength, this.alphabet);
        }

        // initialize return structures
        int [] returnFactors = new int [maxLength + 1];
        Automaton[] returnAutomata = new Automaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicAutomata.makeEmpty();
            returnFactors[i] = 0;
        }

        // merge result automata into return array
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < returnAutomata.length; j++) {
                if (!results[i][j].isEmpty()) {
                    returnAutomata[j] = returnAutomata[j].union(results[i][j]);
                    returnAutomata[j].minimize();
                    returnFactors[j] += factors[i];
                }
            }
        }

        // return results
        return new Tuple<>(returnAutomata, returnFactors);
    }

    @SuppressWarnings("Duplicates")
    private Tuple<Automaton[], int[]> performUnaryAutomatonOperations(UnaryAutomatonOp op, int maxLength) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton bounding = BasicAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        Automaton[][] results = new Automaton[automata.length][maxLength];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            Automaton result = op.op(automata[i]);

            // bound result
            result = result.intersection(bounding);

            // minimize result
            result.minimize();

            // set appropriate index in results array
            results[i] = splitAutomatonByLength(result, maxLength, this.alphabet);
        }

        // initialize return structures
        int [] returnFactors = new int [maxLength + 1];
        Automaton[] returnAutomata = new Automaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicAutomata.makeEmpty();
            returnFactors[i] = 0;
        }

        // merge result automata into return array
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < returnAutomata.length; j++) {
                if (!results[i][j].isEmpty()) {
                    returnAutomata[j] = returnAutomata[j].union(results[i][j]);
                    returnAutomata[j].minimize();
                    returnFactors[j] += factors[i];
                }
            }
        }

        // return results
        return new Tuple<>(returnAutomata, returnFactors);
    }

	@Override
	public String getAutomaton() {
		return automata.toString();
	}
}
