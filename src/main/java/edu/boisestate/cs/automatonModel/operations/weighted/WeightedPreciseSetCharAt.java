package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedPreciseSetCharAt extends BinaryWeightedOperation {

    private int index;

    public WeightedPreciseSetCharAt(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "setCharAt(" + index + ", <char>)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton baseAutomaton,
                                WeightedAutomaton argAutomaton) {
        return null;
    }
}
