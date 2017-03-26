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
    public Automaton op(Automaton a) {
        //eas per documentation:
        //StringIndexOutOfBoundsException - if length is negative, greater
        // than length(), or greater than end.

        // if length is greater than end or automaton is empty
        if (this.length < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        } else if (length == 0) {
            return BasicAutomata.makeEmptyString();
        }

        // create automaton clone
        Automaton clone = a.clone();

        // create new initial state
        State initial = new State();

        // initialize state set
        Set<State> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        Automaton returnAutomaton = new Automaton();
        returnAutomaton.setInitialState(initial);

        // create automaton of \u0000 chars
        Automaton nulls = BasicAutomata.makeChar('\u0000')
                                       .repeat(length, length);

        // initialize null state
        State nullState = nulls.getInitialState();

        // initialize epsilon list
        List<StatePair> epsilons = new ArrayList<>();

        // initialize state map
        Map<State, State> stateMap = new HashMap<>();
        stateMap.put(initial, clone.getInitialState());

        // create copy of automaton before length
        for (int i = 0; i < this.length; i++) {

            // initialize next state set
            Set<State> nextStates = new HashSet<>();

            // get all transitions from each state
            for (State state : states) {

                // get original state from clone
                State originalState = stateMap.get(state);

                // add transitions to copied states
                for (Transition t : originalState.getTransitions()) {

                    // create a copy of the destination state and add to map
                    State destination = new State();
                    stateMap.put(destination, t.getDest());

                    // add destination state as next state
                    nextStates.add(destination);

                    // create a transition from the previous state copy
                    state.addTransition(new Transition(t.getMin(),
                                                       t.getMax(),
                                                       destination));
                }

                // if accept state, add null character transition
                if (originalState.isAccept()) {
                    epsilons.add(new StatePair(state, nullState));
                }
            }

            // update states with new states
            states = nextStates;

            // update null state
            nullState = nullState.step('\u0000');
        }

        for (State s : states) {
            epsilons.add(new StatePair(s, nullState));
        }

        // add epsilons
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public CharSet charsetTransfer(CharSet arg0) {
        return arg0;
    }

    @Override
    public String toString() {
        return "PreciseDelete";
    }

    @Override
    public int getPriority() {
        return 4;
    }

}
