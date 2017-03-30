package edu.boisestate.cs.automatonModel.operations;

import edu.boisestate.cs.automaton.WeightedAutomaton;

public interface BinaryWeightedAutomatonOp {
    WeightedAutomaton op(WeightedAutomaton a1, WeightedAutomaton a2);
}
