/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.*;

@SuppressWarnings("Duplicates")
public class PreciseSetLength
        extends UnaryOperation {
    private int length;

    public PreciseSetLength(int length) {
        // initialize indices from parameters
        this.length = length;
    }

    @Override
    public CharSet charsetTransfer(CharSet arg0) {
        return arg0;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public Automaton op(Automaton a) {
    	//eas per documentation:
    	//StringIndexOutOfBoundsException - if length is negative, greater than length(), or greater than end.
    	
        // if length is greater than end or automaton is empty
        if (this.length < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }

        // create automaton clone
        Automaton clone = a.clone();
        
        //eas: even though length = end and per documentation the
        //string will not be changes, the shorter strings that
        //the automaton represents will throw an exception, thus
        //we have to remove those string from this DFA.

        // create new initial state
        State initial = new State();

        // initialize state set
        Set<State> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        Automaton returnAutomaton = new Automaton();
        returnAutomaton.setInitialState(initial);

        // initialize state map
        Map<State, State> stateMap = new HashMap<>();
        stateMap.put(initial, clone.getInitialState());

        // create copy of automaton before length
        for (int i = 0; i < this.length; i++) {

            // initialize next state set
            Set<State> nextStates = new HashSet<>();

            // get all transitions from each state
            for (State state : states) {

                // if state is copy of existing state
                if (stateMap.containsKey(state)) {
                    // get original state from clone
                    State originalState = stateMap.get(state);

                    // add transitions to copied states
                    for (Transition transition : originalState.getTransitions()) {

                        // create a copy of the destination state and add to map
                        State destination = new State();
                        stateMap.put(destination, transition.getDest());

                        // add destination state as next state
                        nextStates.add(destination);

                        // create a transition from the previous state copy
                        state.addTransition(new Transition(transition.getMin(),
                                                           transition.getMax(),
                                                           destination));
                    }

                    // if accept state, add null character transition
                    if (state.isAccept()) {
                        State destination = new State();
                        state.addTransition(new Transition('\u0000',destination));
                        nextStates.add(destination);
                    }
                } else {
                    // add null character transition
                    State destination = new State();
                    state.addTransition(new Transition('\u0000',destination));
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
        return "PreciseDelete";
    }

}
