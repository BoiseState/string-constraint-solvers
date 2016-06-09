package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import edu.boisestate.cs.Alphabet;

import java.util.Arrays;

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
    public AutomatonModel concatenate(AutomatonModel argModel) {

        // initialize result automata array
        Automaton[] results = new Automaton[this.automata.length];

        // get arg automata array
        Automaton[] argAutomata = this.getAutomataFromModel(argModel);

        // get any string automaton for bounding to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet);

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
            Automaton automaton = ((UnboundedAutomatonModel) model).automaton;
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
    public boolean equals(AutomatonModel argModel) {
        return false;
    }

    @Override
    public String getAcceptedStringExample() {
        return null;
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public AutomatonModel minus(AutomatonModel x) {
        return null;
    }

    @Override
    public AutomatonModel union(AutomatonModel arg) {
        return null;
    }
}
