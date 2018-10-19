package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

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
    public WeightedAutomaton op(WeightedAutomaton a) {
        WeightedAutomaton b = a.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                WeightedState dest = t.getDest();
                s.addTransition(new WeightedTransition(replace, dest, t.getWeightInt()));
            }
        }
        b.setDeterministic(false);
        b.reduce();
        return b;
    }
}
