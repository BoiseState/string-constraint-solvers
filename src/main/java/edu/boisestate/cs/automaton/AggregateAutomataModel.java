package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;

public class AggregateAutomataModel
        extends AutomatonModel {

    private Automaton[] automata;

    Automaton[] getAutomata() {
        return automata;
    }

    AggregateAutomataModel(Automaton[] automata, Alphabet alphabet, int initialBoundLength) {
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
        return null;
    }

    @Override
    public boolean equals(AutomatonModel argModel) {
        return false;
    }

    @Override
    public AutomatonModel union(AutomatonModel arg) {
        return null;
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel arg) {
        return null;
    }

    @Override
    public boolean containsString(String actualValue) {
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
}
