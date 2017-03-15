package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AggregateAutomataModel
        extends AutomatonModel {

    private Automaton[] automata;
    private int[] factors;

    private void setAutomata(Automaton[] automatonArray) {

        // fill automata array with automaton clones
        for (int i = 0; i < automatonArray.length; i++) {
            Automaton clone = automatonArray[i].clone();
            this.automata[i] = clone;

            // add factors to factor array
            this.factors[i] = 1;
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

        this.modelManager = new AggregateAutomatonModelManager(alphabet,
                                                               initialBoundLength);
    }

    AggregateAutomataModel(Automaton[] automata,
                           Alphabet alphabet,
                           int initialBoundLength,
                           int[] currentFactors) {
        super(alphabet, initialBoundLength);

        // create arrays from parameters
        this.automata = new Automaton[automata.length];
        this.factors = new int[currentFactors.length];

        setAutomata(automata);

        // copy existing factors into array
        System.arraycopy(currentFactors, 0, factors, 0, currentFactors.length);

        this.modelManager = new AggregateAutomatonModelManager(alphabet,
                                                               initialBoundLength);
    }

    AggregateAutomataModel(Automaton automaton, Alphabet alphabet) {
        super(alphabet, 0);

        this.automata = new Automaton[]{automaton};
        this.factors = new int[]{1};

        this.modelManager = new AggregateAutomatonModelManager(alphabet, 0);
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
            int[] f = new int[] {1};
            return new AggregateAutomataModel(a, this.alphabet, 0, f);
        }

        // get all substrings
        Automaton substrings = performUnaryOperation(containing, new Substring(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(substrings);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        ensureAggregateModel(containedModel);

        // create any string automata
        Automaton anyString1 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        Automaton anyString2 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] containedAutomata = getAutomataFromAggregateModel(containedModel);
        Automaton contained = mergeAutomata(containedAutomata);
        Automaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(x);
            results[i].minimize();
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(BasicAutomata.makeEmptyString());
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, 0, this.factors);
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
            int[] f = new int[] {1};
            return new AggregateAutomataModel(a, this.alphabet, 0, f);
        }

        // get all suffixes
        Automaton suffixes = performUnaryOperation(containing, new Postfix(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(suffixes);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        ensureAggregateModel(endingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] endingAutomata = getAutomataFromAggregateModel(endingModel);
        Automaton ending = mergeAutomata(endingAutomata);
        Automaton x = anyString.concatenate(ending);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(x);
            results[i].minimize();
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureAggregateModel(equalModel);

        // get equal automaton
        Automaton[] equalAutomata = getAutomataFromAggregateModel(equalModel);
        Automaton equal = mergeAutomata(equalAutomata);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(equal);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        ensureAggregateModel(equalModel);

        // get equal automaton
        Automaton[] equalAutomata = getAutomataFromAggregateModel(equalModel);
        Automaton equal = mergeAutomata(equalAutomata);
        Automaton equalIgnoreCase = performUnaryOperation(equal, new IgnoreCase(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(equalIgnoreCase);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        // check min and max
        if (min > max) {
            Automaton[] a = new Automaton[] {BasicAutomata.makeEmpty()};
            int[] f = new int[] {1};
            return new AggregateAutomataModel(a, this.alphabet, 0, f);
        }

        // get any string with length between min and max
        Automaton minMax = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat(min, max);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(minMax);
        }

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength, this.factors);
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
        }

        // get all substrings
        Automaton substrings = performUnaryOperation(notContaining, new Substring(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(substrings);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureAggregateModel(notContainedModel);

        // create any string automata
        Automaton anyString1 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        Automaton anyString2 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] notContainedAutomata = getAutomataFromAggregateModel(notContainedModel);
        Automaton notContained = mergeAutomata(notContainedAutomata);
        Automaton x = anyString1.concatenate(notContained).concatenate(anyString2);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(x);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(BasicAutomata.makeEmptyString());
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
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
        }

        // get all suffixes
        Automaton suffixes = performUnaryOperation(notContaining, new Postfix(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(suffixes);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureAggregateModel(notEndingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] notEndingAutomata = getAutomataFromAggregateModel(notEndingModel);
        Automaton notEnding = mergeAutomata(notEndingAutomata);
        Automaton x = anyString.concatenate(notEnding);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(x);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // concatenate with contained automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(notEqual);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureAggregateModel(notEqualModel);

        // concatenate with contained automaton
        Automaton[] notEqualAutomata = getAutomataFromAggregateModel(notEqualModel);
        Automaton notEqual = mergeAutomata(notEqualAutomata);
        Automaton notEqualIgnoreCase = performUnaryOperation(notEqual, new IgnoreCase(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(notEqualIgnoreCase);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
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
        }

        // get all prefixes
        Automaton prefixes = performUnaryOperation(notContaining, new Prefix(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(prefixes);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartingModel) {
        ensureAggregateModel(notStartingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] notStartingAutomata = getAutomataFromAggregateModel(notStartingModel);
        Automaton notStarting = mergeAutomata(notStartingAutomata);
        Automaton x = notStarting.concatenate(anyString);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].minus(x);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
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
            int[] f = new int[] {1};
            return new AggregateAutomataModel(a, this.alphabet, 0, f);
        }

        // get all prefixes
        Automaton prefixes = performUnaryOperation(containing, new Prefix(), this.alphabet);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(prefixes);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        ensureAggregateModel(startingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton[] startingAutomata = getAutomataFromAggregateModel(startingModel);
        Automaton starting = mergeAutomata(startingAutomata);
        Automaton x = starting.concatenate(anyString);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(x);
            results[i].minimize();
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, this.boundLength, this.factors);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get arg automaton
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].concatenate(arg);
        }

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          newBoundLength,
                                          this.factors);
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

    @Override
    public AutomatonModel delete(int start, int end) {
        // get resulting automata
        PreciseDelete operation = new PreciseDelete(start, end);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
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

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
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
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = this.automata[i].intersection(arg);
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get all suffixes
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        PreciseInsert operation = new PreciseInsert(offset);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i], arg);
        }

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength, this.factors);
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
        Replace1 operation = new Replace1(find, replace);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {
        // get resulting automata
        Replace6 operation = new Replace6(find, replace);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceChar() {
        // get resulting automata
        Replace4 operation = new Replace4();
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {
        // get resulting automata
        Replace2 operation = new Replace2(find);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {
        // get resulting automata
        Replace3 operation = new Replace3(replace);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel reverse() {
        // get resulting automata
        Reverse operation = new Reverse();
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel substring(int start, int end) {
        // get resulting automata
        PreciseSubstring operation = new PreciseSubstring(start, end);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // calculate new bound length
        int newBoundLength = end - start;

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength, this.factors);
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        ensureAggregateModel(argModel);

        // get all suffixes
        Automaton[] argAutomata = getAutomataFromAggregateModel(argModel);
        Automaton arg = mergeAutomata(argAutomata);

        // get resulting automata
        PreciseSetCharAt operation = new PreciseSetCharAt(offset);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i], arg);
        }

        // calculate new bound length
        int newBoundLength = this.boundLength + 1;

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, newBoundLength, this.factors);
    }

    @Override
    public AutomatonModel setLength(int length) {
        // get resulting automata
        PreciseSetLength operation = new PreciseSetLength(length);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automaton
        return new AggregateAutomataModel(results, this.alphabet, length, this.factors);
    }

    @Override
    public AutomatonModel suffix(int start) {
        // get resulting automata
        PreciseSuffix operation = new PreciseSuffix(start);
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // calculate new bound length
        int newBoundLength = boundLength - start;

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          newBoundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel toLowercase() {
        // get resulting automata
        ToLowerCase operation = new ToLowerCase();
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel toUppercase() {
        // get resulting automata
        ToUpperCase operation = new ToUpperCase();
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel trim() {
        // get resulting automata
        Trim operation = new Trim();
        Automaton[] results = new Automaton[this.automata.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = operation.op(this.automata[i]);
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {
        Automaton[] clones = new Automaton[this.automata.length];
        for (int i = 0; i < clones.length; i++) {
            clones[i] = this.automata[i].clone();
        }

        // create new model from existing automata
        return new AggregateAutomataModel(clones,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
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

    private Automaton[] performUnaryOperations(Automaton[] automata,
                                               UnaryOperation operation) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton alphabet = BasicAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        Automaton[] results = new Automaton[automata.length];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            Automaton result = operation.op(automata[i]);

            // bound result
            result = result.intersection(alphabet);

            // set appropriate index in results array
            results[i] = result;
        }

        // return results array
        return results;
    }
}
