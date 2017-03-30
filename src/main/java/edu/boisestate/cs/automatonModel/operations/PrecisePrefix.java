/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.AssertHasLength;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class PrecisePrefix
        extends UnaryOperation {
    int end;

    public PrecisePrefix(int end) {
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
        // if start is greater than end or automaton is empty
        if (this.end < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }

        // clone automaton
        Automaton clone = a.clone();

        // initialize working list of states
        LinkedList<State> states = new LinkedList<State>();

        // start algorithm with initial state in working list
        states.add(clone.getInitialState());

        for (int i = 0; i < end; i++) {
            LinkedList<Transition> transitions = new LinkedList<Transition>();

            while (states.size() > 0) {
                transitions.addAll(states.removeFirst().getTransitions());
            }
            while (transitions.size() > 0) {
                states.add(transitions.removeFirst().getDest());
            }
        }
        Set<StatePair> epsilons = new HashSet<StatePair>();
        State finalState = new State();
        while (states.size() > 0) {
            epsilons.add(new StatePair(states.removeFirst(), finalState));

        }
        finalState.setAccept(true);
        for (State s : clone.getAcceptStates()) {
            if (s != finalState) {
                s.setAccept(false);
            }
        }
        clone.addEpsilons(epsilons);
        clone.determinize();
        clone.minimize();
        Automaton any = Automaton.makeAnyString();
        AssertHasLength l = new AssertHasLength(end, end);
        clone = clone.intersection(l.op(any));
        return clone;
    }

    @Override
    public String toString() {
        return "PreciseSuffix";
    }

}
