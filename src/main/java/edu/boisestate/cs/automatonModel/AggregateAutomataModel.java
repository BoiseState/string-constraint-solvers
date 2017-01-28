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

        this.modelManager = new AggregateAutomatonModelManager(alphabet, 0);
    }

    @Override
    public AutomatonModel allPrefixes() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // check if automata array has non-empty automata
        boolean isEmpty = true;
        for (Automaton a : automata) {
            if (!a.isEmpty()) {
                isEmpty = false;
            }
        }

        // perform operations
        Automaton[] results = new Automaton[] {BasicAutomata.makeEmpty()};
        int newBoundLength = 0;
        if (isEmpty) {
            results = this.performUnaryOperations(automata, new Prefix());
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          newBoundLength,
                                          this.factors);
    }

    Automaton[] getAutomata() {
        return automata;
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

    @Override
    public AutomatonModel allSubstrings() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // check if automata array has non-empty automata
        boolean isEmpty = true;
        for (Automaton a : automata) {
            if (!a.isEmpty()) {
                isEmpty = false;
            }
        }

        // perform operations
        Automaton[] results = new Automaton[] {BasicAutomata.makeEmpty()};
        int newBoundLength = 0;
        if (isEmpty) {
            results = this.performUnaryOperations(automata, new Substring());
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          newBoundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel allSuffixes() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Postfix());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {

        // create new model from existing automata
        return new AggregateAutomataModel(this.automata,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel complement() {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet);

        // for each automaton
        for (int i = 0; i < this.automata.length; i++) {

            // perform operation on automaton at index
            Automaton result = this.automata[i].complement();

            // bound result to alphabet
            Automaton bounding = anyString.repeat(0, this.boundLength);
            result = result.intersection(bounding);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);
        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // for each automaton in model
        for (int i = 0; i < this.automata.length; i++) {

            // initialize temp automata array
            Automaton[] temp = new Automaton[argAutomata.length];

            // for each automaton in arg model
            for (int j = 0; j < argAutomata.length; j++) {

                // concatenate automata
                temp[j] = this.automata[i].concatenate(argAutomata[j]);
            }

            // union temp automaton for result
            Automaton result = BasicOperations.union(Arrays.asList(temp));

            // minimize result automaton
            result.minimize();

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    private Automaton[] getAutomataFromModel(AutomatonModel model) {

        // if model is Unbounded (includes subclass Bounded) automaton model
        if (model instanceof UnboundedAutomatonModel) {

            // return array of single automaton
            Automaton automaton =
                    ((UnboundedAutomatonModel) model).getAutomaton();
            return new Automaton[]{automaton.clone()};

        } else if (model instanceof AggregateAutomataModel) {

            // return array of automata
            return ((AggregateAutomataModel) model).automata;
        }

        // throw exception
        throw new UnsupportedOperationException(
                "The argument AutomatonModel is not a known instance type and" +
                " cannot be concatenated with an AggregateAutomataModel.");
    }

    @Override
    public AutomatonModel concatenateIndividual(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);
        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // for each pair of automata from both models
        for (int i = 0; i < this.automata.length; i++) {

            // concatenate automata
            Automaton result = this.automata[i].concatenate(argAutomata[i]);

            // minimize result automaton
            result.minimize();

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
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

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // for each automaton
        for (int i = 0; i < this.automata.length; i++) {

            // create precise delete operation
            PreciseDelete delete = new PreciseDelete(start, end);

            // perform operation on automaton at index
            Automaton result = delete.op(this.automata[i]);

            // set array index with result
            results[i] = result;
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

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // for each automaton
        for (Automaton automaton : automata) {

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
    public AutomatonModel ignoreCase() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new IgnoreCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel intersect(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);
        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // for each automaton in model
        for (int i = 0; i < this.automata.length; i++) {

            // initialize temp automata array
            Automaton[] temp = new Automaton[argAutomata.length];

            // for each automaton in arg model
            for (int j = 0; j < argAutomata.length; j++) {

                // intersect automata
                temp[j] = this.automata[i].intersection(argAutomata[j]);
            }

            // union temp automaton for result
            Automaton result = BasicOperations.union(Arrays.asList(temp));

            // minimize result automaton
            result.minimize();

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
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
    public AutomatonModel minus(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);
        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // for each automaton in model
        for (int i = 0; i < this.automata.length; i++) {

            // initialize temp automata array
            Automaton[] temp = new Automaton[argAutomata.length];

            // for each automaton in arg model
            for (int j = 0; j < argAutomata.length; j++) {

                // minus automata
                temp[j] = this.automata[i].minus(argAutomata[j]);
            }

            // intersect temp automaton for result
            Automaton result = temp[0];
            for (int j = 1; j < temp.length; j++) {
                result = result.intersection(temp[j]);
            }

            // minimize result automaton
            result.minimize();

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public BigInteger modelCount() {

        // initialize total model count as big integer
        BigInteger totalModelCount = new BigInteger("0");

        // for each automaton in automata array
        for (int i = 0; i < this.automata.length; i++) {

            // get automaton model count
            BigInteger modelCount =
                    StringModelCounter.ModelCount(this.automata[i]);

            // correct model count against initial model counts
            BigInteger adjustedModelCount =

                    // add automaton model count to total model count
                    totalModelCount = totalModelCount.add(modelCount);
        }

        // return final model count
        return totalModelCount;
    }

    @Override
    public AutomatonModel prefix(int end) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new PrecisePrefix(end));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replace(char find, char replace) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata,
                                            new Replace1(find, replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata,
                                            new Replace6(find, replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceChar() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace4());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace2(find));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace3(replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel reverse() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Reverse());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel suffix(int start) {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new PreciseSuffix(start));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel toLowercase() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new ToLowerCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel toUppercase() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new ToUpperCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel trim() {

        // get automata from model
        Automaton[] automata = this.getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Substring());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }

    @Override
    public AutomatonModel union(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);
        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // for each automaton in model
        for (int i = 0; i < this.automata.length; i++) {

            // initialize temp automata array
            Automaton[] temp = new Automaton[argAutomata.length];

            // for each automaton in arg model
            for (int j = 0; j < argAutomata.length; j++) {

                // union automata
                temp[j] = this.automata[i].union(argAutomata[j]);
            }

            // union temp automaton for result
            Automaton result = BasicOperations.union(Arrays.asList(temp));

            // minimize result automaton
            result.minimize();

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength,
                                          this.factors);
    }
}
