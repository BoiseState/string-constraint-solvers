package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

public class PreciseTrim
        extends UnaryOperation {
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PreciseTrim;
    }

    @Override
    public Automaton op(Automaton a) {
        return null;
    }

    @Override
    public CharSet charsetTransfer(CharSet a) {
        return a;
    }

    @Override
    public String toString() {
        return "trim";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
