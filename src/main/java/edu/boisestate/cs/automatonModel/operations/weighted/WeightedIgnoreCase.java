package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 */
public class WeightedIgnoreCase
        extends UnaryWeightedOperation {

    /**
     * Unary operation on automata.
     *
     * @param a
     *         input automaton, should not be modified
     *
     * @return output automaton
     */
    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {

        // clone automaton
        WeightedAutomaton clone = a.clone();

        // for all states
        for (WeightedState state : clone.getStates()) {

            // all transitions from state
            Set<WeightedTransition> transitions = state.getTransitions();

            // for all transitions in current set of transitions
            for (WeightedTransition t : new ArrayList<>(transitions)) {

                // get transition values
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                int weight = t.getWeightInt();

                // if transition represents subset of characters
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {

                    // for each character represented in transition
                    for (int i = min; i <= max; i++) {

                        // get i as char
                        char c = (char) i;

                        // duplicate transition for single char if needed
                        if (min != max) {
                            transitions.add(new WeightedTransition(c, dest, weight));
                        }

                        // if char is uppercase
                        if (Character.isUpperCase(c)) {

                            // add corresponding lowercase transition
                            char lc = Character.toLowerCase(c);
                            WeightedTransition lcTrans = new WeightedTransition(lc, dest, weight);
                            transitions.add(lcTrans);
                        }

                        // if char is lowercase
                        if (Character.isLowerCase(c)) {

                            // add corresponding uppercase transition
                            char uc = Character.toUpperCase(c);
                            WeightedTransition ucTrans = new WeightedTransition(uc, dest, weight);
                            transitions.add(ucTrans);
                        }
                    }
                }
                if (min != max) {
                    transitions.remove(t);

                }
            }
        }

        clone.setDeterministic(false);
        clone.reduce();

        return clone;
    }

    /**
     * Returns name of this operation.
     */
    @Override
    public String toString() {
        return "ignoreCase";
    }
}
