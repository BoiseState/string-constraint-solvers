/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    	//eas per documentation:
    	//StringIndexOutOfBoundsException - if length is negative, greater than length(), or greater than end.
    	
        // if length is greater than end or automaton is empty
        if (this.length < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicWeightedAutomata.makeEmpty();
        }

        // create automaton clone
        WeightedAutomaton clone = a.clone();
        
        //eas: even though length = end and per documentation the
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

        // create copy of automaton before length
        for (int i = 0; i < this.length; i++) {

            // initialize next state set
            Set<WeightedState> nextStates = new HashSet<>();

            // get all transitions from each state
            for (WeightedState state : states) {

                // if state is copy of existing state
                if (stateMap.containsKey(state)) {
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
                        state.addTransition(new WeightedTransition(transition.getMin(), transition.getMax(), destination, transition.getWeight()));
                    }

                    // if accept state, add null character transition
                    if (state.isAccept()) {
                        WeightedState destination = new WeightedState();
                        state.addTransition(new WeightedTransition('\u0000',destination));
                        nextStates.add(destination);
                    }
                } else {
                    // add null character transition
                    WeightedState destination = new WeightedState();
                    state.addTransition(new WeightedTransition('\u0000',destination));
                    nextStates.add(destination);
                }
            }

            // update states with new states
            states = nextStates;;
        }

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "setLength";
    }

}
