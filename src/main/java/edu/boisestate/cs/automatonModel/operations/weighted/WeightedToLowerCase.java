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
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                int weight = t.getWeightInt();
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {
                    transitions.remove(t);
                    for (int c = min; c <= max; c++) {
                        WeightedTransition newT = new WeightedTransition(Character.toLowerCase((char) c), dest, weight);
                        if (transitions.contains(newT)) {
                            transitions.remove(newT);
                            int newWeight = weight * 2;
                            transitions.add(new WeightedTransition(newT.getMin(), dest, newWeight));
                        } else {
                            transitions.add(newT);
                        }
                    }
                }
            }
        }
        clone.setDeterministic(false);
        clone.reduce();
        return clone;
    }
}
