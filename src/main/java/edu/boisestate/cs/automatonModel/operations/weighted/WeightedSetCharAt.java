package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.*;

import java.util.*;

public class WeightedSetCharAt extends BinaryWeightedOperation {

    private final int offset;

    public WeightedSetCharAt(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "setCharAt(" + offset + ", <char>)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton base, WeightedAutomaton arg) {
        return null;
    }
}
