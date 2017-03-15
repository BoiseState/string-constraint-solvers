package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedReplaceCharFindKnown
        extends UnaryWeightedOperation {

    private char find;

    public WeightedReplaceCharFindKnown(char find) {
        this.find = find;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', ?)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }
}
