package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;

import static edu.boisestate.cs.automaton.AutomatonOperations.boundAutomaton;

public class AggregateAutomataModel
        implements AutomatonModel {

    private Automaton[] automata;

    Automaton[] getAutomata() {
        return automata;
    }

    AggregateAutomataModel(Automaton[] automata) {
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

    @Override
    public AutomatonModel clone() {

        // declare clone model
        AggregateAutomataModel cloneModel;

        try {

            // create clone via super as specified by Java
            Object clone = super.clone();

            // cas object clone
            cloneModel = (AggregateAutomataModel) clone;

            // set automata for deep copy
            cloneModel.setAutomata(this.automata);

        } catch (CloneNotSupportedException e) {

            // create new model from existing automata
            cloneModel = new AggregateAutomataModel(this.automata);

        }

        // return clone model
        return cloneModel;
    }

    @Override
    public AutomatonModel complement() {
        return null;
    }

    @Override
    public boolean equals() {
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
        return false;
    }

    @Override
    public void setBound(int newBound) {

    }

    @Override
    public AutomatonModel minus(AutomatonModel x) {
        return null;
    }
}
