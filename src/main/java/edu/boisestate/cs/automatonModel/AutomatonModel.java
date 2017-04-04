package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.MinMaxPair;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AutomatonModel
        implements Cloneable {

    protected final Alphabet alphabet;
    protected int boundLength;
    protected AutomatonModelManager modelManager;

    public abstract String getAcceptedStringExample();

    public int getBoundLength() {
        return boundLength;
    }

    public abstract Set<String> getFiniteStrings();

    public abstract boolean isEmpty();

    public abstract boolean isSingleton();

    public void setBoundLength(int boundLength) {
        this.boundLength = boundLength;
    }

    protected AutomatonModel(Alphabet alphabet,
                             int initialBoundLength) {

        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    static Automaton performUnaryOperation(Automaton automaton,
                                           UnaryOperation operation,
                                           Alphabet alphabet) {
        // use operation
        Automaton result = operation.op(automaton);

        // bound resulting automaton to alphabet
        String charSet = alphabet.getCharSet();
        Automaton anyChar = BasicAutomata.makeCharSet(charSet).repeat();
        result = result.intersection(anyChar);

        //eas: even so the operation return the minimized automaton
        //the intersection might mess it up.
        //no need to call determinize since a minimize does
        //call that method first - only deterministic FA
        //can be minimized.
        result.minimize();

        // return resulting automaton
        return result;
    }

    public abstract AutomatonModel assertContainedInOther(AutomatonModel containingModel);

    public abstract AutomatonModel assertContainsOther(AutomatonModel containedModel);

    public abstract AutomatonModel assertEmpty();

    public abstract AutomatonModel assertEndsOther(AutomatonModel baseModel);

    public abstract AutomatonModel assertEndsWith(AutomatonModel endingModel);

    public abstract AutomatonModel assertEquals(AutomatonModel equalModel);

    public abstract AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel);

    public abstract AutomatonModel assertHasLength(int min, int max);

    public abstract AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel);

    public abstract AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel);

    public abstract AutomatonModel assertNotEmpty();

    public abstract AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel);

    public abstract AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel);

    public abstract AutomatonModel assertNotEquals(AutomatonModel notEqualModel);

    public abstract AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel);

    public abstract AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel);

    public abstract AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel);

    public abstract AutomatonModel assertStartsOther(AutomatonModel startingModel);

    public abstract AutomatonModel assertStartsWith(AutomatonModel startingModel);

    public abstract AutomatonModel concatenate(AutomatonModel arg);

    public abstract boolean containsString(String actualValue);

    public abstract AutomatonModel delete(int start, int end);

    public abstract boolean equals(AutomatonModel arg);

    public abstract AutomatonModel intersect(AutomatonModel arg);

    public abstract AutomatonModel insert(int offset, AutomatonModel argModel);

    public abstract BigInteger modelCount();

    public abstract AutomatonModel replace(char find, char replace);

    public abstract AutomatonModel replace(String find, String replace);

    public abstract AutomatonModel replaceChar();

    public abstract AutomatonModel replaceFindKnown(char find);

    public abstract AutomatonModel replaceReplaceKnown(char replace);

    public abstract AutomatonModel reverse();

    public abstract AutomatonModel substring(int start, int end);

    public abstract AutomatonModel setCharAt(int offset, AutomatonModel argModel);

    public abstract AutomatonModel setLength(int length);

    public abstract AutomatonModel suffix(int start);

    public abstract AutomatonModel toLowercase();

    public abstract AutomatonModel toUppercase();

    public abstract AutomatonModel trim();

    public abstract AutomatonModel clone();

    static Automaton getRequiredCharAutomaton(Automaton a, Alphabet alphabet, int boundLength) {
        // if initial state is accepting
        State initialState = a.getInitialState();
        if (initialState.isAccept() && initialState.getTransitions().isEmpty()) {
            return BasicAutomata.makeEmptyString();
        }

        // initialize required char map
        Map<Integer, Character> requiredCharMap = new HashMap<>();

        // initialize state set
        Set<State> states = new TreeSet<>();
        states.add(initialState);

        // walk automaton up to bound length
        int accept = -1;
        for (int i = 0; i < boundLength && accept < 0; i++) {
            // initialize flag as true
            boolean isSame = true;

            // initialize current char to unused value
            char c = Character.MAX_VALUE;
            Set<State> newStates = new TreeSet<>();
            for (State s : states) {
                // if no transitions
                if (s.getTransitions().size() != 1) {
                    isSame = false;
                    continue;
                }
                // check if transition destination is an accepting state
                for (Transition t : s.getTransitions()) {
                    newStates.add(t.getDest());
                    if (t.getDest().isAccept()) {
                        accept = i;
                    }
                    // if transitions allow more than one character at length i
                    if (t.getMin() != t.getMax() ||
                        (c != Character.MAX_VALUE && c != t.getMin())) {
                        isSame = false;
                        continue;
                    }

                    // set current char to single char from transition
                    c = t.getMin();
                }
            }

            // if single char for transition at lenght i
            if (isSame && c != Character.MAX_VALUE) {
                requiredCharMap.put(i, c);
            }

            // update state set
            states = newStates;
        }

        // if no required single characters
        if (requiredCharMap.isEmpty()) {
            return BasicAutomata.makeEmpty();
        }

        // initialize initial state and current state variable
        State initial = new State();
        State s = initial;

        // create required char automaton
        int length = boundLength;
        if (accept >= 0) {
            length = accept;
        }
        for (int i = 0; i < length; i ++) {
            // create new destination state
            State dest = new State();

            // if single character at length i
            if (requiredCharMap.containsKey(i)) {
                // add single char transition
                s.addTransition(new Transition(requiredCharMap.get(i), dest));
            } else {
                // add transition for all chars in alphabet
                for (MinMaxPair pair : alphabet.getCharRanges()) {
                    s.addTransition(new Transition(pair.getMin(), pair.getMax(), dest));
                }
            }

            // update current state
            s = dest;
        }

        // initialize return automaton and set initial and accepting states
        Automaton returnAutomaton = new Automaton();
        returnAutomaton.setInitialState(initial);
        s.setAccept(true);

        // return automaton
        return returnAutomaton;
    }
}
