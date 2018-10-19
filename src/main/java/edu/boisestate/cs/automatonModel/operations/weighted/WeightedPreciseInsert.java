/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;


import edu.boisestate.cs.automaton.*;

import java.util.*;

@SuppressWarnings("Duplicates")
public class WeightedPreciseInsert
        extends BinaryWeightedOperation {
    private int offset;

    public WeightedPreciseInsert(int offset) {
        this.offset = offset;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton base, WeightedAutomaton arg) {
        // if start is greater than end or automaton is empty
        if (this.offset < 0 || base.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }

        // clone base automaton
        WeightedAutomaton clone1 = base.clone();

        // create new initial state
        WeightedState initial = new WeightedState();

        // initialize state set
        Set<WeightedState> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        WeightedAutomaton returnAutomaton = new WeightedAutomaton();
        returnAutomaton.setInitialState(initial);

        // initialize state map
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        stateMap.put(initial, clone1.getInitialState());

        // create copy of automaton before start
        for (int i = 0; i < this.offset; i++) {
            // if automaton not long enough
            if (states.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

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

            // update states with new states
            states = nextStates;
        }

        // add epsilon transitions
        List<WeightedStatePair> epsilons = new ArrayList<>();
        for (WeightedState state : states) {
            WeightedAutomaton argClone = arg.clone();
            epsilons.add(
                    new WeightedStatePair(state, argClone.getInitialState()));
            for (WeightedState argAccept : argClone.getAcceptStates()) {
                argAccept.setAccept(false);
                epsilons.add(
                        new WeightedStatePair(argAccept, stateMap.get(state)));
            }
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "insert(" + offset + ", <String>)";
    }

}
