package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
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

        setAutomata(automata);

        this.modelManager = new AggregateAutomatonModelManager(alphabet, initialBoundLength);
    }

    AggregateAutomataModel(Automaton automaton, Alphabet alphabet) {
        super(alphabet, 0);

        this.automata = new Automaton[]{automaton};

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
        Automaton[] returnAutomata = new Automaton[maxLength+1];
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
        Automaton[] results = performBinaryAutomatonOperation(automata, substrings, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automata
        Automaton[] results = performBinaryAutomatonOperation(automata, BasicAutomata.makeEmptyString(), intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, 0);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, suffixes, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureAggregateModel(equalModel);

        // get equal automaton
        Automaton[] equalAutomata = getAutomataFromAggregateModel(equalModel);
        Automaton equal = mergeAutomata(equalAutomata);

        // get resulting automata
        Automaton[] results = performBinaryAutomatonOperation(automata, equal, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, equalIgnoreCase, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, minMax, intersectOp, boundLength);

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty()) {
            return this.clone();
        } else if (this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Automaton[] results = automata;
        if (!notContaining.isEmpty()) {

            // get all substrings
            Automaton substrings = performUnaryOperation(notContaining, new Substring(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, substrings, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureAggregateModel(notContainedModel);

        Automaton[] notContainedAutomata = getAutomataFromAggregateModel(notContainedModel);
        Automaton notContained = mergeAutomata(notContainedAutomata);

        // get automaton of required chars from not containing automaton
        notContained = getRequiredCharAutomaton(notContained, alphabet, boundLength);

        Automaton[] results = automata;
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
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automata
        Automaton[] results = performBinaryAutomatonOperation(automata, BasicAutomata.makeEmptyString(), minusOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty()) {
            return this.clone();
        } else if (this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Automaton[] results = automata;
        if (!notContaining.isEmpty()) {

            // get all suffixes
            Automaton suffixes = performUnaryOperation(notContaining, new Postfix(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, suffixes, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureAggregateModel(notEndingModel);

        Automaton[] notEndingAutomata = getAutomataFromAggregateModel(notEndingModel);
        Automaton notEnding = mergeAutomata(notEndingAutomata);

        // get automaton of required chars from not containing automaton
        notEnding = getRequiredCharAutomaton(notEnding, alphabet, boundLength);

        Automaton[] results = automata;
        if (!notEnding.isEmpty()) {
            // create any string automata
            Automaton anyString =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();

            // concatenate with contained automaton
            Automaton x = anyString.concatenate(notEnding);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // get not equal automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);

        // if not equal automaton is a singleton
        Automaton[] results = automata;
        if (notEqual.getFiniteStrings(1) != null) {
            // get resulting automata
            results = performBinaryAutomatonOperation(automata, notEqual, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // get not equal automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);

        // if not equal automaton is a singleton
        Automaton[] results = automata;
        if (notEqual.getFiniteStrings(1) != null) {
            Automaton notEqualIgnoreCase = performUnaryOperation(notEqual, new IgnoreCase(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, notEqualIgnoreCase, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notContainingModel) {
        ensureAggregateModel(notContainingModel);

        // get containing automata
        Automaton[] notContainingArray = getAutomataFromAggregateModel(notContainingModel);
        Automaton notContaining = mergeAutomata(notContainingArray);

        // if either automata is  empty
        if (notContaining.isEmpty()) {
            return this.clone();
        } else if (this.isEmpty()) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            return new AggregateAutomataModel(a, this.alphabet, 0);
        }

        // get automaton of required chars from not containing automaton
        notContaining = getRequiredCharAutomaton(notContaining, alphabet, boundLength);

        Automaton[] results = automata;
        if (!notContaining.isEmpty()) {

            // get all prefixes
            Automaton prefixes = performUnaryOperation(notContaining, new Prefix(), this.alphabet);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, prefixes, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartingModel) {
        ensureAggregateModel(notStartingModel);

        Automaton[] notStartingAutomata = getAutomataFromAggregateModel(notStartingModel);
        Automaton notStarting = mergeAutomata(notStartingAutomata);

        // get automaton of required chars from not containing automaton
        notStarting = getRequiredCharAutomaton(notStarting, alphabet, boundLength);

        Automaton[] results = automata;
        if (!notStarting.isEmpty()) {
            // create any string automata
            Automaton anyString =
                    BasicAutomata.makeCharSet(this.alphabet.getCharSet())
                                 .repeat();

            // concatenate with contained automaton
            Automaton x = notStarting.concatenate(anyString);

            // get resulting automata
            results = performBinaryAutomatonOperation(automata, x, minusOp, boundLength);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, prefixes, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, x, intersectOp, boundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, arg, op, newBoundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
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
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new PreciseDelete(start, end)), newBoundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results, alphabet, newBoundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, arg, intersectOp, boundLength);

        // return new model from results automata array
        return new AggregateAutomataModel(results, alphabet, boundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, arg, binaryOp, newBoundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public BigInteger modelCount() {
        // initialize total model count as big integer
        BigInteger totalModelCount = BigInteger.ZERO;

        // for each automaton in automata array
        for (Automaton automaton : this.automata) {
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
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Replace1(find, replace)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        int newBoundLength = boundLength;
        if (find != null && replace != null && (replace.length() - find.length()) > 0) {
            newBoundLength = boundLength + replace.length() - find.length();
        }

        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Replace6(find, replace)), newBoundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Replace4()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Replace2(find)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Replace3(replace)), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel reverse() {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new Reverse()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel substring(int start, int end) {

        // calculate new bound length
        int newBoundLength = end - start;

        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new PreciseSubstring(start, end)), newBoundLength);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
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
        Automaton[] results = performBinaryAutomatonOperation(automata, arg, binaryOp, boundLength);

        // calculate new bound length
        int newBoundLength = this.boundLength + 1;

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel setLength(int length) {
        // add null to new alphabet
        Set<Character> symbolSet = alphabet.getSymbolSet();
        symbolSet.add('\u0000');
        Alphabet newAlphabet = new Alphabet(symbolSet);

        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new PreciseSetLength(length)), length);

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, newAlphabet, length);
    }

    @Override
    public AutomatonModel suffix(int start) {

        // calculate new bound length
        int newBoundLength = boundLength;
        if (boundLength >= start){
            newBoundLength = boundLength - start;
        }

        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new PreciseSuffix(start)), newBoundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel toLowercase() {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new ToLowerCase()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel toUppercase() {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new ToUpperCase()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel trim() {
        // get resulting automata
        Automaton[] results = performUnaryAutomatonOperations(automata, getUnaryOp(new PreciseTrim()), boundLength);

        // return new model from resulting automata
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {
        Automaton[] clones = new Automaton[this.automata.length];
        for (int i = 0; i < clones.length; i++) {
            clones[i] = this.automata[i].clone();
        }

        // create new model from existing automata
        return new AggregateAutomataModel(clones, this.alphabet, this.boundLength);
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
        return result;
    }

    private Automaton[] performBinaryAutomatonOperation(Automaton[] automata, Automaton arg, BinaryAutomatonOp op, int maxLength) {

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

        // initialize return automaton array
        Automaton[] returnAutomata = new Automaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicAutomata.makeEmpty();
        }

        // merge result automata into return array
        for (Automaton[] result : results) {
            for (int j = 0; j < returnAutomata.length; j++) {
                returnAutomata[j] = returnAutomata[j].union(result[j]);
                returnAutomata[j].minimize();
            }
        }

        // return results array
        return returnAutomata;
    }

    private Automaton[] performUnaryAutomatonOperations(Automaton[] automata,
                                               UnaryAutomatonOp op,
                                               int maxLength) {

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

        // initialize return automaton array
        Automaton[] returnAutomata = new Automaton[maxLength + 1];
        for (int i = 0; i < returnAutomata.length; i++) {
            returnAutomata[i] = BasicAutomata.makeEmpty();
        }

        // merge result automata into return array
        for (Automaton[] result : results) {
            for (int j = 0; j < returnAutomata.length; j++) {
                returnAutomata[j] = returnAutomata[j].union(result[j]);
            }
        }

        // return results array
        return returnAutomata;
    }
}
