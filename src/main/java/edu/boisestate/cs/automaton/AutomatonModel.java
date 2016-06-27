package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;

public abstract class AutomatonModel
        implements Cloneable {

    protected final Alphabet alphabet;
    protected int boundLength;

    public abstract String getAcceptedStringExample();

    public int getBoundLength() {
        return boundLength;
    }

    public abstract boolean isEmpty();

    public abstract boolean isSingleton();

    public void setBoundLength(int boundLength) {
        this.boundLength = boundLength;
    }

    protected AutomatonModel(Alphabet alphabet,
                             int initialBoundLength) {

        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    public abstract AutomatonModel concatenate(AutomatonModel arg);

    public abstract boolean containsString(String actualValue);

    public abstract AutomatonModel intersect(AutomatonModel arg);

    public abstract AutomatonModel minus(AutomatonModel arg);

    public abstract AutomatonModel complement();

    public abstract boolean equals(AutomatonModel arg);

    public abstract AutomatonModel union(AutomatonModel arg);

    public abstract AutomatonModel clone();
}
