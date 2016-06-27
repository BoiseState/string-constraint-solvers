package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import edu.boisestate.cs.Alphabet;

import java.util.Arrays;
import java.util.Set;

public class AggregateAutomataModel
        extends AutomatonModel {

    private Automaton[] automata;

    Automaton[] getAutomata() {
        return automata;
    }

    AggregateAutomataModel(Automaton[] automata,
                           Alphabet alphabet,
                           int initialBoundLength) {
        super(alphabet, initialBoundLength);

        setAutomata(automata);
    }

    private void setAutomata(Automaton[] automata) {

        // create automata array from parameter
        this.automata = new Automaton[automata.length];

        // fill automata array with automaton clones
        for (int i = 0; i < automata.length; i++) {
            Automaton clone = automata[i].clone();
            this.automata[i] = clone;
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {

        // create new model from existing automata
        return new AggregateAutomataModel(this.automata,
                                          this.alphabet,
                                          this.boundLength);
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
                                          this.boundLength);
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

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength);
    }

    private Automaton[] getAutomataFromModel(AutomatonModel model) {

        // if model is Unbounded (includes subclass Bounded) automaton model
        if (model instanceof UnboundedAutomatonModel) {

            // return array of single automaton
            Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();
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

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength);
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

            // union temp automaton for result
            Automaton result = BasicOperations.union(Arrays.asList(temp));

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength);
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

            // bound result to alphabet
            result = result.intersection(anyString);

            // set array index with result
            results[i] = result;
        }

        // return new model from results automata array
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          this.boundLength);
    }
}
