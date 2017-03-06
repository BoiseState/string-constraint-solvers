package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAllSubstrings extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allSubstrings";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return BasicWeightedAutomata.makeEmpty();
    }
}
