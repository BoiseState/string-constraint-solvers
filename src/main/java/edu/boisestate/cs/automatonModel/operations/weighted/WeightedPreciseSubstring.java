/**
 * An extended EJSA operation for a more precise substring operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.PrecisePrefix;
import edu.boisestate.cs.automatonModel.operations.PreciseSuffix;

import java.math.BigInteger;
import java.util.*;

import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .pseudoModelCount;

public class WeightedPreciseSubstring
        extends UnaryWeightedOperation {
    private int end;
    private int start;

    public WeightedPreciseSubstring(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        // if start is greater than end or automaton is empty
        if (start < 0 || start > end || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }

        // create automaton clone
        WeightedAutomaton clone = a.clone();

        // initialize state set
        Set<WeightedState> startStates = new TreeSet<>();
        startStates.add(clone.getInitialState());

        // initialize state weight map
        Map<WeightedState, Integer> stateWeights = new HashMap<>();
        stateWeights.put(clone.getInitialState(), clone.getInitialFactor());

        // create copy of automaton before start
        for (int i = 0; i < this.start; i++) {
            // if automaton not long enough
            if (startStates.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

            // initialize next state set
            Set<WeightedState> nextWeightedStates = new TreeSet<>();
            Map<WeightedState, Integer> nextStateWeights = new HashMap<>();

            // get all transitions from each state
            for (WeightedState state : startStates) {
                // add transitions to copied states
                for (WeightedTransition t : state.getTransitions()) {
                    // create a copy of the destination state and add to map
                    WeightedState dest = t.getDest();
                    nextWeightedStates.add(dest);
                    int size = t.getMax() - t.getMin() + 1;
                    int nextWeight = stateWeights.get(state) * t.getWeightInt() * size;
                    if (nextStateWeights.containsKey(dest)) {
                        nextWeight += nextStateWeights.get(dest);
                    }
                    nextStateWeights.put(dest, nextWeight);
                }
            }

            // update states with new states
            startStates = nextWeightedStates;
            stateWeights = nextStateWeights;
        }

        // initialize needed data structures
        Map<WeightedState, Set<WeightedTransition>> incomingTransitionMap = new HashMap<>();
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        Set<WeightedState> endStates = new TreeSet<>();
        for (WeightedState s : startStates) {
            WeightedState newState = new WeightedState();
            stateMap.put(newState, s);
            incomingTransitionMap.put(newState, new HashSet<WeightedTransition>());
            endStates.add(newState);
        }

        // create copy of automaton before start
        for (int i = start; i < end; i++) {
            // if automaton not long enough
            if (endStates.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

            // initialize next state set
            Set<WeightedState> nextWeightedStates = new HashSet<>();

            // get all transitions from each state
            for (WeightedState state : endStates) {
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
                                                   transition.getWeightInt());
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
            endStates = nextWeightedStates;
        }

        // set end states as accept states
        for (WeightedState s : endStates) {
            s.setAccept(true);
        }

        // TODO: add weights from before and after substring

        WeightedState initial = new WeightedState();
        WeightedAutomaton returnAutomaton = new WeightedAutomaton();
        returnAutomaton.setInitialState(initial);
        List<WeightedStatePair> epsilons = new ArrayList<>();
        for (WeightedState s : startStates) {
                // adjust weights
                int removedWeight = stateWeights.get(s);
                if (s.getTransitions().isEmpty()) {
                    clone.setNumEmptyStrings(removedWeight);
                }
                WeightedState startState = null;
                for (Map.Entry<WeightedState, WeightedState> kv : stateMap.entrySet()) {
                    if (kv.getValue() == s) {
                        startState = kv.getKey();
                    }
                }
                epsilons.add(new WeightedStatePair(initial, startState, removedWeight));
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "substring(" + start + ", " + end + ")";
    }

}
