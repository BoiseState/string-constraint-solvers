package edu.boisestate.cs.automatonModel.operations;


import edu.boisestate.cs.automaton.WeightedAutomaton;

public interface UnaryWeightedAutomatonOp {
    WeightedAutomaton op(WeightedAutomaton a1);
}
