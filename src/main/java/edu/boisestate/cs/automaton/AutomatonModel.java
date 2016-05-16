package edu.boisestate.cs.automaton;

public interface AutomatonModel
        extends Cloneable {

    String getAcceptedStringExample();

    int getBound();

    boolean isEmpty();

    boolean isSingleton();

    AutomatonModel concatenate(AutomatonModel arg);

    boolean containsString(String actualValue);

    AutomatonModel intersect(AutomatonModel arg);

    AutomatonModel minus(AutomatonModel x);

    AutomatonModel complement();

    void setBound();

    AutomatonModel clone();
}
