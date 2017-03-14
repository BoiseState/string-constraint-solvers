package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.*;

import java.util.*;

public class WeightedSetCharAt extends BinaryWeightedOperation {

    private final int offset;

    public WeightedSetCharAt(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "setCharAt";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton baseAutomaton, WeightedAutomaton argAutomaton) {
        // if start is greater than end or automaton is empty
        if (this.offset < 0 || baseAutomaton.isEmpty()) {
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
                for (WeightedTransition transition : originalState.getTransitions()) {
                    // create a copy of the destination state and add to map
                    WeightedState destination = new WeightedState();
                    stateMap.put(destination, transition.getDest());

                    // add destination state as next state
                    nextStates.add(destination);

                    // create a transition from the previous state copy
                    state.addTransition(new WeightedTransition(transition.getMin(),
                                                               transition.getMax(),
                                                               destination,
                                                               transition.getWeight()));
                }
            }

            // update states with new states
            states = nextStates;
        }

        // add epsilon transitions
        List<WeightedStatePair> epsilons = new ArrayList<>();
        for (WeightedState state : states) {
            WeightedAutomaton argClone = argAutomaton.clone();
            epsilons.add(new WeightedStatePair(state, argClone.getInitialState()));
            for (WeightedState argAccept : argClone.getAcceptStates()) {
                argAccept.setAccept(false);
                epsilons.add(new WeightedStatePair(argAccept, stateMap.get(state)));
            }
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }
}
