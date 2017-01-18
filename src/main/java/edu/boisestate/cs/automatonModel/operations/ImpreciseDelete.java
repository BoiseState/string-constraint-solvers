/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.*;

@SuppressWarnings("Duplicates")
public class ImpreciseDelete
        extends UnaryOperation {
    private int end;
    private int start;

    public ImpreciseDelete(int start, int end) {
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
        // create first automaton clone without accepting states
        Automaton clone1 = a.clone();
        for (State acceptStates : clone1.getAcceptStates()) {
            acceptStates.setAccept(false);
        }

        // create second automaton clone
        Automaton clone2 = a.clone();

        // if start and end indices are equal, return clone2
        if (this.start == this.end) {
            return clone2;
        }

        // if start is greater than end, return empty automaton (exception)
        if (this.start > this.end) {
            BasicAutomata.makeEmpty();
        }

        Map<State, State> stateMap = new HashMap<>();
        stateMap.put(clone1.getInitialState(), clone2.getInitialState());

        // initialize state set
        Set<State> states = new HashSet<>();
        states.add(clone1.getInitialState());

        // walk automaton up to end index
        for (int i = 0; i < this.start; i++) {

            // initialize transition set
            Map<State, Set<Transition>> transitionMap = new HashMap<>();

            // get all transitions for states
            for (State s : states) {
                Set<Transition> transitionSet;
                if (transitionMap.containsKey(s)) {
                    transitionSet = transitionMap.get(s);
                } else {
                    transitionSet = new HashSet<>();
                    transitionMap.put(s, transitionSet);
                }
                transitionSet.addAll(s.getTransitions());
            }

            // clear state set
            states.clear();

            // get next states from transitions
            for (State s : transitionMap.keySet()) {
                for (Transition t : transitionMap.get(s)) {

                    // if destination state not in state map
                    if (!stateMap.containsKey(t.getDest())) {

                        // get transitions from other state in map
                        Set<Transition> otherTransitions =
                                stateMap.get(s).getTransitions();

                        // get other destination state
                        State otherDest = null;
                        for (Transition otherT : otherTransitions) {
                            if (otherT.getMin() == t.getMin() &&
                                otherT.getMax() == t.getMax()) {
                                otherDest = otherT.getDest();
                            }
                        }

                        stateMap.put(t.getDest(), otherDest);
                    }

                    // add destination state to state set
                    states.add(t.getDest());
                }
            }
        }

        // initialize start end map from states set
        Map<State, Set<State>> startEndMap = new HashMap<>();
        for (State s : states) {
            Set<State> endStates = new HashSet<>();
            endStates.add(s);
            startEndMap.put(s, endStates);
        }

        // walk automaton between start and end indices
        for (int i = this.start; i < this.end; i++) {
            for (State startState : startEndMap.keySet()) {

                // get end set as states variable
                states = startEndMap.get(startState);

                // initialize transition set
                Map<State, Set<Transition>> transitionMap = new HashMap<>();

                // get all transitions for states
                for (State s : states) {
                    Set<Transition> transitionSet;
                    if (transitionMap.containsKey(s)) {
                        transitionSet = transitionMap.get(s);
                    } else {
                        transitionSet = new HashSet<>();
                        transitionMap.put(s, transitionSet);
                    }
                    transitionSet.addAll(s.getTransitions());
                }

                // clear state set
                states.clear();

                // get next states from transitions
                for (State s : transitionMap.keySet()) {
                    for (Transition t : transitionMap.get(s)) {

                        // if destination state not in state map
                        if (!stateMap.containsKey(t.getDest())) {

                            // get transitions from other state in map
                            Set<Transition> otherTransitions =
                                    stateMap.get(s).getTransitions();

                            // get other destination state
                            State otherDest = null;
                            for (Transition otherT : otherTransitions) {
                                if (otherT.getMin() == t.getMin() &&
                                    otherT.getMax() == t.getMax()) {
                                    otherDest = otherT.getDest();
                                }
                            }

                            stateMap.put(t.getDest(), otherDest);
                        }

                        // add destination state to state set
                        states.add(t.getDest());
                    }
                }
            }
        }

        // initialize epsilon transition set
        Set<StatePair> epsilons = new HashSet<StatePair>();

        // create epsilon transitions for start and end
        for (State startState : startEndMap.keySet()) {
            for (State endState : startEndMap.get(startState)) {
                // create episilon transition state pair and add to set
                StatePair epsilon =
                        new StatePair(startState, stateMap.get(endState));
                epsilons.add(epsilon);
            }
        }

        // add epsilon transitions to deleted automaton
        clone1.addEpsilons(epsilons);
        System.out.println("O \n"+clone1);
        // determinize and minimize deleted automaton
        clone1.determinize();
        System.out.println("D \n" + clone1);
        clone1.minimize();
        System.out.println("M \n" + clone1);

        // return the deleted automaton
        return clone1;
    }

    @Override
    public String toString() {
        return "PreciseDelete";
    }

}
