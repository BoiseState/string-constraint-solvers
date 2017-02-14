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
 * Basic automata operations.
 */
final public class BasicOperations {

    private BasicOperations() {
    }

    /**
     * Adds epsilon transitions to the given automaton. This method adds extra
     * character interval transitions that are equivalent to the given set of
     * epsilon transitions.
     *
     * @param pairs
     *         collection of {@link StatePair} objects representing pairs of
     *         source/destination states where epsilon transitions should be
     *         added
     */
    public static void addEpsilons(WeightedAutomaton a, Collection<StatePair>
            pairs) {
        a.expandSingleton();
        HashMap<WeightedState, HashSet<WeightedState>> forward = new
                HashMap<WeightedState, HashSet<WeightedState>>();
        HashMap<WeightedState, HashSet<WeightedState>> back =
                new HashMap<WeightedState, HashSet<WeightedState>>();
        for (StatePair p : pairs) {
            HashSet<WeightedState> to = forward.get(p.s1);
            if (to == null) {
                to = new HashSet<WeightedState>();
                forward.put(p.s1, to);
            }
            to.add(p.s2);
            HashSet<WeightedState> from = back.get(p.s2);
            if (from == null) {
                from = new HashSet<WeightedState>();
                back.put(p.s2, from);
            }
            from.add(p.s1);
        }
        // calculate epsilon closure
        LinkedList<StatePair> worklist = new LinkedList<StatePair>(pairs);
        HashSet<StatePair> workset = new HashSet<StatePair>(pairs);
        while (!worklist.isEmpty()) {
            StatePair p = worklist.removeFirst();
            workset.remove(p);
            HashSet<WeightedState> to = forward.get(p.s2);
            HashSet<WeightedState> from = back.get(p.s1);
            if (to != null) {
                for (WeightedState s : to) {
                    StatePair pp = new StatePair(p.s1, s);
                    if (!pairs.contains(pp)) {
                        pairs.add(pp);
                        forward.get(p.s1).add(s);
                        back.get(s).add(p.s1);
                        worklist.add(pp);
                        workset.add(pp);
                        if (from != null) {
                            for (WeightedState q : from) {
                                StatePair qq = new StatePair(q, p.s1);
                                if (!workset.contains(qq)) {
                                    worklist.add(qq);
                                    workset.add(qq);
                                }
                            }
                        }
                    }
                }
            }
        }
        // add transitions
        for (StatePair p : pairs) {
            p.s1.addEpsilon(p.s2);
        }
        a.deterministic = false;
        a.clearHashCode();
        a.checkMinimizeAlways();
    }

    /**
     * Returns a (deterministic) automaton that accepts the complement of the
     * language of the given automaton.
     * <p>
     * Complexity: linear in number of states (if already deterministic).
     */
    static public WeightedAutomaton complement(WeightedAutomaton a) {
        a = a.cloneExpandedIfRequired();
        a.determinize();
        a.totalize();
        for (WeightedState p : a.getStates()) {
            p.setAccept(!p.isAccept());
        }
        a.removeDeadTransitions();
        return a;
    }

    /**
     * Returns an automaton that accepts the concatenation of the languages of
     * the given automata.
     * <p>
     * Complexity: linear in number of states.
     */
    static public WeightedAutomaton concatenate(WeightedAutomaton a1,
                                                WeightedAutomaton a2) {
        if (a1.isSingleton() && a2.isSingleton()) {
            return BasicAutomata.makeString(a1.singleton + a2.singleton);
        }
        if (isEmpty(a1) || isEmpty(a2)) {
            return BasicAutomata.makeEmpty();
        }
        boolean deterministic = a1.isSingleton() && a2.isDeterministic();
        if (a1 == a2) {
            a1 = a1.cloneExpanded();
            a2 = a2.cloneExpanded();
        } else {
            a1 = a1.cloneExpandedIfRequired();
            a2 = a2.cloneExpandedIfRequired();
        }
        for (WeightedState s : a1.getAcceptStates()) {
            s.setAccept(false);
            s.addEpsilon(a2.initial);
        }
        a1.deterministic = deterministic;
        a1.clearHashCode();
        a1.checkMinimizeAlways();
        return a1;
    }

    /**
     * Returns true if the given automaton accepts no strings.
     */
    public static boolean isEmpty(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return false;
        }
        return !a.initial.isAccept() && a.initial.getTransitions().isEmpty();
    }

    /**
     * Determinizes the given automaton.
     * <p>
     * Complexity: exponential in number of states.
     */
    public static void determinize(WeightedAutomaton a) {
        if (a.deterministic || a.isSingleton()) {
            return;
        }
        Set<WeightedState> initialset = new HashSet<WeightedState>();
        initialset.add(a.initial);
        determinize(a, initialset);
    }

    /**
     * Determinizes the given automaton using the given set of initial states.
     */
    static void determinize(WeightedAutomaton a,
                            Set<WeightedState> initialset) {
        char[] points = a.getStartPoints();
        // subset construction
        Map<Set<WeightedState>, Set<WeightedState>> sets =
                new HashMap<Set<WeightedState>, Set<WeightedState>>();
        LinkedList<Set<WeightedState>> worklist =
                new LinkedList<Set<WeightedState>>();
        Map<Set<WeightedState>, WeightedState> newstate =
                new HashMap<Set<WeightedState>, WeightedState>();
        sets.put(initialset, initialset);
        worklist.add(initialset);
        a.initial = new WeightedState();
        newstate.put(initialset, a.initial);
        while (worklist.size() > 0) {
            Set<WeightedState> s = worklist.removeFirst();
            WeightedState r = newstate.get(s);
            for (WeightedState q : s) {
                if (q.isAccept()) {
                    r.setAccept(true);
                    break;
                }
            }
            for (int n = 0; n < points.length; n++) {
                Set<WeightedState> p = new HashSet<WeightedState>();
                for (WeightedState q : s) {
                    for (WeightedTransition t : q.getTransitions()) {
                        if (t.getMin() <= points[n] &&
                            points[n] <= t.getMax()) {
                            p.add(t.getDest());
                        }
                    }
                }
                if (!sets.containsKey(p)) {
                    sets.put(p, p);
                    worklist.add(p);
                    newstate.put(p, new WeightedState());
                }
                WeightedState q = newstate.get(p);
                char min = points[n];
                char max;
                if (n + 1 < points.length) {
                    max = (char) (points[n + 1] - 1);
                } else {
                    max = Character.MAX_VALUE;
                }
                r.getTransitions().add(new WeightedTransition(min, max, q));
            }
        }
        a.deterministic = true;
        a.removeDeadTransitions();
    }

    /**
     * Returns a shortest accepted/rejected string. If more than one shortest
     * string is found, the lexicographically first of the shortest strings is
     * returned.
     *
     * @param accepted
     *         if true, look for accepted strings; otherwise, look for rejected
     *         strings
     *
     * @return the string, null if none found
     */
    public static String getShortestExample(WeightedAutomaton a, boolean
            accepted) {
        if (a.isSingleton()) {
            if (accepted) {
                return a.singleton;
            } else if (a.singleton.length() > 0) {
                return "";
            } else {
                return "\u0000";
            }

        }
        return getShortestExample(a.getInitialState(), accepted);
    }

    static String getShortestExample(WeightedState s, boolean accepted) {
        Map<WeightedState, String> path = new HashMap<WeightedState, String>();
        LinkedList<WeightedState> queue = new LinkedList<WeightedState>();
        path.put(s, "");
        queue.add(s);
        String best = null;
        while (!queue.isEmpty()) {
            WeightedState q = queue.removeFirst();
            String p = path.get(q);
            if (q.isAccept() == accepted) {
                if (best == null ||
                    p.length() < best.length() ||
                    (p.length() == best.length() && p.compareTo(best) < 0)) {
                    best = p;
                }
            } else {
                for (WeightedTransition t : q.getTransitions()) {
                    String tp = path.get(t.getDest());
                    String np = p + t.getMin();
                    if (tp == null ||
                        (tp.length() == np.length() && np.compareTo(tp) < 0)) {
                        if (tp == null) {
                            queue.addLast(t.getDest());
                        }
                        path.put(t.getDest(), np);
                    }
                }
            }
        }
        return best;
    }

    /**
     * Returns true if the given automaton accepts the empty string and nothing
     * else.
     */
    public static boolean isEmptyString(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return a.singleton.length() == 0;
        } else {
            return a.initial.isAccept() && a.initial.getTransitions().isEmpty();
        }
    }

    /**
     * Returns true if the given automaton accepts all strings.
     */
    public static boolean isTotal(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return false;
        }
        if (a.initial.isAccept() && a.initial.getTransitions().size() == 1) {
            WeightedTransition t = a.initial.getTransitions().iterator().next();
            return t.getDest() == a.initial &&
                   t.getMin() == Character.MIN_VALUE &&
                   t.getMax() == Character.MAX_VALUE;
        }
        return false;
    }

    /**
     * Returns a (deterministic) automaton that accepts the intersection of the
     * language of <code>a1</code> and the complement of the language of
     * <code>a2</code>. As a side-effect, the automata may be determinized, if
     * not already deterministic.
     * <p>
     * Complexity: quadratic in number of states (if already deterministic).
     */
    static public WeightedAutomaton minus(WeightedAutomaton a1,
                                          WeightedAutomaton a2) {
        if (a1.isEmpty() || a1 == a2) {
            return BasicAutomata.makeEmpty();
        }
        if (a2.isEmpty()) {
            return a1.cloneIfRequired();
        }
        if (a1.isSingleton()) {
            if (a2.run(a1.singleton)) {
                return BasicAutomata.makeEmpty();
            } else {
                return a1.cloneIfRequired();
            }
        }
        return intersection(a1, a2.complement());
    }

    /**
     * Returns an automaton that accepts the intersection of
     * the languages of the given automata.
     * Never modifies the input automata languages.
     * <p>
     * Complexity: quadratic in number of states.
     */
    static public WeightedAutomaton intersection(WeightedAutomaton a1,
                                                 WeightedAutomaton a2) {
        if (a1.isSingleton()) {
            if (a2.run(a1.singleton)) {
                return a1.cloneIfRequired();
            } else {
                return BasicAutomata.makeEmpty();
            }
        }
        if (a2.isSingleton()) {
            if (a1.run(a2.singleton)) {
                return a2.cloneIfRequired();
            } else {
                return BasicAutomata.makeEmpty();
            }
        }
        if (a1 == a2) {
            return a1.cloneIfRequired();
        }
        WeightedTransition[][] transitions1 =
                WeightedAutomaton.getSortedTransitions(a1.getStates());
        WeightedTransition[][] transitions2 =
                WeightedAutomaton.getSortedTransitions(a2.getStates());
        WeightedAutomaton c = new WeightedAutomaton();
        LinkedList<StatePair> worklist = new LinkedList<StatePair>();
        HashMap<StatePair, StatePair> newstates =
                new HashMap<StatePair, StatePair>();
        StatePair p = new StatePair(c.initial, a1.initial, a2.initial);
        worklist.add(p);
        newstates.put(p, p);
        while (worklist.size() > 0) {
            p = worklist.removeFirst();
            p.s.setAccept(p.s1.isAccept() && p.s2.isAccept());
            WeightedTransition[] t1 = transitions1[p.s1.getNumber()];
            WeightedTransition[] t2 = transitions2[p.s2.getNumber()];
            for (int n1 = 0, b2 = 0; n1 < t1.length; n1++) {
                while (b2 < t2.length && t2[b2].getMax() < t1[n1].getMin()) {
                    b2++;
                }
                for (int n2 = b2;
                     n2 < t2.length && t1[n1].getMax() >= t2[n2].getMin();
                     n2++) {
                    if (t2[n2].getMax() >= t1[n1].getMin()) {
                        StatePair q = new StatePair(t1[n1].getDest(),
                                                    t2[n2].getDest());
                        StatePair r = newstates.get(q);
                        if (r == null) {
                            q.s = new WeightedState();
                            worklist.add(q);
                            newstates.put(q, q);
                            r = q;
                        }
                        char min = t1[n1].getMin() > t2[n2].getMin() ?
                                   t1[n1].getMin() :
                                   t2[n2].getMin();
                        char max = t1[n1].getMax() < t2[n2].getMax() ?
                                   t1[n1].getMax() :
                                   t2[n2].getMax();
                        p.s.getTransitions()
                           .add(new WeightedTransition(min, max, r.s));
                    }
                }
            }
        }
        c.deterministic = a1.deterministic && a2.deterministic;
        c.removeDeadTransitions();
        c.checkMinimizeAlways();
        return c;
    }

    /**
     * Returns an automaton that accepts the union of the empty string and the
     * language of the given automaton.
     * <p>
     * Complexity: linear in number of states.
     */
    static public WeightedAutomaton optional(WeightedAutomaton a) {
        a = a.cloneExpandedIfRequired();
        WeightedState s = new WeightedState();
        s.addEpsilon(a.initial);
        s.setAccept(true);
        a.initial = s;
        a.deterministic = false;
        a.clearHashCode();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Returns an automaton that accepts <code>min</code> or more
     * concatenated repetitions of the language of the given automaton.
     * <p>
     * Complexity: linear in number of states and in <code>min</code>.
     */
    static public WeightedAutomaton repeat(WeightedAutomaton a, int min) {
        if (min == 0) {
            return repeat(a);
        }
        List<WeightedAutomaton> as = new ArrayList<WeightedAutomaton>();
        while (min-- > 0) {
            as.add(a);
        }
        as.add(repeat(a));
        return concatenate(as);
    }

    /**
     * Returns an automaton that accepts the concatenation of the languages of
     * the given automata.
     * <p>
     * Complexity: linear in total number of states.
     */
    static public WeightedAutomaton concatenate(List<WeightedAutomaton> l) {
        if (l.isEmpty()) {
            return BasicAutomata.makeEmptyString();
        }
        boolean all_singleton = true;
        for (WeightedAutomaton a : l) {
            if (!a.isSingleton()) {
                all_singleton = false;
                break;
            }
        }
        if (all_singleton) {
            StringBuilder b = new StringBuilder();
            for (WeightedAutomaton a : l) {
                b.append(a.singleton);
            }
            return BasicAutomata.makeString(b.toString());
        } else {
            for (WeightedAutomaton a : l) {
                if (a.isEmpty()) {
                    return BasicAutomata.makeEmpty();
                }
            }
            Set<Integer> ids = new HashSet<Integer>();
            for (WeightedAutomaton a : l) {
                ids.add(System.identityHashCode(a));
            }
            boolean has_aliases = ids.size() != l.size();
            WeightedAutomaton b = l.get(0);
            if (has_aliases) {
                b = b.cloneExpanded();
            } else {
                b = b.cloneExpandedIfRequired();
            }
            Set<WeightedState> ac = b.getAcceptStates();
            boolean first = true;
            for (WeightedAutomaton a : l) {
                if (first) {
                    first = false;
                } else {
                    if (a.isEmptyString()) {
                        continue;
                    }
                    WeightedAutomaton aa = a;
                    if (has_aliases) {
                        aa = aa.cloneExpanded();
                    } else {
                        aa = aa.cloneExpandedIfRequired();
                    }
                    Set<WeightedState> ns = aa.getAcceptStates();
                    for (WeightedState s : ac) {
                        s.setAccept(false);
                        s.addEpsilon(aa.initial);
                        if (s.isAccept()) {
                            ns.add(s);
                        }
                    }
                    ac = ns;
                }
            }
            b.deterministic = false;
            b.clearHashCode();
            b.checkMinimizeAlways();
            return b;
        }
    }

    /**
     * Returns an automaton that accepts the Kleene star (zero or more
     * concatenated repetitions) of the language of the given automaton.
     * Never modifies the input automaton language.
     * <p>
     * Complexity: linear in number of states.
     */
    static public WeightedAutomaton repeat(WeightedAutomaton a) {
        a = a.cloneExpanded();
        WeightedState s = new WeightedState();
        s.setAccept(true);
        s.addEpsilon(a.initial);
        for (WeightedState p : a.getAcceptStates()) {
            p.addEpsilon(s);
        }
        a.initial = s;
        a.deterministic = false;
        a.clearHashCode();
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Returns an automaton that accepts between <code>min</code> and
     * <code>max</code> (including both) concatenated repetitions of the
     * language of the given automaton.
     * <p>
     * Complexity: linear in number of states and in <code>min</code> and
     * <code>max</code>.
     */
    static public WeightedAutomaton repeat(WeightedAutomaton a, int min, int
            max) {
        if (min > max) {
            return BasicAutomata.makeEmpty();
        }
        max -= min;
        a.expandSingleton();
        WeightedAutomaton b;
        if (min == 0) {
            b = BasicAutomata.makeEmptyString();
        } else if (min == 1) {
            b = a.clone();
        } else {
            List<WeightedAutomaton> as = new ArrayList<WeightedAutomaton>();
            while (min-- > 0) {
                as.add(a);
            }
            b = concatenate(as);
        }
        if (max > 0) {
            WeightedAutomaton d = a.clone();
            while (--max > 0) {
                WeightedAutomaton c = a.clone();
                for (WeightedState p : c.getAcceptStates()) {
                    p.addEpsilon(d.initial);
                }
                d = c;
            }
            for (WeightedState p : b.getAcceptStates()) {
                p.addEpsilon(d.initial);
            }
            b.deterministic = false;
            b.clearHashCode();
            b.checkMinimizeAlways();
        }
        return b;
    }

    /**
     * Returns true if the given string is accepted by the automaton.
     * <p>
     * Complexity: linear in the length of the string.
     * <p>
     * <b>Note:</b> for full performance, use the {@link RunAutomaton} class.
     */
    public static boolean run(WeightedAutomaton a, String s) {
        if (a.isSingleton()) {
            return s.equals(a.singleton);
        }
        if (a.deterministic) {
            WeightedState p = a.initial;
            for (int i = 0; i < s.length(); i++) {
                WeightedState q = p.step(s.charAt(i)).getState();
                if (q == null) {
                    return false;
                }
                p = q;
            }
            return p.isAccept();
        } else {
            Set<WeightedState> states = a.getStates();
            WeightedAutomaton.setStateNumbers(states);
            LinkedList<WeightedState> pp = new LinkedList<WeightedState>();
            LinkedList<WeightedState> pp_other =
                    new LinkedList<WeightedState>();
            BitSet bb = new BitSet(states.size());
            BitSet bb_other = new BitSet(states.size());
            pp.add(a.initial);
            ArrayList<StateWeight> dest = new ArrayList<StateWeight>();
            boolean accept = a.initial.isAccept();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                accept = false;
                pp_other.clear();
                bb_other.clear();
                for (WeightedState p : pp) {
                    dest.clear();
                    p.step(c, dest);
                    for (StateWeight sw : dest) {
                        if (sw.getState().isAccept()) {
                            accept = true;
                        }
                        if (!bb_other.get(sw.getState().getNumber())) {
                            bb_other.set(sw.getState().getNumber());
                            pp_other.add(sw.getState());
                        }
                    }
                }
                LinkedList<WeightedState> tp = pp;
                pp = pp_other;
                pp_other = tp;
                BitSet tb = bb;
                bb = bb_other;
                bb_other = tb;
            }
            return accept;
        }
    }

    /**
     * Returns true if the language of <code>a1</code> is a subset of the
     * language of <code>a2</code>. As a side-effect, <code>a2</code> is
     * determinized if not already marked as deterministic.
     * <p>
     * Complexity: quadratic in number of states.
     */
    public static boolean subsetOf(WeightedAutomaton a1, WeightedAutomaton
            a2) {
        if (a1 == a2) {
            return true;
        }
        if (a1.isSingleton()) {
            if (a2.isSingleton()) {
                return a1.singleton.equals(a2.singleton);
            }
            return a2.run(a1.singleton);
        }
        a2.determinize();
        WeightedTransition[][] transitions1 =
                WeightedAutomaton.getSortedTransitions(a1.getStates());
        WeightedTransition[][] transitions2 =
                WeightedAutomaton.getSortedTransitions(a2.getStates());
        LinkedList<StatePair> worklist = new LinkedList<StatePair>();
        HashSet<StatePair> visited = new HashSet<StatePair>();
        StatePair p = new StatePair(a1.initial, a2.initial);
        worklist.add(p);
        visited.add(p);
        while (worklist.size() > 0) {
            p = worklist.removeFirst();
            if (p.s1.isAccept() && !p.s2.isAccept()) {
                return false;
            }
            WeightedTransition[] t1 = transitions1[p.s1.getNumber()];
            WeightedTransition[] t2 = transitions2[p.s2.getNumber()];
            for (int n1 = 0, b2 = 0; n1 < t1.length; n1++) {
                while (b2 < t2.length && t2[b2].getMax() < t1[n1].getMin()) {
                    b2++;
                }
                int min1 = t1[n1].getMin(), max1 = t1[n1].getMax();
                for (int n2 = b2;
                     n2 < t2.length && t1[n1].getMax() >= t2[n2].getMin();
                     n2++) {
                    if (t2[n2].getMin() > min1) {
                        return false;
                    }
                    if (t2[n2].getMax() < Character.MAX_VALUE) {
                        min1 = t2[n2].getMax() + 1;
                    } else {
                        min1 = Character.MAX_VALUE;
                        max1 = Character.MIN_VALUE;
                    }
                    StatePair q =
                            new StatePair(t1[n1].getDest(), t2[n2].getDest());
                    if (!visited.contains(q)) {
                        worklist.add(q);
                        visited.add(q);
                    }
                }
                if (min1 <= max1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns an automaton that accepts the union of the languages of the given
     * automata.
     * <p>
     * Complexity: linear in number of states.
     */
    public static WeightedAutomaton union(WeightedAutomaton a1,
                                          WeightedAutomaton a2) {
        if ((a1.isSingleton() &&
             a2.isSingleton() &&
             a1.singleton.equals(a2.singleton)) || a1 == a2) {
            return a1.cloneIfRequired();
        }
        if (a1 == a2) {
            a1 = a1.cloneExpanded();
            a2 = a2.cloneExpanded();
        } else {
            a1 = a1.cloneExpandedIfRequired();
            a2 = a2.cloneExpandedIfRequired();
        }
        WeightedState s = new WeightedState();
        s.addEpsilon(a1.initial);
        s.addEpsilon(a2.initial);
        a1.initial = s;
        a1.deterministic = false;
        a1.clearHashCode();
        a1.checkMinimizeAlways();
        return a1;
    }

    /**
     * Returns an automaton that accepts the union of the languages of the given
     * automata.
     * <p>
     * Complexity: linear in number of states.
     */
    public static WeightedAutomaton union(Collection<WeightedAutomaton> l) {
        Set<Integer> ids = new HashSet<Integer>();
        for (WeightedAutomaton a : l) {
            ids.add(System.identityHashCode(a));
        }
        boolean has_aliases = ids.size() != l.size();
        WeightedState s = new WeightedState();
        for (WeightedAutomaton b : l) {
            if (b.isEmpty()) {
                continue;
            }
            WeightedAutomaton bb = b;
            if (has_aliases) {
                bb = bb.cloneExpanded();
            } else {
                bb = bb.cloneExpandedIfRequired();
            }
            s.addEpsilon(bb.initial);
        }
        WeightedAutomaton a = new WeightedAutomaton();
        a.initial = s;
        a.deterministic = false;
        a.clearHashCode();
        a.checkMinimizeAlways();
        return a;
    }
}
