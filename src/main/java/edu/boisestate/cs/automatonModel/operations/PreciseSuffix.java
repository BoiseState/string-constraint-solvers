/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class PreciseSuffix
        extends UnaryOperation {

    int start;

    public PreciseSuffix(int start) {
        this.start = start;
    }

    @Override
    public CharSet charsetTransfer(CharSet arg0) {
        return arg0;
    }

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 4;
    }

    @Override
    public Automaton op(Automaton a) {
        // if start is greater than end or automaton is empty
        if (this.start < 0 || a.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }
        Automaton b = a.clone();
        LinkedList<State> states = new LinkedList<State>();
        states.add(b.getInitialState());
        for (int i = 0; i < start; i++) {
            LinkedList<Transition> transitions = new LinkedList<Transition>();

            while (states.size() > 0) {
                transitions.addAll(states.removeFirst().getTransitions());
            }
            while (transitions.size() > 0) {
                states.add(transitions.removeFirst().getDest());
            }
        }
        Set<StatePair> epsilons = new HashSet<StatePair>();
        State initial = new State();
        while (states.size() > 0) {
            epsilons.add(new StatePair(initial, states.removeFirst()));

        }
        b.setInitialState(initial);
        b.addEpsilons(epsilons);
        b.minimize();
        return b;
    }

    @Override
    public String toString() {
        return "PrecisePrefix";
    }

}
