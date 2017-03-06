package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAllPrefixes
        extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allPrefixes";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return BasicWeightedAutomata.makeEmpty();
    }
}
