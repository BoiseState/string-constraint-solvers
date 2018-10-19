package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

public class WeightedToUpperCase
        extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "toUpperCase()";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        WeightedAutomaton b = automaton.clone();
        for (WeightedState s : b.getStates()) {
            Set<WeightedTransition> transitions = s.getTransitions();
            for (WeightedTransition t : new ArrayList<WeightedTransition>(transitions)) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                int weight = t.getWeightInt();
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {
                    transitions.remove(t);
                    for (int c = min; c <= max; c++) {
                        String up = String.valueOf((char)c).toUpperCase();
                        if (up.length() == 1) {
                            WeightedTransition newT = new WeightedTransition(Character.toUpperCase((char) c), dest, weight);
                            if (transitions.contains(newT)) {
                                transitions.remove(newT);
                                int newWeight = weight * 2;
                                transitions.add(new WeightedTransition(newT.getMin(), dest, newWeight));
                            } else {
                                transitions.add(newT);
                            }
                        } else {
                            // YES some characters translate to more than one character when turned upper case
                            // for example the German character "�" becomes "SS"
                            WeightedState lastState = s;
                            for (int i=0; i<up.length()-1; i++) {
                                char ch = up.charAt(i);
                                WeightedState state = new WeightedState();
                                lastState.addTransition(new WeightedTransition(ch, state, weight));
                                lastState = state;
                            }
                            lastState.addTransition(new WeightedTransition(up.charAt(up.length()-1), dest, weight));
                        }
                    }
                }
            }
        }
        b.setDeterministic(false);
        b.reduce();
        return b;
    }
}
