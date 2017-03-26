/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.BinaryOperation;

import java.util.*;

@SuppressWarnings("Duplicates")
public class PreciseSetCharAt
        extends BinaryOperation {
    private int offset;

    public PreciseSetCharAt(int offset) {
        this.offset = offset;
    }

    @Override
    public CharSet charsetTransfer(CharSet arg0, CharSet arg1) {
        return arg0.union(arg1);
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public Automaton op(Automaton baseAutomaton, Automaton argAutomaton) {

        // if start is greater than end or automaton is empty
        if (this.offset < 0 || baseAutomaton.isEmpty() || baseAutomaton.isEmptyString()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }

        // clone base automaton
        Automaton clone1 = baseAutomaton.clone();

        // create new initial state
        State initial = new State();

        // initialize state set
        Set<State> states = new HashSet<>();
        states.add(initial);

        // create return automaton from initial state
        Automaton returnAutomaton = new Automaton();
        returnAutomaton.setDeterministic(false);
        returnAutomaton.setInitialState(initial);

        // initialize state map
        Map<State, State> stateMap = new HashMap<>();
        stateMap.put(initial, clone1.getInitialState());

        // create copy of automaton before start
        for (int i = 0; i < this.offset; i++) {
            // initialize next state set
            Set<State> nextStates = new HashSet<>();

            // get all transistions from each state
            for (State state : states) {
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
            }

            // if automaton not long enough
            if (nextStates.isEmpty()) {
                return BasicAutomata.makeEmpty();
            }

            // update states with new states
            states = nextStates;
        }

        for (State state : states) {
            State originalState = stateMap.get(state);
            for (Transition charT : argAutomaton.getInitialState().getTransitions()) {
                for (Transition t : originalState.getTransitions()) {
                    state.addTransition(new Transition(charT.getMin(),
                                                       charT.getMax(),
                                                       t.getDest()));
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
