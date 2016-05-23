package edu.boisestate.cs.automaton;

public interface AutomatonModel
        extends Cloneable {

    String getAcceptedStringExample();

    int getBound();

    boolean isEmpty();

    boolean isSingleton();

    void setBound(int newBound);

    AutomatonModel concatenate(AutomatonModel arg);

    boolean containsString(String actualValue);

    AutomatonModel intersect(AutomatonModel arg);

    AutomatonModel minus(AutomatonModel x);

    AutomatonModel complement();

    boolean equals();

    AutomatonModel union(AutomatonModel arg);

    AutomatonModel clone();
}
