package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;

import java.util.Set;

public class UnboundedAutomatonModel
        implements AutomatonModel {

    private Automaton model;

    UnboundedAutomatonModel(Automaton model) {

        // set model from parameter
        this.model = model;
    }

    @Override
    public AutomatonModel clone() {
        return null;
    }

    @Override
    public AutomatonModel complement() {
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
    public boolean equals() {
        return false;
    }

    @Override
    public String getAcceptedStringExample() {
        return null;
    }

    @Override
    public int getBound() {
        return 0;
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

        // get one finite string, null if more
        Set<String> strings = this.model.getStrings(1);

        // return if single non-null string in automaton
        return strings != null &&
               strings.size() == 1 &&
               strings.iterator().next() != null;
    }

    @Override
    public AutomatonModel minus(AutomatonModel x) {
        return null;
    }

    @Override
    public void setBound(int newBound) {
        // nothing to do
    }

    @Override
    public AutomatonModel union(AutomatonModel arg) {
        return null;
    }
}
