package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

public class WeightedReplaceCharFindKnown
        extends UnaryWeightedOperation {

    private char find;

    public WeightedReplaceCharFindKnown(char find) {
        this.find = find;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', ?)";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        WeightedAutomaton b = a.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                if (min <= find && find <= max) {
                    s.addTransition(new WeightedTransition(Character.MIN_VALUE, Character.MAX_VALUE, dest, t.getWeightInt()));
                }
            }
        }
        b.setDeterministic(false);
        b.reduce();
        return b;
    }
}
