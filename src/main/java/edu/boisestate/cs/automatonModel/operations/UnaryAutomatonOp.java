package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;

public interface UnaryAutomatonOp {
    Automaton op(Automaton a1);
}
