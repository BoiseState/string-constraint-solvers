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
    private int removed;

    public PreciseDelete(int start, int end) {
        // initialize indices from parameters
        this.start = start;
        this.end = end;
        this.removed = 0;
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
    	//StringIndexOutOfBoundsException - if start is negative, greater than length(), or greater than end.
    	
        // if start is greater than end or automaton is empty
        if (this.start <0 || this.start > this.end || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }

        // create automaton clone
        Automaton clone = a.clone();
        
        //eas: even though start = end and per documentation the 
        //string will not be changed, the shorter strings that
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

            // update states with new states
            states = nextStates;
        }

        // initialize end states map
        Map<State, Set<State>> endStatesMap = new HashMap<>();
//        Map<State, Map<State, Integer>> statesRemovedMap = new HashMap<>();
        for (State state : states) {
            Set<State> stateSet = new HashSet<>();
            stateSet.add(stateMap.get(state));
            endStatesMap.put(state, stateSet);
//            Map<State, Integer> statesRemoved = new HashMap<>();
//            statesRemoved.put(stateMap.get(state), 1);
//            statesRemovedMap.put(state, statesRemoved);
        }

        // walk automaton from start index to end index
        for (int i = start; i < end; i++) {
            for (State keyState : states) {
                // get existing state set
                Set<State> stateSet = endStatesMap.get(keyState);
//                Map<State, Integer> statesRemoved = statesRemovedMap.get(keyState);

                // if automaton is long enough
                if (!stateSet.isEmpty()) {
                    // initialize next state set
                    Set<State> nextStates = new HashSet<>();
//                    Map<State, Integer> nextStatesRemoved = new HashMap<>();

                    // get all transistions from each state
                    for (State state : stateSet) {

                        // add transitions to copied states
                        for (Transition t : state.getTransitions()) {
                            // add destination state as next state
                            State dest = t.getDest();
                            nextStates.add(dest);
//                            int size = t.getMax() - t.getMin() + 1;
//                            int nextRemove = statesRemoved.get(state) * size;
//                            if (nextStatesRemoved.containsKey(dest)) {
//                                nextRemove += nextStatesRemoved.get(dest);
//                            }
//                            nextStatesRemoved.put(dest, nextRemove);
                        }
                    }

                    // update end states with new states
                    endStatesMap.put(keyState, nextStates);
//                    statesRemovedMap.put(keyState, nextStatesRemoved);
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

                // update removed
//                Map<State, Integer> statesRemoved = new HashMap<>();
//                BigInteger mc = StringModelCounter.ModelCount(stateMap.get(state), start-end);
//                statesRemoved.put(endState, mc.intValue());
//                statesRemovedMap.put(state, statesRemoved);
            }
        }

        // add epsilon transitions from state set and map
        List<StatePair> epsilons = new ArrayList<>();
        for (State state : states) {
            for (State endState: endStatesMap.get(state)){
                epsilons.add(new StatePair(state, endState));

                // get removed count
//                Map<State, Integer> statesRemoved = statesRemovedMap.get(state);
//                this.removed += statesRemoved.get(endState);
            }
        }

        // add epsilons to automaton
        returnAutomaton.addEpsilons(epsilons);

        // return the deleted automaton
        return returnAutomaton;
    }

    @Override
    public String toString() {
        return "PreciseDelete";
    }

}
