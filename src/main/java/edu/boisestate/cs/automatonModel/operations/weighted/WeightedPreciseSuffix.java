/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class WeightedPreciseSuffix
        extends UnaryWeightedOperation {

    public WeightedPreciseSuffix(int start) {
        this.start = start;
    }

    private int start;

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        // TODO: adjust transition weights
        // if start is greater than end or automaton is empty
        if (this.start < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }
        WeightedAutomaton clone = a.clone();
        LinkedList<WeightedState> states = new LinkedList<>();
        states.add(clone.getInitialState());
        for (int i = 0; i < start; i++) {
            LinkedList<WeightedTransition> transitions = new LinkedList<>();

            while (states.size() > 0) {
                transitions.addAll(states.removeFirst().getTransitions());
            }
            while (transitions.size() > 0) {
                states.add(transitions.removeFirst().getDest());
            }
        }
        Set<WeightedStatePair> epsilons = new HashSet<>();
        WeightedState initial = new WeightedState();
        while (states.size() > 0) {
            epsilons.add(new WeightedStatePair(initial, states.removeFirst()));

        }
        clone.setInitialState(initial);
        clone.addEpsilons(epsilons);
        clone.minimize();
        return clone;
    }

    @Override
    public String toString() {
        return "suffix(" + start + ")";
    }

}
