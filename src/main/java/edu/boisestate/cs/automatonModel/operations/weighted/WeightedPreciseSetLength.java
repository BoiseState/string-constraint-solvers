package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.*;

import java.util.*;

@SuppressWarnings("Duplicates")
public class WeightedPreciseSetLength
        extends UnaryWeightedOperation {
    private int length;

    public WeightedPreciseSetLength(int length) {
        // initialize indices from parameters
        this.length = length;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        // TODO: adjust transition weights
        // if length is greater than end or automaton is empty
        if (this.length < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        } else if (length == 0) {
            return BasicWeightedAutomata.makeEmptyString();
        }

        // create automaton clone
        WeightedAutomaton clone = a.clone();

        // create new initial state
        WeightedState initial = new WeightedState();

        // initialize state set
        Set<WeightedState> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        WeightedAutomaton returnAutomaton = new WeightedAutomaton();
        returnAutomaton.setInitialState(initial);

        // create automaton of \u0000 chars
        WeightedAutomaton nulls = BasicWeightedAutomata.makeChar('\u0000')
                                       .repeat(length, length);

        // initialize null state
        WeightedState nullState = nulls.getInitialState();

        // initialize epsilon list
        List<WeightedStatePair> epsilons = new ArrayList<>();

        // initialize state map
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        stateMap.put(initial, clone.getInitialState());

        // create copy of automaton before length
        for (int i = 0; i < this.length; i++) {

            // initialize next state set
            Set<WeightedState> nextStates = new HashSet<>();

            // get all transitions from each state
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
                                                       destination));
                }

                // if accept state, add null character transition
                if (originalState.isAccept()) {
                    epsilons.add(new WeightedStatePair(state, nullState));
                }
            }

            // update states with new states
            states = nextStates;

            // update null state
            nullState = nullState.step('\u0000').getState();
        }

        for (WeightedState s : states) {
            epsilons.add(new WeightedStatePair(s, nullState));
        }

        // add epsilons
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "setLength(" + length + ")";
    }

}
