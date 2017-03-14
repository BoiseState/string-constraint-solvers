package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

public class WeightedToLowerCase extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "toLowerCase()";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        WeightedAutomaton clone = automaton.clone();
        for (WeightedState s : clone.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<WeightedTransition>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                int weight = t.getWeight();
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {
                    transitions.remove(t);
                    for (int c = min; c <= max; c++) {
                        transitions.add(new WeightedTransition(Character.toLowerCase((char) c), dest, weight));
                    }
                }
            }
        }
        clone.setDeterministic(false);
        clone.reduce();
        return clone;
    }
}
