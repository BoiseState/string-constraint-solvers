package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;

import java.util.HashSet;
import java.util.Set;

public class AutomatonTestUtilities {
    public static Set<String> getStrings(Automaton automaton, int minLength, int maxLength) {
        // initialize return set
        Set<String> stringSet = new HashSet<>();

        // add all strings between lengths to set
        for (int i = minLength; i <= maxLength; i++) {
            stringSet.addAll(automaton.getStrings(i));
        }

        // return string set
        return stringSet;
    }
}
