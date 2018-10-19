/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.boisestate.cs.automaton;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Finite-state automaton with regular expression operations.
 * <p>
 * Class invariants: <ul> <li> An automaton is either represented explicitly
 * (with {@link WeightedState} and {@link WeightedTransition} objects) or with a
 * singleton string (see {@link #getSingleton()} and {@link #expandSingleton()})
 * in case the automaton is known to accept exactly one string. (Implicitly, all
 * states and transitions of an automaton are reachable from its initial state.)
 * <li> Automata are always reduced (see {@link #reduce()}) and have no
 * transitions to dead states (see {@link #removeDeadTransitions()}). <li> If an
 * automaton is nondeterministic, then {@link #isDeterministic()} returns false
 * (but the converse is not required). <li> Automata provided as input to
 * operations are generally assumed to be disjoint. </ul>
 * <p>
 * If the states or transitions are manipulated manually, the {@link
 * #restoreInvariant()} and {@link #setDeterministic(boolean)} methods should be
 * used afterwards to restore representation invariants that are assumed by the
 * built-in automata operations.
 *
 * @author Andrew Harris
 * @author Anders M&oslash;ller &lt;
 * <a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class WeightedAutomaton
        implements Serializable, Cloneable {

    /**
     * Minimize using Brzozowski's O(2<sup>n</sup>) algorithm. This algorithm
     * uses the reverse-determinize-reverse-determinize trick, which has a bad
     * worst-case behavior but often works very well in practice (even better
     * than Hopcroft's!).
     *
     * @see #setMinimization(int)
     */
    public static final int MINIMIZE_BRZOZOWSKI = 1;
    /**
     * Minimize using Hopcroft's O(n log n) algorithm. This is regarded as one
     * of the most generally efficient algorithms that exist.
     *
     * @see #setMinimization(int)
     */
    public static final int MINIMIZE_HOPCROFT = 2;
    /**
     * Minimize using Huffman's O(n<sup>2</sup>) algorithm.
     * This is the standard text-book algorithm.
     *
     * @see #setMinimization(int)
     */
    public static final int MINIMIZE_HUFFMAN = 0;
    /**
     * Selects whether operations may modify the input automata (default:
     * <code>false</code>).
     */
    static boolean allow_mutation = false;
    /**
     * Caches the <code>isDebug</code> state.
     */
    static Boolean is_debug = null;
    /**
     * Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT
     * </code>).
     */
    static int minimization = MINIMIZE_HOPCROFT;
    /**
     * Minimize always flag.
     */
    static boolean minimize_always = false;
    /**
     * If true, then this automaton is definitely deterministic
     * (i.e., there are no choices for any run, but a run may crash).
     */
    boolean deterministic;
    /**
     * Hash code. Recomputed by {@link #minimize()}.
     */
    int hash_code;
    /**
     * Extra data associated with this automaton.
     */
    transient Object info;
    /**
     * Initial state of this automaton.
     */
    WeightedState initial;
    /**
     * Singleton string. Null if not applicable.
     */
    String singleton;
    /**
     * Initial multiplicative factor for model counting automaton
     */
    private int initialFactor;
    /**
     * Number of empty strings modeled by automaton
     */
    private int numEmptyStrings;

    /**
     * Returns the set of reachable accept states.
     *
     * @return set of {@link WeightedState} objects
     */
    public Set<WeightedState> getAcceptStates() {
        expandSingleton();
        HashSet<WeightedState> accepts = new HashSet<WeightedState>();
        HashSet<WeightedState> visited = new HashSet<WeightedState>();
        LinkedList<WeightedState> worklist = new LinkedList<WeightedState>();
        worklist.add(initial);
        visited.add(initial);
        while (worklist.size() > 0) {
            WeightedState s = worklist.removeFirst();
            if (s.isAccept()) {
                accepts.add(s);
            }
            for (WeightedTransition t : s.getTransitions()) {
                if (!visited.contains(t.getDest())) {
                    visited.add(t.getDest());
                    worklist.add(t.getDest());
                }
            }
        }
        return accepts;
    }

    /**
     * Returns the state of the allow mutate flag. If this flag is set, then all
     * automata operations may modify automata given as input; otherwise,
     * operations will always leave input automata languages unmodified. By
     * default, the flag is not set.
     *
     * @return current value of the flag
     */
    static boolean getAllowMutate() {
        return allow_mutation;
    }

    /**
     * See {@link SpecialWeightedOperations#getCommonPrefix(WeightedAutomaton)}.
     */
    public String getCommonPrefix() {
        return SpecialWeightedOperations.getCommonPrefix(this);
    }

    /**
     * See {@link SpecialWeightedOperations#getFiniteStrings(WeightedAutomaton)}.
     */
    public Set<String> getFiniteStrings() {
        return SpecialWeightedOperations.getFiniteStrings(this);
    }

    /**
     * Returns extra information associated with this automaton.
     *
     * @return extra information
     *
     * @see #setInfo(Object)
     */
    public Object getInfo() {
        return info;
    }

    public int getInitialFactor() {
        return initialFactor;
    }

    /**
     * Gets initial state.
     *
     * @return state
     */
    public WeightedState getInitialState() {
        expandSingleton();
        return initial;
    }

    /**
     * Returns the set of live states. A state is "live" if an accept state is
     * reachable from it.
     *
     * @return set of {@link WeightedState} objects
     */
    public Set<WeightedState> getLiveStates() {
        expandSingleton();
        return getLiveStates(getStates());
    }

    public int getNumEmptyStrings() {
        return numEmptyStrings;
    }

    /**
     * Returns the number of states in this automaton.
     */
    public int getNumberOfStates() {
        if (isSingleton()) {
            return singleton.length() + 1;
        }
        return getStates().size();
    }

    /**
     * Returns the number of transitions in this automaton. This number is
     * counted as the total number of edges, where one edge may be a character
     * interval.
     */
    public int getNumberOfTransitions() {
        if (isSingleton()) {
            return singleton.length();
        }
        int c = 0;
        for (WeightedState s : getStates()) {
            c += s.getTransitions().size();
        }
        return c;
    }

    /**
     * Returns the singleton string for this automaton. An automaton that
     * accepts exactly one string <i>may</i> be represented in singleton mode.
     * In that case, this method may be used to obtain the string.
     *
     * @return string, null if this automaton is not in singleton mode.
     */
    public String getSingleton() {
        return singleton;
    }

    /**
     * Returns sorted array of all interval start points.
     */
    public char[] getStartPoints() {
        Set<Character> pointset = new HashSet<Character>();
        for (WeightedState s : getStates()) {
            pointset.add(Character.MIN_VALUE);
            for (WeightedTransition t : s.getTransitions()) {
                pointset.add(t.getMin());
                if (t.getMax() < Character.MAX_VALUE) {
                    pointset.add((char) (t.getMax() + 1));
                }
            }
        }
        char[] points = new char[pointset.size()];
        int n = 0;
        for (Character m : pointset) {
            points[n++] = m;
        }
        Arrays.sort(points);
        return points;
    }

    /**
     * Returns the set of states that are reachable from the initial state.
     *
     * @return set of {@link WeightedState} objects
     */
    public Set<WeightedState> getStates() {
        expandSingleton();
        Set<WeightedState> visited;
        if (isDebug()) {
            visited = new LinkedHashSet<WeightedState>();
        } else {
            visited = new HashSet<WeightedState>();
        }
        LinkedList<WeightedState> worklist = new LinkedList<WeightedState>();
        worklist.add(initial);
        visited.add(initial);
        while (worklist.size() > 0) {
            WeightedState s = worklist.removeFirst();
            Collection<WeightedTransition> tr;
            if (isDebug()) {
                tr = s.getSortedTransitions(false);
            } else {
                tr = s.getTransitions();
            }
            for (WeightedTransition t : tr) {
                if (!visited.contains(t.getDest())) {
                    visited.add(t.getDest());
                    worklist.add(t.getDest());
                }
            }
        }
        return visited;
    }

    boolean isDebug() {
        if (is_debug == null) {
            is_debug = Boolean.valueOf(System.getProperty(
                    "dk.brics.automaton.debug") != null);
        }
        return is_debug.booleanValue();
    }

    /**
     * Returns deterministic flag for this automaton.
     *
     * @return true if the automaton is definitely deterministic, false if the
     * automaton may be nondeterministic
     */
    public boolean isDeterministic() {
        return deterministic;
    }

    /**
     * See {@link BasicWeightedOperations#isEmpty(WeightedAutomaton)}.
     */
    public boolean isEmpty() {
        return BasicWeightedOperations.isEmpty(this);
    }

    /**
     * See {@link BasicWeightedOperations#isEmptyString(WeightedAutomaton)}.
     */
    public boolean isEmptyString() {
        return BasicWeightedOperations.isEmptyString(this);
    }

    /**
     * See {@link SpecialWeightedOperations#isFinite(WeightedAutomaton)}.
     */
    public boolean isFinite() {
        return SpecialWeightedOperations.isFinite(this);
    }

    boolean isSingleton() {
        return singleton != null;
    }

    /**
     * See {@link BasicWeightedOperations#isTotal(WeightedAutomaton)}.
     */
    public boolean isTotal() {
        return BasicWeightedOperations.isTotal(this);
    }

    /**
     * Sets deterministic flag for this automaton.
     * This method should (only) be used if automata are constructed manually.
     *
     * @param deterministic
     *         true if the automaton is definitely deterministic, false if the
     *         automaton may be nondeterministic
     */
    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    /**
     * Associates extra information with this automaton.
     *
     * @param info
     *         extra information
     */
    public void setInfo(Object info) {
        this.info = info;
    }

    public void setInitialFactor(int initialFactor) {
        this.initialFactor = initialFactor;
    }

    /**
     * Sets initial state.
     *
     * @param s
     *         state
     */
    public void setInitialState(WeightedState s) {
        initial = s;
        singleton = null;
    }

    /**
     * Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT</code>).
     *
     * @param algorithm
     *         minimization algorithm
     */
    static public void setMinimization(int algorithm) {
        minimization = algorithm;
    }

    /**
     * Sets or resets minimize always flag. If this flag is set, then {@link
     * #minimize()} will automatically be invoked after all operations that
     * otherwise may produce non-minimal automata. By default, the flag is not
     * set.
     *
     * @param flag
     *         if true, the flag is set
     */
    static public void setMinimizeAlways(boolean flag) {
        minimize_always = flag;
    }

    public void setNumEmptyStrings(int numEmptyStrings) {
        this.numEmptyStrings = numEmptyStrings;
    }

    /**
     * Assigns consecutive numbers to the given states.
     */
    static void setStateNumbers(Set<WeightedState> states) {
        int number = 0;
        for (WeightedState s : states) {
            s.setNumber(number++);
        }
    }

    /**
     * Constructs a new automaton that accepts the empty language.
     * Using this constructor, automata can be constructed manually from
     * {@link WeightedState} and {@link WeightedTransition} objects.
     *
     * @see #setInitialState(WeightedState)
     * @see WeightedState
     * @see WeightedTransition
     */
    public WeightedAutomaton() {
        initial = new WeightedState();
        deterministic = true;
        singleton = null;
        initialFactor = 1;
        numEmptyStrings = 1;
    }

    /**
     * See {@link BasicWeightedOperations#concatenate(List)}.
     */
    static public WeightedAutomaton concatenate(List<WeightedAutomaton> l) {
        return BasicWeightedOperations.concatenate(l);
    }

    /**
     * Returns a sorted array of transitions for each state (and sets state
     * numbers).
     */
    static WeightedTransition[][] getSortedTransitions(Set<WeightedState>
															   states) {
        setStateNumbers(states);
        WeightedTransition[][] transitions =
                new WeightedTransition[states.size()][];
        for (WeightedState s : states) {
            transitions[s.getNumber()] = s.getSortedTransitionArray(false);
        }
        return transitions;
    }

    /**
     * See {@link SpecialWeightedOperations#hexCases(WeightedAutomaton)}.
     */
    public static WeightedAutomaton hexCases(WeightedAutomaton a) {
        return SpecialWeightedOperations.hexCases(a);
    }

    /**
     * Retrieves a serialized <code>Automaton</code> located by a URL.
     *
     * @param url
     *         URL of serialized automaton
     *
     * @throws IOException
     *         if input/output related exception occurs
     * @throws OptionalDataException
     *         if the data is not a serialized object
     * @throws InvalidClassException
     *         if the class serial number does not match
     * @throws ClassCastException
     *         if the data is not a serialized <code>Automaton</code>
     * @throws ClassNotFoundException
     *         if the class of the serialized object cannot be found
     */
    public static WeightedAutomaton load(URL url)
            throws IOException,
                   OptionalDataException, ClassCastException,
                   ClassNotFoundException, InvalidClassException {
        return load(url.openStream());
    }

    /**
     * Retrieves a serialized <code>Automaton</code> from a stream.
     *
     * @param stream
     *         input stream with serialized automaton
     *
     * @throws IOException
     *         if input/output related exception occurs
     * @throws OptionalDataException
     *         if the data is not a serialized object
     * @throws InvalidClassException
     *         if the class serial number does not match
     * @throws ClassCastException
     *         if the data is not a serialized <code>Automaton</code>
     * @throws ClassNotFoundException
     *         if the class of the serialized object cannot be found
     */
    public static WeightedAutomaton load(InputStream stream)
            throws
            IOException,
            OptionalDataException,
            ClassCastException,
            ClassNotFoundException,
            InvalidClassException {
        ObjectInputStream s = new ObjectInputStream(stream);
        return (WeightedAutomaton) s.readObject();
    }

    /**
     * See {@link BasicWeightedAutomata#makeAnyChar()}.
     */
    public static WeightedAutomaton makeAnyChar() {
        return BasicWeightedAutomata.makeAnyChar();
    }

    /**
     * See {@link BasicWeightedAutomata#makeAnyString()}.
     */
    public static WeightedAutomaton makeAnyString() {
        return BasicWeightedAutomata.makeAnyString();
    }

    /**
     * See {@link BasicWeightedAutomata#makeChar(char)}.
     */
    public static WeightedAutomaton makeChar(char c) {
        return BasicWeightedAutomata.makeChar(c);
    }

    /**
     * See {@link BasicWeightedAutomata#makeCharRange(char, char)}.
     */
    public static WeightedAutomaton makeCharRange(char min, char max) {
        return BasicWeightedAutomata.makeCharRange(min, max);
    }

    /**
     * See {@link BasicWeightedAutomata#makeCharSet(String)}.
     */
    public static WeightedAutomaton makeCharSet(String set) {
        return BasicWeightedAutomata.makeCharSet(set);
    }

    /**
     * See {@link BasicWeightedAutomata#makeDecimalValue(String)}.
     */
    public static WeightedAutomaton makeDecimalValue(String value) {
        return BasicWeightedAutomata.makeDecimalValue(value);
    }

    /**
     * See {@link BasicWeightedAutomata#makeEmpty()}.
     */
    public static WeightedAutomaton makeEmpty() {
        return BasicWeightedAutomata.makeEmpty();
    }

    /**
     * See {@link BasicWeightedAutomata#makeEmptyString()}.
     */
    public static WeightedAutomaton makeEmptyString() {
        return BasicWeightedAutomata.makeEmptyString();
    }

    /**
     * See {@link BasicWeightedAutomata#makeFractionDigits(int)}.
     */
    public static WeightedAutomaton makeFractionDigits(int i) {
        return BasicWeightedAutomata.makeFractionDigits(i);
    }

    /**
     * See {@link BasicWeightedAutomata#makeIntegerValue(String)}.
     */
    public static WeightedAutomaton makeIntegerValue(String value) {
        return BasicWeightedAutomata.makeIntegerValue(value);
    }

    /**
     * See {@link BasicWeightedAutomata#makeInterval(int, int, int)}.
     */
    public static WeightedAutomaton makeInterval(int min, int max, int digits)
            throws IllegalArgumentException {
        return BasicWeightedAutomata.makeInterval(min, max, digits);
    }

    /**
     * See {@link BasicWeightedAutomata#makeMaxInteger(String)}.
     */
    public static WeightedAutomaton makeMaxInteger(String n) {
        return BasicWeightedAutomata.makeMaxInteger(n);
    }

    /**
     * See {@link BasicWeightedAutomata#makeMinInteger(String)}.
     */
    public static WeightedAutomaton makeMinInteger(String n) {
        return BasicWeightedAutomata.makeMinInteger(n);
    }

    /**
     * See {@link BasicWeightedAutomata#makeString(String)}.
     */
    public static WeightedAutomaton makeString(String s) {
        return BasicWeightedAutomata.makeString(s);
    }

    /**
     * See {@link BasicWeightedAutomata#makeStringMatcher(String)}.
     */
    public static WeightedAutomaton makeStringMatcher(String s) {
        return BasicWeightedAutomata.makeStringMatcher(s);
    }

    /**
     * See {@link BasicWeightedAutomata#makeStringUnion(CharSequence...)}.
     */
    public static WeightedAutomaton makeStringUnion(CharSequence... strings) {
        return BasicWeightedAutomata.makeStringUnion(strings);
    }

    /**
     * See {@link BasicWeightedAutomata#makeTotalDigits(int)}.
     */
    public static WeightedAutomaton makeTotalDigits(int i) {
        return BasicWeightedAutomata.makeTotalDigits(i);
    }

    /**
     * See {@link WeightedMinimizationOperations#minimize(WeightedAutomaton)}.
     * Returns the automaton being given as argument.
     */
    public static WeightedAutomaton minimize(WeightedAutomaton a) {
        a.minimize();
        return a;
    }

    /**
     * See {@link SpecialWeightedOperations#replaceWhitespace(WeightedAutomaton)}.
     */
    public static WeightedAutomaton replaceWhitespace(WeightedAutomaton a) {
        return SpecialWeightedOperations.replaceWhitespace(a);
    }

    /**
     * Sets or resets allow mutate flag. If this flag is set, then all automata
     * operations may modify automata given as input; otherwise, operations will
     * always leave input automata languages unmodified. By default, the
     * flag is
     * not set.
     *
     * @param flag
     *         if true, the flag is set
     *
     * @return previous value of the flag
     */
    static public boolean setAllowMutate(boolean flag) {
        boolean b = allow_mutation;
        allow_mutation = flag;
        return b;
    }

    /**
     * See {@link ShuffleOperations#shuffleSubsetOf(Collection,
     * WeightedAutomaton, Character, Character)}.
     */
    public static String shuffleSubsetOf(Collection<WeightedAutomaton> ca,
                                         WeightedAutomaton a,
                                         Character suspend_shuffle,
                                         Character resume_shuffle) {
        return ShuffleOperations.shuffleSubsetOf(ca,
                                                 a,
                                                 suspend_shuffle,
                                                 resume_shuffle);
    }

    /**
     * See {@link BasicWeightedOperations#union(Collection)}.
     */
    static public WeightedAutomaton union(Collection<WeightedAutomaton> l) {
        return BasicWeightedOperations.union(l);
    }

    /**
     * Returns a clone of this automaton.
     */
    @Override
    public WeightedAutomaton clone() {
        try {
            WeightedAutomaton a = (WeightedAutomaton) super.clone();
            a.setInitialFactor(initialFactor);
            a.setNumEmptyStrings(numEmptyStrings);
            if (!isSingleton()) {
                HashMap<WeightedState, WeightedState> m = new HashMap<WeightedState, WeightedState>();
                Set<WeightedState> states = getStates();
                for (WeightedState s : states) {
                    m.put(s, new WeightedState());
                }
                for (WeightedState s : states) {
                    WeightedState p = m.get(s);
                    p.setAccept(s.isAccept());
                    if (s == initial) {
                        a.initial = p;
                    }
                    for (WeightedTransition t : s.getTransitions()) {
                        p.getTransitions().add(new WeightedTransition(t.getMin(), t.getMax(), m.get(t.getDest()), t.getWeight()));
                    }
                }
            }
            return a;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the language of this automaton is equal to the language
     * of the given automaton. Implemented using <code>hashCode</code> and
     * <code>subsetOf</code>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WeightedAutomaton)) {
            return false;
        }
        WeightedAutomaton a = (WeightedAutomaton) obj;
        if (isSingleton() && a.isSingleton()) {
            return singleton.equals(a.singleton);
        }
        return hashCode() == a.hashCode() && subsetOf(a) && a.subsetOf(this);
    }

    /**
     * Returns hash code for this automaton. The hash code is based on the
     * number of states and transitions in the minimized automaton.
     * Invoking this method may involve minimizing the automaton.
     */
    @Override
    public int hashCode() {
        if (hash_code == 0) {
            minimize();
        }
        return hash_code;
    }

    /**
     * Returns a string representation of this automaton.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (isSingleton()) {
            b.append("singleton: ");
            for (char c : singleton.toCharArray()) {
                WeightedTransition.appendCharString(c, b);
            }
            b.append("\n");
        } else {
            Set<WeightedState> states = getStates();
            setStateNumbers(states);
            b.append("initial state: ")
             .append(initial.getNumber())
             .append("\n");
            for (WeightedState s : states) {
                b.append(s.toString());
            }
        }
        return b.toString();
    }

    /**
     * See {@link BasicWeightedOperations#addEpsilons(WeightedAutomaton, Collection)}.
     */
    public void addEpsilons(Collection<WeightedStatePair> pairs) {
        BasicWeightedOperations.addEpsilons(this, pairs);
    }

    /**
     * See {@link BasicWeightedOperations#complement(WeightedAutomaton)}.
     */
    public WeightedAutomaton complement() {
        return BasicWeightedOperations.complement(this);
    }

    /**
     * See {@link SpecialWeightedOperations#compress(WeightedAutomaton, String, char)}.
     */
    public WeightedAutomaton compress(String set, char c) {
        return SpecialWeightedOperations.compress(this, set, c);
    }

    /**
     * See {@link BasicWeightedOperations#concatenate(WeightedAutomaton,
     * WeightedAutomaton)}.
     */
    public WeightedAutomaton concatenate(WeightedAutomaton a) {
        return BasicWeightedOperations.concatenate(this, a);
    }

    /**
     * See {@link BasicWeightedOperations#determinize(WeightedAutomaton)}.
     */
    public void determinize() {
        BasicWeightedOperations.determinize(this);
    }

    /**
     * Expands singleton representation to normal representation.
     * Does nothing if not in singleton representation.
     */
    public void expandSingleton() {
        if (isSingleton()) {
            WeightedState p = new WeightedState();
            initial = p;
            for (int i = 0; i < singleton.length(); i++) {
                WeightedState q = new WeightedState();
                p.getTransitions()
                 .add(new WeightedTransition(singleton.charAt(i), q));
                p = q;
            }
            p.setAccept(true);
            deterministic = true;
            singleton = null;
        }
    }

    /**
     * See {@link SpecialWeightedOperations#getFiniteStrings(WeightedAutomaton, int)}.
     */
    public Set<String> getFiniteStrings(int limit) {
        return SpecialWeightedOperations.getFiniteStrings(this, limit);
    }

    /**
     * See {@link BasicWeightedOperations#getShortestExample(WeightedAutomaton,
     * boolean)}.
     */
    public String getShortestExample(boolean accepted) {
        return BasicWeightedOperations.getShortestExample(this, accepted);
    }

    /**
     * See {@link SpecialWeightedOperations#getStrings(WeightedAutomaton, int)}.
     */
    public Set<String> getStrings(int length) {
        return SpecialWeightedOperations.getStrings(this, length);
    }

    /**
     * See {@link SpecialWeightedOperations#homomorph(WeightedAutomaton, char[],
     * char[])}.
     */
    public WeightedAutomaton homomorph(char[] source, char[] dest) {
        return SpecialWeightedOperations.homomorph(this, source, dest);
    }

    /**
     * See {@link BasicWeightedOperations#intersection(WeightedAutomaton,
     * WeightedAutomaton)}.
     */
    public WeightedAutomaton intersection(WeightedAutomaton a) {
        return BasicWeightedOperations.intersection(this, a);
    }

    /**
     * See {@link WeightedMinimizationOperations#minimize(WeightedAutomaton)}.
     */
    public void minimize() {
        WeightedMinimizationOperations.minimize(this);
    }

    /**
     * See {@link BasicWeightedOperations#minus(WeightedAutomaton, WeightedAutomaton)}.
     */
    public WeightedAutomaton minus(WeightedAutomaton a) {
        return BasicWeightedOperations.minus(this, a);
    }

    /**
     * See {@link BasicWeightedOperations#optional(WeightedAutomaton)}.
     */
    public WeightedAutomaton optional() {
        return BasicWeightedOperations.optional(this);
    }

    /**
     * See {@link SpecialWeightedOperations#overlap(WeightedAutomaton,
     * WeightedAutomaton)}.
     */
    public WeightedAutomaton overlap(WeightedAutomaton a) {
        return SpecialWeightedOperations.overlap(this, a);
    }

    /**
     * See {@link SpecialWeightedOperations#prefixClose(WeightedAutomaton)}.
     */
    public void prefixClose() {
        SpecialWeightedOperations.prefixClose(this);
    }

    /**
     * See {@link SpecialWeightedOperations#projectChars(WeightedAutomaton, Set)}.
     */
    public WeightedAutomaton projectChars(Set<Character> chars) {
        return SpecialWeightedOperations.projectChars(this, chars);
    }

//    /**
//     * Reduces this automaton. An automaton is "reduced" by combining
//     * overlapping and adjacent edge intervals with same destination.
//     */
//    public void reduce() {
//        if (isSingleton()) {
//            return;
//        }
//        Set<WeightedState> states = getStates();
//        setStateNumbers(states);
//        for (WeightedState s : states) {
//            List<WeightedTransition> st = s.getSortedTransitions(true);
//            SortedMap<Character, Integer> rangeWeights = new TreeMap<>();
//            s.resetTransitions();
//            WeightedState p = null;
//            int min = -1, max = -1, weight = -1;
//            for (WeightedTransition t : st) {
//                if (p == t.getDest()) {
//                    if (min == t.getMin()) {
//                        rangeWeights.put(t.getMax(), weight);
//                        weight += t.getWeight();
//                    }
//                    // range does not overlap
//                    else if (max < t.getMin()) {
//                        for (Map.Entry<Character, Integer> entry : rangeWeights.entrySet()) {
//                            s.addTransition(new WeightedTransition((char) min, entry.getKey(), p, entry.getValue()));
//                        }
//                        rangeWeights.clear();
//                        min = t.getMin();
//                        max = t.getMax();
//                        weight = t.getWeight();
//                    }
//                    // range does overlap
//                    else {
//                        LinkedList<Character> keys = new LinkedList<>(rangeWeights.keySet());
//                        char c = keys.removeFirst();
//                        while (!keys.isEmpty() && c < t.getMin()) {
//                            s.addTransition(new WeightedTransition((char) min, c, p, rangeWeights.get(c)));
//                            rangeWeights.remove(c);
//                            c = keys.removeFirst();
//                        }
//                        min = t.getMin();
//                        weight = rangeWeights.get(c);
//                        for (int i = min; i <= t.getMax(); i++) {
//                            int tempWeight = rangeWeights.get((char)i);
//                            rangeWeights.put((char)i, tempWeight + t.getWeight());
//                        }
//                    }
//                } else {
//                    if (p != null) {
//                        for (Map.Entry<Character, Integer> entry : rangeWeights.entrySet()) {
//                            s.addTransition(new WeightedTransition((char) min, entry.getKey(), p, entry.getValue()));
//                        }
//                        rangeWeights.clear();
//                    }
//                    p = t.getDest();
//                    min = t.getMin();
//                    max = t.getMax();
//                    weight = t.getWeight();
//                    rangeWeights.put(t.getMax(), weight);
//                }
//            }
//            if (p != null) {
//                for (Map.Entry<Character, Integer> entry : rangeWeights.entrySet()) {
//                    s.addTransition(new WeightedTransition((char) min, entry.getKey(), p, entry.getValue()));
//                }
//            }
//        }
//        clearHashCode();
//    }

    /**
     * Reduces this automaton. An automaton is "reduced" by combining
     * overlapping and adjacent edge intervals with same destination.
     */
    public void reduce() {
        if (isSingleton()) {
            return;
        }
        Set<WeightedState> states = getStates();
        setStateNumbers(states);
        for (WeightedState s : states) {
            List<WeightedTransition> st = s.getSortedTransitions(true);
            s.resetTransitions();
            SortedMap<Character, Integer> charMap = new TreeMap<>();
            WeightedState p = null;
            for (WeightedTransition t : st) {
                if (p != t.getDest()) {
                    if (p != null) {
                        addReducedTransitions(s, charMap, p);
                        charMap.clear();
                    }
                    p = t.getDest();
                }
                // store char weights in map
                for (int i = t.getMin(); i <= t.getMax(); i++) {
                    int charWeight = t.getWeightInt();
                    char c = (char)i;
                    if (charMap.containsKey(c)) {
                        charWeight += charMap.get(c);
                    }
                    charMap.put(c, charWeight);
                }
            }
            if (p != null) {
                addReducedTransitions(s, charMap, p);
            }
        }
        clearHashCode();
    }

    private static void addReducedTransitions(WeightedState s,
                                              SortedMap<Character, Integer> charMap,
                                              WeightedState p) {
        int min = -1;
        int max = -1;
        int weight = 0;
        for (Map.Entry<Character, Integer> entry: charMap.entrySet()) {
            if (min == -1) {
                min = entry.getKey();
                max = entry.getKey();
                weight = entry.getValue();
            } else if ((max + 1) == entry.getKey() &&
                       weight == entry.getValue()) {
                max = entry.getKey();
            } else {
                s.addTransition(new WeightedTransition((char)min, (char)max, p, weight));
                min = entry.getKey();
                max = entry.getKey();
                weight = entry.getValue();
            }
        }
        s.addTransition(new WeightedTransition((char)min, (char)max, p, weight));
    }

    /**
     * Removes transitions to dead states and calls {@link #reduce()} and {@link
     * #clearHashCode()}. (A state is "dead" if no accept state is reachable
     * from it.)
     */
    public void removeDeadTransitions() {
        clearHashCode();
        if (isSingleton()) {
            return;
        }
        Set<WeightedState> states = getStates();
        Set<WeightedState> live = getLiveStates(states);
        for (WeightedState s : states) {
            Set<WeightedTransition> st = s.getTransitions();
            s.resetTransitions();
            for (WeightedTransition t : st) {
                if (live.contains(t.getDest())) {
                    s.getTransitions().add(t);
                }
            }
        }
        reduce();
    }

    /**
     * See {@link BasicWeightedOperations#repeat(WeightedAutomaton)}.
     */
    public WeightedAutomaton repeat() {
        return BasicWeightedOperations.repeat(this);
    }

    /**
     * See {@link BasicWeightedOperations#repeat(WeightedAutomaton, int)}.
     */
    public WeightedAutomaton repeat(int min) {
        return BasicWeightedOperations.repeat(this, min);
    }

    /**
     * See {@link BasicWeightedOperations#repeat(WeightedAutomaton, int, int)}.
     */
    public WeightedAutomaton repeat(int min, int max) {
        return BasicWeightedOperations.repeat(this, min, max);
    }

    /**
     * Restores representation invariant. This method must be invoked before any
     * built-in automata operation is performed if automaton states or
     * transitions are manipulated manually.
     *
     * @see #setDeterministic(boolean)
     */
    public void restoreInvariant() {
        removeDeadTransitions();
    }

    /**
     * See {@link BasicWeightedOperations#run(WeightedAutomaton, String)}.
     */
    public boolean run(String s) {
        return BasicWeightedOperations.run(this, s);
    }

    /**
     * See {@link ShuffleOperations#shuffle(WeightedAutomaton,
     * WeightedAutomaton)}.
     */
    public WeightedAutomaton shuffle(WeightedAutomaton a) {
        return ShuffleOperations.shuffle(this, a);
    }

    /**
     * See {@link SpecialWeightedOperations#singleChars(WeightedAutomaton)}.
     */
    public WeightedAutomaton singleChars() {
        return SpecialWeightedOperations.singleChars(this);
    }

    /**
     * Writes this <code>Automaton</code> to the given stream.
     *
     * @param stream
     *         output stream for serialized automaton
     *
     * @throws IOException
     *         if input/output related exception occurs
     */
    public void store(OutputStream stream)
            throws IOException {
        ObjectOutputStream s = new ObjectOutputStream(stream);
        s.writeObject(this);
        s.flush();
    }

    /**
     * See {@link BasicWeightedOperations#subsetOf(WeightedAutomaton,
     * WeightedAutomaton)}.
     */
    public boolean subsetOf(WeightedAutomaton a) {
        return BasicWeightedOperations.subsetOf(this, a);
    }

    /**
     * See {@link SpecialWeightedOperations#subst(WeightedAutomaton, Map)}.
     */
    public WeightedAutomaton subst(Map<Character, Set<Character>> map) {
        return SpecialWeightedOperations.subst(this, map);
    }

    /**
     * See {@link SpecialWeightedOperations#subst(WeightedAutomaton, char, String)}.
     */
    public WeightedAutomaton subst(char c, String s) {
        return SpecialWeightedOperations.subst(this, c, s);
    }

    /**
     * Returns <a href="http://www.research.att.com/sw/tools/graphviz/"
     * target="_top">Graphviz Dot</a> representation of this automaton.
     */
    public String toDot() {
        StringBuilder b = new StringBuilder("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        if (numEmptyStrings > 1) {
            b.append("  info [shape=note,label=\"Empty Strings: ")
             .append(numEmptyStrings)
             .append("\"];\n");
        }
        Set<WeightedState> states = getStates();
        setStateNumbers(states);
        for (WeightedState s : states) {
            b.append("  ").append(s.getNumber());
            if (s.isAccept()) {
                b.append(" [shape=doublecircle");
            } else {
                b.append(" [shape=circle");
            }
            b.append(",label=\"").append(s.getNumber()).append("\"];\n");
            if (s == initial) {
                b.append("  initial [shape=plaintext,label=\"");
                if (initialFactor > 1){
                    b.append(initialFactor);
                }
                b.append("\"];\n");
                b.append("  initial -> ").append(s.getNumber()).append("\n");
            }
            for (WeightedTransition t : s.getTransitions()) {
                b.append("  ").append(s.getNumber());
                t.appendDot(b);
            }
        }
        return b.append("}\n").toString();
    }

    /**
     * See {@link SpecialWeightedOperations#trim(WeightedAutomaton, String, char)}.
     */
    public WeightedAutomaton trim(String set, char c) {
        return SpecialWeightedOperations.trim(this, set, c);
    }

    /**
     * See {@link BasicWeightedOperations#union(WeightedAutomaton, WeightedAutomaton)}.
     */
    public WeightedAutomaton union(WeightedAutomaton a) {
        return BasicWeightedOperations.union(this, a);
    }

    void checkMinimizeAlways() {
        if (minimize_always) {
            minimize();
        }
    }

    /**
     * Must be invoked when the stored hash code may no longer be valid.
     */
    void clearHashCode() {
        hash_code = 0;
    }

    /**
     * Returns a clone of this automaton, expands if singleton.
     */
    WeightedAutomaton cloneExpanded() {
        WeightedAutomaton a = clone();
        a.expandSingleton();
        return a;
    }

    /**
     * Returns a clone of this automaton unless <code>allow_mutation</code> is
     * set, expands if singleton.
     */
    WeightedAutomaton cloneExpandedIfRequired() {
        if (allow_mutation) {
            expandSingleton();
            return this;
        } else {
            return cloneExpanded();
        }
    }

    /**
     * Returns a clone of this automaton, or this automaton itself if
     * <code>allow_mutation</code> flag is set.
     */
    WeightedAutomaton cloneIfRequired() {
        if (allow_mutation) {
            return this;
        } else {
            return clone();
        }
    }

    /**
     * Recomputes the hash code.
     * The automaton must be minimal when this operation is performed.
     */
    void recomputeHashCode() {
        hash_code = getNumberOfStates() * 3 + getNumberOfTransitions() * 2;
        if (hash_code == 0) {
            hash_code = 1;
        }
    }

    /**
     * Adds transitions to explicit crash state to ensure that transition
     * function is total.
     */
    void totalize() {
        WeightedState s = new WeightedState();
        s.getTransitions()
         .add(new WeightedTransition(Character.MIN_VALUE,
                                     Character.MAX_VALUE,
                                     s));
        for (WeightedState p : getStates()) {
            int maxi = Character.MIN_VALUE;
            for (WeightedTransition t : p.getSortedTransitions(false)) {
                if (t.getMin() > maxi) {
                    p.getTransitions()
                     .add(new WeightedTransition((char) maxi,
                                                 (char) (t.getMin() - 1),
                                                 s));
                }
                if (t.getMax() + 1 > maxi) {
                    maxi = t.getMax() + 1;
                }
            }
            if (maxi <= Character.MAX_VALUE) {
                p.getTransitions()
                 .add(new WeightedTransition((char) maxi,
                                             Character.MAX_VALUE,
                                             s));
            }
        }
    }

    private Set<WeightedState> getLiveStates(Set<WeightedState> states) {
        HashMap<WeightedState, Set<WeightedState>> map = new HashMap
                <WeightedState, Set<WeightedState>>();
        for (WeightedState s : states) {
            map.put(s, new HashSet<WeightedState>());
        }
        for (WeightedState s : states) {
            for (WeightedTransition t : s.getTransitions()) {
                map.get(t.getDest()).add(s);
            }
        }
        Set<WeightedState> live = new HashSet<WeightedState>(getAcceptStates());
        LinkedList<WeightedState> worklist =
                new LinkedList<WeightedState>(live);
        while (worklist.size() > 0) {
            WeightedState s = worklist.removeFirst();
            for (WeightedState p : map.get(s)) {
                if (!live.contains(p)) {
                    live.add(p);
                    worklist.add(p);
                }
            }
        }
        return live;
    }
}
