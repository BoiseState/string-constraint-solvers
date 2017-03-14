/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.*;

import java.math.BigInteger;
import java.util.*;

import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .pseudoModelCount;

@SuppressWarnings("Duplicates")
public class PreciseWeightedDelete
        extends UnaryWeightedOperation {
    private int end;
    private int start;

    public PreciseWeightedDelete(int start, int end) {
        // initialize indices from parameters
        this.start = start;
        this.end = end;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        // if start is greater than end or automaton is empty
        if (this.start <0 || this.start > this.end || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
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

        // initialize state map
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        stateMap.put(initial, clone.getInitialState());

        // initialize state map
        Map<WeightedState, Set<WeightedTransition>> incomingTransitionMap = new HashMap<>();
        incomingTransitionMap.put(initial, new HashSet<WeightedTransition>());

        // create copy of automaton before start
        for (int i = 0; i < this.start; i++) {
            // if automaton not long enough
            if (states.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

            // initialize next state set
            Set<WeightedState> nextWeightedStates = new HashSet<>();

            // get all transitions from each state
            for (WeightedState state : states) {
                // get original state from clone
                WeightedState originalWeightedState = stateMap.get(state);

                // add transitions to copied states
                for (WeightedTransition transition : originalWeightedState.getTransitions()) {
                    // create a copy of the destination state and add to map
                    WeightedState destination = new WeightedState();
                    stateMap.put(destination, transition.getDest());

                    // add destination state as next state
                    nextWeightedStates.add(destination);

                    // create a transition from the previous state copy
                    WeightedTransition newTransition =
                            new WeightedTransition(transition.getMin(),
                                                   transition.getMax(),
                                                   destination,
                                                   transition.getWeight());
                    state.addTransition(newTransition);

                    // update incoming state map
                    Set<WeightedTransition> inTransitions = incomingTransitionMap.get(destination);
                    if (inTransitions == null) {
                        inTransitions = new HashSet<>();
                        incomingTransitionMap.put(destination, inTransitions);
                    }
                    inTransitions.add(newTransition);
                }
            }

            // update states with new states
            states = nextWeightedStates;
        }

        // initialize end states map
        Map<WeightedState, Set<WeightedState>> endStatesMap = new HashMap<>();
        Map<WeightedState, BigInteger> stateWeights = new HashMap<>();
        for (WeightedState state : states) {
            Set<WeightedState> stateSet = new HashSet<>();
            stateSet.add(stateMap.get(state));
            endStatesMap.put(state, stateSet);

            // get removed weights
            if (end > start) {
                BigInteger removedCount = pseudoModelCount(stateMap.get(state), end - start, BigInteger.ONE);
                stateWeights.put(state, removedCount);
            } else {
                stateWeights.put(state, BigInteger.ONE);
            }

        }

        // walk automaton from start index to end index
        for (int i = start; i < end; i++) {
            for (WeightedState keyWeightedState : states) {
                // get existing state set
                Set<WeightedState> stateSet = endStatesMap.get(keyWeightedState);

                // if automaton is long enough
                if (!stateSet.isEmpty()) {
                    // initialize next state set
                    Set<WeightedState> nextWeightedStates = new HashSet<>();

                    // get all transitions from each state
                    for (WeightedState state : stateSet) {

                        // add transitions to copied states
                        for (WeightedTransition transition : state.getTransitions()) {
                            // add destination state as next state
                            nextWeightedStates.add(transition.getDest());
                        }
                    }

                    // update end states with new states
                    endStatesMap.put(keyWeightedState, nextWeightedStates);
                }
            }
        }


        // handle empty end state sets
        for (WeightedState state : states) {
            if (endStatesMap.get(state).isEmpty()) {
                // create new empty accepting state
                WeightedState endWeightedState = new WeightedState();
                endWeightedState.setAccept(true);

                // create set for new accepting state
                Set<WeightedState> endWeightedStates = new HashSet<>();
                endWeightedStates.add(endWeightedState);

                // set end states map from set
                endStatesMap.put(state, endWeightedStates);
            }
        }

        // update previous transitions with removed weights
        if (start == 0) {
            BigInteger removedCount = BigInteger.ZERO;
            for (WeightedState state : states) {
                BigInteger removedWeights = stateWeights.get(state);
                removedCount = removedCount.add(removedWeights);
            }
            if (removedCount.compareTo(BigInteger.ZERO) > 0 ) {
                int num = a.getInitialFactor() * removedCount.intValue();
                returnAutomaton.setInitialFactor(num);
            }
        }
        for (WeightedState state : states) {
            for (WeightedTransition t : incomingTransitionMap.get(state)) {
                int removedWeight = stateWeights.get(state).intValue();
                int newWeight = t.getWeight() * removedWeight;
                t.setWeight(newWeight);
            }
        }

        // add epsilon transitions from state set and map
        List<WeightedStatePair> epsilons = new ArrayList<>();
        for (WeightedState state : states) {
            for (WeightedState endWeightedState: endStatesMap.get(state)){
                epsilons.add(new WeightedStatePair(state, endWeightedState));
            }
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "delete(" + start + ", " + end + ")";
    }

}
