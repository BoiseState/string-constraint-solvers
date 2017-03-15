package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

public class WeightedReplaceChar
        extends UnaryWeightedOperation {

    private final char find;
    private final char replace;

    public WeightedReplaceChar(char find, char replace) {
        this.find = find;
        this.replace = replace;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', '" + replace + "')";
    }
}
