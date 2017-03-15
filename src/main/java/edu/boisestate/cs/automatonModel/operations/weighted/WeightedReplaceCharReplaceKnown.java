package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedReplaceCharReplaceKnown
        extends UnaryWeightedOperation {

    private char replace;

    public WeightedReplaceCharReplaceKnown(char replace) {
        this.replace = replace;
    }

    @Override
    public String toString() {
        return "replace(?, '" + replace + "')";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }
}
