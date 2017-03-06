package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAllSuffixes
        extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allSuffixes";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return BasicWeightedAutomata.makeEmpty();
    }
}
