/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class WeightedPreciseSetLength
        extends UnaryWeightedOperation {
    private int length;

    public WeightedPreciseSetLength(int length) {
        // initialize indices from parameters
        this.length = length;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }

    @Override
    public String toString() {
        return "setLength(" + length + ")";
    }

}
