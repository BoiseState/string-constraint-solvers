package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeightedPreciseSetCharAt
        extends BinaryWeightedOperation {

    private int offset;

    public WeightedPreciseSetCharAt(int offset) {
        this.offset = offset;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton baseAutomaton,
                                WeightedAutomaton argAutomaton) {
        // if start is greater than end or automaton is empty
        if (this.offset < 0 ||
            baseAutomaton.isEmpty() ||
            baseAutomaton.isEmptyString()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }

        // clone base automaton
        WeightedAutomaton clone1 = baseAutomaton.clone();

        // create new initial state
        WeightedState initial = new WeightedState();

        // initialize state set
        Set<WeightedState> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        WeightedAutomaton returnAutomaton = new WeightedAutomaton();
        returnAutomaton.setDeterministic(false);
        returnAutomaton.setInitialState(initial);

        // initialize state map
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        stateMap.put(initial, clone1.getInitialState());

        // create copy of automaton before start
        for (int i = 0; i < this.offset; i++) {
            // initialize next state set
            Set<WeightedState> nextStates = new HashSet<>();

            // get all transistions from each state
            for (WeightedState state : states) {
                // get original state from clone
                WeightedState originalState = stateMap.get(state);

                // add transitions to copied states
                for (WeightedTransition t : originalState.getTransitions()) {
                    // create a copy of the destination state and add to map
                    WeightedState destination = new WeightedState();
                    stateMap.put(destination, t.getDest());

                    // add destination state as next state
                    nextStates.add(destination);

                    // create a transition from the previous state copy
                    state.addTransition(new WeightedTransition(t.getMin(),
                                                               t.getMax(),
                                                               destination,
                                                               t.getWeightInt()));
                }
            }

            // if automaton not long enough
            if (nextStates.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

            // update states with new states
            states = nextStates;
        }

        for (WeightedState state : states) {
            WeightedState originalState = stateMap.get(state);
            Set<WeightedTransition> argTransitions =
                    argAutomaton.getInitialState().getTransitions();
            for (WeightedTransition charT : argTransitions) {
                for (WeightedTransition t : originalState.getTransitions()) {
                    state.addTransition(
                            new WeightedTransition(charT.getMin(),
                                                   charT.getMax(),
                                                   t.getDest(),
                                                   t.getWeightInt()));
                }
            }
        }

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "setCharAt(" + offset + ", <char>)";
    }
}
