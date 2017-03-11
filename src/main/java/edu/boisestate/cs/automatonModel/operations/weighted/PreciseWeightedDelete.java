/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.*;

import java.util.*;

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
    	//eas per documentation:
    	//StringIndexOutOfBoundsException - if start is negative, greater than length(), or greater than end.
    	
        // if start is greater than end or automaton is empty
        if (this.start <0 || this.start > this.end || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }

        // create automaton clone
        WeightedAutomaton clone = a.clone();
        
        //eas: even though start = end and per documentation the 
        //string will not be changes, the shorter strings that
        //the automaton represents will throw an exception, thus
        //we have to remove those string from this DFA.

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
        Map<WeightedState, Set<WeightedState>> incomingStateMap = new HashMap<>();
        incomingStateMap.put(initial, new HashSet<WeightedState>());

        // create copy of automaton before start
        for (int i = 0; i < this.start; i++) {
            // if automaton not long enough
            if (states.isEmpty()) {
                return BasicWeightedAutomata.makeEmpty();
            }

            // initialize next state set
            Set<WeightedState> nextWeightedStates = new HashSet<>();

            // get all transistions from each state
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
                    state.addTransition(new WeightedTransition(transition.getMin(), transition.getMax(), destination));
                }
            }

            // update states with new states
            states = nextWeightedStates;;
        }

        // initialize end states map
        Map<WeightedState, Set<WeightedState>> endWeightedStatesMap = new HashMap<>();
        for (WeightedState state : states) {
            Set<WeightedState> stateSet = new HashSet<>();
            stateSet.add(stateMap.get(state));
            endWeightedStatesMap.put(state, stateSet);
        }

        // walk automaton from start index to end index
        for (int i = start; i < end; i++) {
            for (WeightedState keyWeightedState : states) {
                // get existing state set
                Set<WeightedState> stateSet = endWeightedStatesMap.get(keyWeightedState);

                // if automaton is long enough
                if (!stateSet.isEmpty()) {
                    // initialize next state set
                    Set<WeightedState> nextWeightedStates = new HashSet<>();

                    // get all transistions from each state
                    for (WeightedState state : stateSet) {

                        // add transitions to copied states
                        for (WeightedTransition transition : state.getTransitions()) {
                            // add destination state as next state
                            nextWeightedStates.add(transition.getDest());
                        }
                    }

                    // update end states with new states
                    endWeightedStatesMap.put(keyWeightedState, nextWeightedStates);
                }
            }
        }

        // handle empty end state sets
        for (WeightedState state : states) {
            if (endWeightedStatesMap.get(state).isEmpty()) {
                // create new empty accepting state
                WeightedState endWeightedState = new WeightedState();
                endWeightedState.setAccept(true);

                // create set for new accepting state
                Set<WeightedState> endWeightedStates = new HashSet<>();
                endWeightedStates.add(endWeightedState);

                // set end states map from set
                endWeightedStatesMap.put(state, endWeightedStates);
            }
        }

        // add epsilon transitions from state set and map
        List<WeightedStatePair> epsilons = new ArrayList<>();
        for (WeightedState state : states) {
            for (WeightedState endWeightedState: endWeightedStatesMap.get(state)){
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
