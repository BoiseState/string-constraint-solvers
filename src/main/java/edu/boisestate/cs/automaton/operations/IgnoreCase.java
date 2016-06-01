package edu.boisestate.cs.automaton.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 */
public class IgnoreCase extends UnaryOperation {

    /**
     * Unary operation on automata.
     *
     * @param a
     *         input automaton, should not be modified
     *
     * @return output automaton
     */
    @Override
    public Automaton op(Automaton a) {

        // clone automaton
        Automaton clone = a.clone();

        // for all states
        for (State state : clone.getStates()) {

            // all transitions from state
            Set<Transition> transitions = state.getTransitions();

            // for all transitions in current set of transitions
            for (Transition t : new ArrayList<>(transitions)) {

                // get transition values
                char min = t.getMin();
                char max = t.getMax();
                State dest = t.getDest();

                // if transition represents subset of characters
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {

                    // for each character represented in transition
                    for (int i = min; i <= max; i++) {

                        // get i as char
                        char c = (char) i;

                        // if char is uppercase
                        if (Character.isUpperCase(c)) {

                            // add corresponding lowercase transition
                            char lc = Character.toLowerCase(c);
                            Transition lcTrans = new Transition(lc, dest);
                            transitions.add(lcTrans);
                        }

                        // if char is lowercase
                        if (Character.isLowerCase(c)) {

                            // add corresponding uppercase transition
                            char uc = Character.toUpperCase(c);
                            Transition ucTrans = new Transition(uc, dest);
                            transitions.add(ucTrans);
                        }
                    }
                }
            }
        }

        clone.setDeterministic(false);
        clone.reduce();
        clone.minimize();

        return clone;
    }

    /**
     * Transfer function for character set analysis.
     *
     * @param a
     */
    @Override
    public CharSet charsetTransfer(CharSet a) {
        CharSet lower = a.toLowerCase();
        CharSet upper = a.toUpperCase();
        return lower.union(upper);
    }

    /**
     * Returns name of this operation.
     */
    @Override
    public String toString() {
        return "ignoreCase";
    }

    /**
     * Returns priority of this operation. When approximating operation loops in
     * grammars, operations with high priority are considered first.
     */
    @Override
    public int getPriority() {
        return 2;
    }
}
