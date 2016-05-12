package edu.boisestate.cs.automaton;

public class BoundedAutomatonModel
        implements AutomatonModel {
    @Override
    public String getAcceptedStringExample() {
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
    public AutomatonModel concatenate(AutomatonModel arg) {
        return null;
    }

    @Override
    public boolean containsString(String actualValue) {
        return false;
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {
        return null;
    }

    @Override
    public AutomatonModel clone() {
        return null;
    }

    @Override
    public AutomatonModel minus(AutomatonModel x) {
        return null;
    }

    @Override
    public AutomatonModel complement() {
        return null;
    }
}
