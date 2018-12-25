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

public abstract class AutomatonModel<T extends AutomatonModel>
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

    public abstract T assertContainedInOther(T containingModel);

    public abstract T assertContainsOther(T containedModel);

    public abstract T assertEmpty();

    public abstract T assertEndsOther(T baseModel);

    public abstract T assertEndsWith(T endingModel);

    public abstract T assertEquals(T equalModel);

    public abstract T assertEqualsIgnoreCase(T equalModel);

    public abstract T assertHasLength(int min, int max);

    public abstract T assertNotContainedInOther(T notContainingModel);

    public abstract T assertNotContainsOther(T notContainedModel);

    public abstract T assertNotEmpty();

    public abstract T assertNotEndsOther(T notEndingModel);

    public abstract T assertNotEndsWith(T notEndingModel);

    public abstract T assertNotEquals(T notEqualModel);

    public abstract T assertNotEqualsIgnoreCase(T notEqualModel);

    public abstract T assertNotStartsOther(T notStartingModel);

    public abstract T assertNotStartsWith(T notStartsModel);

    public abstract T assertStartsOther(T startingModel);

    public abstract T assertStartsWith(T startingModel);

    public abstract T concatenate(T arg);

    public abstract boolean containsString(String actualValue);

    public abstract T delete(int start, int end);

    public abstract boolean equals(T arg);

    public abstract T intersect(T arg);

    public abstract T insert(int offset, T argModel);

    public abstract BigInteger modelCount();

    public abstract T replace(char find, char replace);

    public abstract T replace(String find, String replace);

    public abstract T replaceChar();

    public abstract T replaceFindKnown(char find);

    public abstract T replaceReplaceKnown(char replace);

    public abstract T reverse();

    public abstract T substring(int start, int end);

    public abstract T setCharAt(int offset, T argModel);

    public abstract T setLength(int length);

    public abstract T suffix(int start);

    public abstract T toLowercase();

    public abstract T toUppercase();

    public abstract T trim();

    public abstract T clone();

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
            length = accept + 1;
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
    
    public abstract String getAutomaton();
}
