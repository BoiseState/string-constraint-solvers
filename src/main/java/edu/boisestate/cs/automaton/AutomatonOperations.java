package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import java.util.*;

public class AutomatonOperations {

    public static Automaton boundAutomaton(Automaton automaton, int length) {

        // create bounding automaton
        Automaton boundingAutomaton = BasicAutomata.makeAnyChar()
                                                   .repeat(0, length);

        // bound automaton and return
        return automaton.intersection(boundingAutomaton);
    }

    public static List<Automaton> boundAndSliceAutomaton(Automaton automaton, int length) {

        // initialize automaton map
        List<Automaton> automatonMap = new ArrayList<>(length + 1);

        // for each value up to and including length
        for (int i = 0; i <= length; i++) {

            // bound automaton
            Automaton a = boundAutomaton(automaton, i);

            // add to map
            automatonMap.add(a);
        }

        // return automaton map
        return automatonMap;
    }

    static public Automaton ignoreCase(Automaton automaton) {

        // clone automaton
        Automaton clone = automaton.clone();

        // for all states
        for (State state : clone.getStates()) {

            // all transitions from state
            Set<Transition> transitions = state.getTransitions();

            // for all transitions in current set of transitions
            for (Transition t : new ArrayList<>(transitions)) {

                // get transition values
                char min = t.getMin();
                char max = t.getMax();
                State dest = t.getDest();

                // if transition represents subset of characters
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {

                    // for each character represented in transition
                    for (int i = min; i <= max; i++) {

                        // get i as char
                        char c = (char) i;

                        // if char is uppercase
                        if (Character.isUpperCase(c)) {

                            // add corresponding lowercase transition
                            char lc = Character.toLowerCase(c);
                            Transition lcTrans = new Transition(lc, dest);
                            transitions.add(lcTrans);
                        }

                        // if char is lowercase
                        if (Character.isLowerCase(c)) {

                            // add corresponding uppercase transition
                            char uc = Character.toUpperCase(c);
                            Transition ucTrans = new Transition(uc, dest);
                            transitions.add(ucTrans);
                        }
                    }
                }
            }
        }

        clone.setDeterministic(false);
        clone.reduce();
        clone.minimize();

        return clone;
    }
}
