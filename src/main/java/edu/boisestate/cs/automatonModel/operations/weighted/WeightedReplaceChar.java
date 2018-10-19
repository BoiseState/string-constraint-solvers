package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedMinimizationOperations;
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
    public WeightedAutomaton op(WeightedAutomaton a) {
        WeightedAutomaton b = a.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                if (min <= find && find <= max) {
                    transitions.remove(t);
                    s.addTransition(new WeightedTransition(replace, dest, t.getWeightInt()));
                    if (min < find) {
                        s.addTransition(new WeightedTransition(min, (char) (find - 1), dest, t.getWeightInt()));
                    }
                    if (find < max) {
                        s.addTransition(new WeightedTransition((char) (find + 1), max, dest, t.getWeightInt()));
                    }
                }
            }
        }
        b.setDeterministic(false);
        b.reduce();
//        WeightedMinimizationOperations.minimizeBrzozowski(b);
        return b;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', '" + replace + "')";
    }
}
