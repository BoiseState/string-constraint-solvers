package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

public class WeightedReplaceCharUnknown extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "replace(?, ?)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        WeightedAutomaton b = a.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                WeightedState dest = t.getDest();
                transitions.remove(t);
                s.addTransition(new WeightedTransition(Character.MIN_VALUE, Character.MAX_VALUE, dest, t.getWeightInt()));
            }
        }
        b.setDeterministic(false);
        b.reduce();
        return b;
    }
}
