package edu.boisestate.cs.automaton;

public interface AutomatonModel
        extends Cloneable {

    String getAcceptedStringExample();

    boolean isEmpty();

    boolean isSingleton();

    AutomatonModel concatenate(AutomatonModel arg);

    boolean containsString(String actualValue);

    AutomatonModel intersect(AutomatonModel arg);

    AutomatonModel clone();

    AutomatonModel minus(AutomatonModel x);

    AutomatonModel complement();
}
