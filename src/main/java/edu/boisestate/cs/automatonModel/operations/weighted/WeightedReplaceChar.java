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
        WeightedAutomaton b = automaton.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                int weight = t.getWeight();
                if (min <= find && find <= max) {
                    transitions.remove(t);
                    transitions.add(new WeightedTransition(replace, dest, weight));
                    if (min < find) {
                        transitions.add(new WeightedTransition(min, (char) (find - 1), dest, weight));
                    }
                    if (find < max) {
                        transitions.add(new WeightedTransition((char) (find + 1), max, dest, weight));
                    }
                }
            }
        }
        b.setDeterministic(false);
        return b;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', '" + replace + "')";
    }
}
