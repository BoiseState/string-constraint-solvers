package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;

public interface BinaryAutomatonOp {
    Automaton op(Automaton a1, Automaton a2);
}
