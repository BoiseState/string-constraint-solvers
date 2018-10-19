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

import java.util.*;

/**
 * Special automata operations.
 */
final public class SpecialWeightedOperations {

    private SpecialWeightedOperations() {
    }

    /**
     * Returns an automaton that accepts the compressed language of the given
     * automaton. Whenever a <code>c</code> character is allowed in the
     * original automaton, one or more <code>set</code> characters are allowed
     * in the new automaton.
     *
     * @param set
     *         set of characters to be compressed
     * @param c
     *         canonical compress character (assumed to be in <code>set</code>)
     */
    public static WeightedAutomaton compress(WeightedAutomaton a, String set,
                                             char c) {
        a = a.cloneExpandedIfRequired();
        for (WeightedState s : a.getStates()) {
            WeightedState r = s.step(c).getState();
            if (r != null) {
                // add inner
                WeightedState q = new WeightedState();
                addSetTransitions(q, set, q);
                addSetTransitions(s, set, q);
                q.addEpsilon(r);
            }
        }
        // add prefix
        a.deterministic = false;
        a.removeDeadTransitions();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Returns the longest string that is a prefix of all accepted strings and
     * visits each state at most once.
     *
     * @return common prefix
     */
    public static String getCommonPrefix(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return a.singleton;
        }
        StringBuilder b = new StringBuilder();
        HashSet<WeightedState> visited = new HashSet<WeightedState>();
        WeightedState s = a.initial;
        boolean done;
        do {
            done = true;
            visited.add(s);
            if (!s.isAccept() && s.getTransitions().size() == 1) {
                WeightedTransition t = s.getTransitions().iterator().next();
                if (t.getMin() == t.getMax() &&
                    !visited.contains(t.getDest())) {
                    b.append(t.getMin());
                    s = t.getDest();
                    done = false;
                }
            }
        } while (!done);
        return b.toString();
    }

    /**
     * Returns the set of accepted strings, assuming this automaton has a finite
     * language. If the language is not finite, null is returned.
     */
    public static Set<String> getFiniteStrings(WeightedAutomaton a) {
        HashSet<String> strings = new HashSet<String>();
        if (a.isSingleton()) {
            strings.add(a.singleton);
        } else if (!getFiniteStrings(a.initial,
                                     new HashSet<WeightedState>(),
                                     strings,
                                     new StringBuilder(),
                                     -1)) {
            return null;
        }
        return strings;
    }

    /**
     * Returns the set of accepted strings, assuming that at most
     * <code>limit</code> strings are accepted. If more than <code>limit</code>
     * strings are accepted, null is returned. If <code>limit</code>&lt;0, then
     * this methods works like {@link #getFiniteStrings(WeightedAutomaton)}.
     */
    public static Set<String> getFiniteStrings(WeightedAutomaton a, int
            limit) {
        HashSet<String> strings = new HashSet<String>();
        if (a.isSingleton()) {
            if (limit > 0) {
                strings.add(a.singleton);
            } else {
                return null;
            }
        } else if (!getFiniteStrings(a.initial,
                                     new HashSet<WeightedState>(),
                                     strings,
                                     new StringBuilder(),
                                     limit)) {
            return null;
        }
        return strings;
    }

    /**
     * Returns the strings that can be produced from the given state, or false
     * if more than <code>limit</code> strings are found.
     * <code>limit</code>&lt;0 means "infinite".
     */
    private static boolean getFiniteStrings(WeightedState s,
                                            HashSet<WeightedState> pathstates,
                                            HashSet<String> strings,
                                            StringBuilder path, int limit) {
        pathstates.add(s);
        for (WeightedTransition t : s.getTransitions()) {
            if (pathstates.contains(t.getDest())) {
                return false;
            }
            for (int n = t.getMin(); n <= t.getMax(); n++) {
                path.append((char) n);
                if (t.getDest().isAccept()) {
                    strings.add(path.toString());
                    if (limit >= 0 && strings.size() > limit) {
                        return false;
                    }
                }
                if (!getFiniteStrings(t.getDest(),
                                      pathstates,
                                      strings,
                                      path,
                                      limit)) {
                    return false;
                }
                path.deleteCharAt(path.length() - 1);
            }
        }
        pathstates.remove(s);
        return true;
    }

    /**
     * Returns the set of accepted strings of the given length.
     */
    public static Set<String> getStrings(WeightedAutomaton a, int length) {
        HashSet<String> strings = new HashSet<String>();
        if (a.isSingleton() && a.singleton.length() == length) {
            strings.add(a.singleton);
        } else if (length >= 0) {
            getStrings(a.initial, strings, new StringBuilder(), length);
        }
        return strings;
    }

    private static void getStrings(WeightedState s,
                                   Set<String> strings,
                                   StringBuilder path,
                                   int length) {
        if (length == 0) {
            if (s.isAccept()) {
                strings.add(path.toString());
            }
        } else {
            for (WeightedTransition t : s.getTransitions()) {
                for (int n = t.getMin(); n <= t.getMax(); n++) {
                    path.append((char) n);
                    getStrings(t.getDest(), strings, path, length - 1);
                    path.deleteCharAt(path.length() - 1);
                }
            }
        }
    }

    /**
     * Constructs automaton that accepts the same strings as the given automaton
     * but ignores upper/lower case of A-F.
     *
     * @param a
     *         automaton
     *
     * @return automaton
     */
    public static WeightedAutomaton hexCases(WeightedAutomaton a) {
        Map<Character, Set<Character>> map = new HashMap<Character,
                Set<Character>>();
        for (char c1 = 'a', c2 = 'A'; c1 <= 'f'; c1++, c2++) {
            Set<Character> ws = new HashSet<Character>();
            ws.add(c1);
            ws.add(c2);
            map.put(c1, ws);
            map.put(c2, ws);
        }
        WeightedAutomaton ws = Datatypes.getWhitespaceAutomaton();
        return ws.concatenate(a.subst(map)).concatenate(ws);
    }

    /**
     * Returns an automaton accepting the homomorphic image of the given
     * automaton using the given function.
     * <p>
     * This method maps each transition label to a new value.
     * <code>source</code> and <code>dest</code> are assumed to be arrays of
     * same length, and <code>source</code> must be sorted in increasing order
     * and contain no duplicates. <code>source</code> defines the starting
     * points of char intervals, and the corresponding entries in
     * <code>dest</code> define the starting points of corresponding new
     * intervals.
     */
    public static WeightedAutomaton homomorph(WeightedAutomaton a, char[]
            source, char[] dest) {
        a = a.cloneExpandedIfRequired();
        for (WeightedState s : a.getStates()) {
            Set<WeightedTransition> st = s.getTransitions();
            s.resetTransitions();
            for (WeightedTransition t : st) {
                int min = t.getMin();
                while (min <= t.getMax()) {
                    int n = findIndex((char) min, source);
                    char nmin = (char) (dest[n] + min - source[n]);
                    int end = (n + 1 == source.length) ?
                              Character.MAX_VALUE :
                              source[n + 1] - 1;
                    int length;
                    if (end < t.getMax()) {
                        length = end + 1 - min;
                    } else {
                        length = t.getMax() + 1 - min;
                    }
                    s.getTransitions()
                     .add(new WeightedTransition(nmin,
                                                 (char) (nmin + length - 1),
                                                 t.getDest()));
                    min += length;
                }
            }
        }
        a.deterministic = false;
        a.removeDeadTransitions();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Returns true if the language of this automaton is finite.
     */
    public static boolean isFinite(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return true;
        }
        return isFinite(a.initial, new HashSet<WeightedState>(), new
                HashSet<WeightedState>());
    }

    /**
     * Checks whether there is a loop containing s. (This is sufficient since
     * there are never transitions to dead states.)
     */
    private static boolean isFinite(WeightedState s, HashSet<WeightedState>
            path, HashSet<WeightedState> visited) {
        path.add(s);
        for (WeightedTransition t : s.getTransitions()) {
            if (path.contains(t.getDest()) ||
                (!visited.contains(t.getDest()) &&
                 !isFinite(t.getDest(), path, visited))) {
                return false;
            }
        }
        path.remove(s);
        visited.add(s);
        return true;
    }

    /**
     * Returns an automaton that accepts the overlap of strings that in more
     * than one way can be split into a left part being accepted by
     * <code>a1</code> and a right part being accepted by <code>a2</code>.
     */
    public static WeightedAutomaton overlap(WeightedAutomaton a1,
                                            WeightedAutomaton a2) {
        WeightedAutomaton b1 = a1.cloneExpanded();
        b1.determinize();
        acceptToAccept(b1);
        WeightedAutomaton b2 = a2.cloneExpanded();
        reverse(b2);
        b2.determinize();
        acceptToAccept(b2);
        reverse(b2);
        b2.determinize();
        return b1.intersection(b2).minus(BasicWeightedAutomata.makeEmptyString());
    }

    /**
     * Reverses the language of the given (non-singleton) automaton while
     * returning the set of new initial states.
     */
    public static Set<WeightedState> reverse(WeightedAutomaton a) {
        // reverse all edges
        HashMap<WeightedState, HashSet<WeightedTransition>> m = new HashMap<>();
        Set<WeightedState> states = a.getStates();
        Set<WeightedState> accept = a.getAcceptStates();
        for (WeightedState r : states) {
            m.put(r, new HashSet<WeightedTransition>());
            r.setAccept(false);
        }
        for (WeightedState r : states) {
            for (WeightedTransition t : r.getTransitions()) {
                m.get(t.getDest()).add(new WeightedTransition(t.getMin(), t.getMax(), r, t.getWeightInt()));
            }
        }
        for (WeightedState r : states) {
            r.setTransitions(m.get(r));
        }
        // make new initial+final states
        a.initial.setAccept(true);
        a.initial = new WeightedState();
        for (WeightedState r : accept) {
            a.initial.addEpsilon(r); // ensures that all initial states are
        }
		// reachable
        a.deterministic = false;
        return accept;
    }

    private static void acceptToAccept(WeightedAutomaton a) {
        WeightedState s = new WeightedState();
        for (WeightedState r : a.getAcceptStates()) {
            s.addEpsilon(r);
        }
        a.initial = s;
        a.deterministic = false;
    }

    /**
     * Prefix closes the given automaton.
     */
    public static void prefixClose(WeightedAutomaton a) {
        for (WeightedState s : a.getStates()) {
            s.setAccept(true);
        }
        a.clearHashCode();
        a.checkMinimizeAlways();
    }

    /**
     * Returns an automaton with projected alphabet. The new automaton accepts
     * all strings that are projections of strings accepted by the given
     * automaton onto the given characters (represented by
     * <code>Character</code>). If <code>null</code> is in the set, it
     * abbreviates the intervals u0000-uDFFF and uF900-uFFFF (i.e., the
     * non-private code points). It is assumed that all other characters from
     * <code>chars</code> are in the interval uE000-uF8FF.
     */
    public static WeightedAutomaton projectChars(WeightedAutomaton a,
                                                 Set<Character> chars) {
        Character[] c = chars.toArray(new Character[chars.size()]);
        char[] cc = new char[c.length];
        boolean normalchars = false;
        for (int i = 0; i < c.length; i++) {
            if (c[i] == null) {
                normalchars = true;
            } else {
                cc[i] = c[i];
            }
        }
        Arrays.sort(cc);
        if (a.isSingleton()) {
            for (int i = 0; i < a.singleton.length(); i++) {
                char sc = a.singleton.charAt(i);
                if (!(normalchars && (sc <= '\udfff' || sc >= '\uf900') ||
                      Arrays.binarySearch(cc, sc) >= 0)) {
                    return BasicWeightedAutomata.makeEmpty();
                }
            }
            return a.cloneIfRequired();
        } else {
            HashSet<WeightedStatePair> epsilons = new HashSet<WeightedStatePair>();
            a = a.cloneExpandedIfRequired();
            for (WeightedState s : a.getStates()) {
                HashSet<WeightedTransition> new_transitions =
                        new HashSet<WeightedTransition>();
                for (WeightedTransition t : s.getTransitions()) {
                    boolean addepsilon = false;
                    if (t.getMin() < '\uf900' && t.getMax() > '\udfff') {
                        int w1 = Arrays.binarySearch(cc,
                                                     t.getMin() > '\ue000' ?
                                                     t.getMin() :
                                                     '\ue000');
                        if (w1 < 0) {
                            w1 = -w1 - 1;
                            addepsilon = true;
                        }
                        int w2 = Arrays.binarySearch(cc,
                                                     t.getMax() < '\uf8ff' ?
                                                     t.getMax() :
                                                     '\uf8ff');
                        if (w2 < 0) {
                            w2 = -w2 - 2;
                            addepsilon = true;
                        }
                        for (int w = w1; w <= w2; w++) {
                            new_transitions.add(new WeightedTransition(cc[w],
                                                                       t
																			   .getDest()));
                            if (w > w1 && cc[w - 1] + 1 != cc[w]) {
                                addepsilon = true;
                            }
                        }
                    }
                    if (normalchars) {
                        if (t.getMin() <= '\udfff') {
                            new_transitions.add(new WeightedTransition(t
                                                                               .getMin(),
                                                                       t
                                                                               .getMax() <
                                                                       '\udfff' ?
                                                                       t
                                                                               .getMax() :
                                                                       '\udfff',
                                                                       t
                                                                               .getDest()));
                        }
                        if (t.getMax() >= '\uf900') {
                            new_transitions.add(new WeightedTransition(t
                                                                               .getMin() >
                                                                       '\uf900' ?
                                                                       t
                                                                               .getMin() :
                                                                       '\uf900',
                                                                       t
                                                                               .getMax(),
                                                                       t
                                                                               .getDest()));
                        }
                    } else if (t.getMin() <= '\udfff' ||
                               t.getMax() >= '\uf900') {
                        addepsilon = true;
                    }
                    if (addepsilon) {
                        epsilons.add(new WeightedStatePair(s, t.getDest()));
                    }
                }
                s.setTransitions(new_transitions);
            }
            a.reduce();
            a.addEpsilons(epsilons);
            a.removeDeadTransitions();
            a.checkMinimizeAlways();
            return a;
        }
    }

    /**
     * Constructs automaton that accepts 0x20, 0x9, 0xa, and 0xd in place of
     * each 0x20 transition in the given automaton.
     *
     * @param a
     *         automaton
     *
     * @return automaton
     */
    public static WeightedAutomaton replaceWhitespace(WeightedAutomaton a) {
        Map<Character, Set<Character>> map = new HashMap<Character,
                Set<Character>>();
        Set<Character> ws = new HashSet<Character>();
        ws.add(' ');
        ws.add('\t');
        ws.add('\n');
        ws.add('\r');
        map.put(' ', ws);
        return a.subst(map);
    }

    /**
     * Returns an automaton that accepts the single chars that occur
     * in strings that are accepted by the given automaton.
     * Never modifies the input automaton.
     */
    public static WeightedAutomaton singleChars(WeightedAutomaton a) {
        WeightedAutomaton b = new WeightedAutomaton();
        WeightedState s = new WeightedState();
        b.initial = s;
        WeightedState q = new WeightedState();
        q.setAccept(true);
        if (a.isSingleton()) {
            for (int i = 0; i < a.singleton.length(); i++) {
                s.getTransitions()
                 .add(new WeightedTransition(a.singleton.charAt(i), q));
            }
        } else {
            for (WeightedState p : a.getStates()) {
                for (WeightedTransition t : p.getTransitions()) {
                    s.getTransitions()
                     .add(new WeightedTransition(t.getMin(), t.getMax(), q));
                }
            }
        }
        b.deterministic = true;
        b.removeDeadTransitions();
        return b;
    }

    /**
     * Returns an automaton where all transition labels have been substituted.
     * <p>
     * Each transition labeled <code>c</code> is changed to a set of
     * transitions, one for each character in <code>map(c)</code>. If
     * <code>map(c)</code> is null, then the transition is unchanged.
     *
     * @param map
     *         map from characters to sets of characters (where characters are
     *         <code>Character</code> objects)
     */
    public static WeightedAutomaton subst(WeightedAutomaton a, Map<Character,
            Set<Character>> map) {
        if (map.isEmpty()) {
            return a.cloneIfRequired();
        }
        Set<Character> ckeys = new TreeSet<Character>(map.keySet());
        char[] keys = new char[ckeys.size()];
        int j = 0;
        for (Character c : ckeys) {
            keys[j++] = c;
        }
        a = a.cloneExpandedIfRequired();
        for (WeightedState s : a.getStates()) {
            Set<WeightedTransition> st = s.getTransitions();
            s.resetTransitions();
            for (WeightedTransition t : st) {
                int index = findIndex(t.getMin(), keys);
                while (t.getMin() <= t.getMax()) {
                    if (keys[index] > t.getMin()) {
                        char m = (char) (keys[index] - 1);
                        if (t.getMax() < m) {
                            m = t.getMax();
                        }
                        s.getTransitions()
                         .add(new WeightedTransition(t.getMin(),
                                                     m,
                                                     t.getDest()));
                        if (m + 1 > Character.MAX_VALUE) {
                            break;
                        }
                        t.setMin((char) (m + 1));
                    } else if (keys[index] < t.getMin()) {
                        char m;
                        if (index + 1 < keys.length) {
                            m = (char) (keys[++index] - 1);
                        } else {
                            m = Character.MAX_VALUE;
                        }
                        if (t.getMax() < m) {
                            m = t.getMax();
                        }
                        s.getTransitions()
                         .add(new WeightedTransition(t.getMin(),
                                                     m,
                                                     t.getDest()));
                        if (m + 1 > Character.MAX_VALUE) {
                            break;
                        }
                        t.setMin((char) (m + 1));
                    } else { // found t.getMin() in substitution map
                        for (Character c : map.get(t.getMin())) {
                            s.getTransitions()
                             .add(new WeightedTransition(c, t.getDest()));
                        }
                        if (t.getMin() + 1 > Character.MAX_VALUE) {
                            break;
                        }
                        t.incMin();
                        if (index + 1 < keys.length &&
                            keys[index + 1] == t.getMin()) {
                            index++;
                        }
                    }
                }
            }
        }
        a.deterministic = false;
        a.removeDeadTransitions();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Finds the largest entry whose value is less than or equal to c,
     * or 0 if there is no such entry.
     */
    static int findIndex(char c, char[] points) {
        int a = 0;
        int b = points.length;
        while (b - a > 1) {
            int d = (a + b) >>> 1;
            if (points[d] > c) {
                b = d;
            } else if (points[d] < c) {
                a = d;
            } else {
                return d;
            }
        }
        return a;
    }

    /**
     * Returns an automaton where all transitions of the given char are replaced
     * by a string.
     *
     * @param c
     *         char
     * @param s
     *         string
     *
     * @return new automaton
     */
    public static WeightedAutomaton subst(WeightedAutomaton a, char c, String
            s) {
        a = a.cloneExpandedIfRequired();
        Set<WeightedStatePair> epsilons = new HashSet<WeightedStatePair>();
        for (WeightedState p : a.getStates()) {
            Set<WeightedTransition> st = p.getTransitions();
            p.resetTransitions();
            for (WeightedTransition t : st) {
                if (t.getMax() < c || t.getMin() > c) {
                    p.getTransitions().add(t);
                } else {
                    if (t.getMin() < c) {
                        p.getTransitions()
                         .add(new WeightedTransition(t.getMin(),
                                                     (char) (c - 1),
                                                     t.getDest()));
                    }
                    if (t.getMax() > c) {
                        p.getTransitions()
                         .add(new WeightedTransition((char) (c + 1),
                                                     t.getMax(),
                                                     t.getDest()));
                    }
                    if (s.length() == 0) {
                        epsilons.add(new WeightedStatePair(p, t.getDest()));
                    } else {
                        WeightedState q = p;
                        for (int i = 0; i < s.length(); i++) {
                            WeightedState r;
                            if (i + 1 == s.length()) {
                                r = t.getDest();
                            } else {
                                r = new WeightedState();
                            }
                            q.getTransitions()
                             .add(new WeightedTransition(s.charAt(i), r));
                            q = r;
                        }
                    }
                }
            }
        }
        a.addEpsilons(epsilons);
        a.deterministic = false;
        a.removeDeadTransitions();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Returns an automaton that accepts the trimmed language of the given
     * automaton. The resulting automaton is constructed as follows: 1)
     * Whenever
     * a <code>c</code> character is allowed in the original automaton, one or
     * more <code>set</code> characters are allowed in the new automaton. 2)
     * The automaton is prefixed and postfixed with any number of
     * <code>set</code> characters.
     *
     * @param set
     *         set of characters to be trimmed
     * @param c
     *         canonical trim character (assumed to be in <code>set</code>)
     */
    public static WeightedAutomaton trim(WeightedAutomaton a,
                                         String set,
                                         char c) {
        a = a.cloneExpandedIfRequired();
        WeightedState f = new WeightedState();
        addSetTransitions(f, set, f);
        f.setAccept(true);
        for (WeightedState s : a.getStates()) {
            WeightedState r = s.step(c).getState();
            if (r != null) {
                // add inner
                WeightedState q = new WeightedState();
                addSetTransitions(q, set, q);
                addSetTransitions(s, set, q);
                q.addEpsilon(r);
            }
            // add postfix
            if (s.isAccept()) {
                s.addEpsilon(f);
            }
        }
        // add prefix
        WeightedState p = new WeightedState();
        addSetTransitions(p, set, p);
        p.addEpsilon(a.initial);
        a.initial = p;
        a.deterministic = false;
        a.removeDeadTransitions();
        a.checkMinimizeAlways();
        return a;
    }

    private static void addSetTransitions(WeightedState s,
                                          String set,
                                          WeightedState p) {
        for (int n = 0; n < set.length(); n++) {
            s.getTransitions().add(new WeightedTransition(set.charAt(n), p));
        }
    }
}
