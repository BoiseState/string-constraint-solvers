package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedReplaceCharUnknown extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "replace(?, ?)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }
}
