/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.*;

@SuppressWarnings("Duplicates")
public class PreciseDelete
        extends UnaryOperation {
    private int end;
    private int start;

    public PreciseDelete(int start, int end) {
        // initialize indices from parameters
        this.start = start;
        this.end = end;
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
        // create automaton clone
        Automaton clone = a.clone();

        // if start is greater than end, return empty automaton (exception)
        if (this.start > this.end) {
            BasicAutomata.makeEmpty();
        }

        // initialize state set
        Set<State> states = new HashSet<>();
        states.add(clone.getInitialState());

        // create new initial state
        State initial = new State();

        // create return automaton from initial state
        Automaton returnAutomaton = new Automaton();
        returnAutomaton.setInitialState(initial);

        // initialize state map
//        Map<State, Set<State>> startStatesMap = new HashMap<>();
        Map<State, State> stateMap = new HashMap<>();
        stateMap.put(clone.getInitialState(), initial);

        // create copy of automaton before start
        for (int i = 0; i < this.start; i++) {
            // if automaton not long enough
            if (states.isEmpty()) {
                return BasicAutomata.makeEmpty();
            }

            // initialize next state set
            Set<State> nextStates = new HashSet<>();

            // get all transistions from each state
            for (State state : states) {
                // get or create copy state
                State copy;
                if (stateMap.containsKey(state)) {
                    copy = stateMap.get(state);
                } else {
                    copy = new State();
                    stateMap.put(state, copy);
                }

                // add transitions to copied states
                for (Transition transition : state.getTransitions()) {
                    // add destination state as next state
                    nextStates.add(transition.getDest());

                    // create a copy of the destination state and add to map
                    State destination = new State();
                    stateMap.put(transition.getDest(), destination);

                    // create a transition from the previous state copy
                    copy.addTransition(new Transition(transition.getMin(),
                                                      transition.getMax(),
                                                      destination));
                }
            }

            // update states with new states
            states.clear();
            states.addAll(nextStates);
        }

        // initialize end states map
        Map<State, Set<State>> endStatesMap = new HashMap<>();
        for (State state : states) {
            Set<State> stateSet = new HashSet<>();
            stateSet.add(state);
            endStatesMap.put(state, stateSet);
        }

        // walk automaton from start index to end index
        for (int i = start; i < end; i++) {
            for (State keyState : states) {
                // get existing state set
                Set<State> stateSet = endStatesMap.get(keyState);

                // if automaton is long enough
                if (!stateSet.isEmpty()) {
                    // initialize next state set
                    Set<State> nextStates = new HashSet<>();

                    // get all transistions from each state
                    for (State state : stateSet) {

                        // add transitions to copied states
                        for (Transition transition : state.getTransitions()) {
                            // add destination state as next state
                            nextStates.add(transition.getDest());
                        }
                    }

                    // update end states with new states
                    endStatesMap.put(keyState, nextStates);
                }
            }
        }

        // handle empty end state sets
        for (State state : states) {
            if (endStatesMap.get(state).isEmpty()) {
                // create new empty accepting state
                State endState = new State();
                endState.setAccept(true);

                // create set for new accepting state
                Set<State> endStates = new HashSet<>();
                endStates.add(endState);

                // set end states map from set
                endStatesMap.put(state, endStates);
            }
        }

        // add epsilon transitions from state set and map
        List<StatePair> epsilons = new ArrayList<>();
        for (State state : states) {
            for (State endState: endStatesMap.get(state)){
                State fromState = stateMap.get(state);
                epsilons.add(new StatePair(fromState, endState));
            }
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // minimize and determinize deleted automaton
        returnAutomaton.minimize();
        returnAutomaton.determinize();

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "PreciseDelete";
    }

}
