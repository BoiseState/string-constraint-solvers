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
final public class BasicWeightedOperations {

    private BasicWeightedOperations() {
    }

    /**
     * Adds epsilon transitions to the given automaton. This method adds extra
     * character interval transitions that are equivalent to the given set of
     * epsilon transitions.
     *
     * @param pairs
     *         collection of {@link WeightedStatePair} objects representing
     *         pairs of source/destination states where epsilon transitions
     *         should be added
     */
    public static void addEpsilons(WeightedAutomaton a, Collection<WeightedStatePair> pairs) {
        a.expandSingleton();
        HashMap<WeightedState, HashSet<WeightedState>> forward = new HashMap<>();
        HashMap<WeightedState, HashSet<WeightedState>> back = new HashMap<>();
        for (WeightedStatePair p : pairs) {
            HashSet<WeightedState> to = forward.get(p.s1);
            if (to == null) {
                to = new HashSet<>();
                forward.put(p.s1, to);
            }
            to.add(p.s2);
            HashSet<WeightedState> from = back.get(p.s2);
            if (from == null) {
                from = new HashSet<>();
                back.put(p.s2, from);
            }
            from.add(p.s1);
        }
        // calculate epsilon closure
        LinkedList<WeightedStatePair> workList = new LinkedList<>(pairs);
        HashSet<WeightedStatePair> workSet = new HashSet<>(pairs);
        while (!workList.isEmpty()) {
            WeightedStatePair p = workList.removeFirst();
            workSet.remove(p);
            HashSet<WeightedState> to = forward.get(p.s2);
            HashSet<WeightedState> from = back.get(p.s1);
            if (to != null) {
                for (WeightedState s : to) {
                    WeightedStatePair pp = new WeightedStatePair(p.s1, s);
                    if (!pairs.contains(pp)) {
                        pairs.add(pp);
                        forward.get(p.s1).add(s);
                        back.get(s).add(p.s1);
                        workList.add(pp);
                        workSet.add(pp);
                        if (from != null) {
                            for (WeightedState q : from) {
                                WeightedStatePair qq = new WeightedStatePair(q, p.s1);
                                if (!workSet.contains(qq)) {
                                    workList.add(qq);
                                    workSet.add(qq);
                                }
                            }
                        }
                    }
                }
            }
        }
        // add transitions
        for (WeightedStatePair p : pairs) {
            p.s1.addEpsilon(p.s2, p.getWeight());
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
            return BasicWeightedAutomata.makeString(a1.singleton +
                                                    a2.singleton);
        }
        if (isEmpty(a1) || isEmpty(a2)) {
            return BasicWeightedAutomata.makeEmpty();
        }
        boolean deterministic = a1.isSingleton() && a2.isDeterministic();
        if (a1 == a2) {
            a1 = a1.cloneExpanded();
            a2 = a2.cloneExpanded();
        } else {
            a1 = a1.cloneExpandedIfRequired();
            a2 = a2.cloneExpandedIfRequired();
        }
        System.out.println("a1 " + a1);
        System.out.println("a2 " + a2);
        for (WeightedState s : a1.getAcceptStates()) {
            s.setAccept(false);
            s.addEpsilon(a2.initial);
        }
        System.out.println("a1 new " + a1);
        a1.deterministic = deterministic;
        a1.clearHashCode();
        a1.checkMinimizeAlways();
        return a1;
    }

    /**
     * Returns an automaton that accepts the concatenation of the languages of
     * the given automata.
     * <p>
     * Complexity: linear in total number of states.
     */
    static public WeightedAutomaton concatenate(List<WeightedAutomaton> l) {
        if (l.isEmpty()) {
            return BasicWeightedAutomata.makeEmptyString();
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
            return BasicWeightedAutomata.makeString(b.toString());
        } else {
            for (WeightedAutomaton a : l) {
                if (a.isEmpty()) {
                    return BasicWeightedAutomata.makeEmpty();
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
     * Determinizes the given automaton.
     * <p>
     * Complexity: exponential in number of states.
     */
    public static void determinize(WeightedAutomaton a) {
        if (a.deterministic || a.isSingleton()) {
            return;
        }
        Set<StateWeight> initialSet = new HashSet<>();
        StateWeight sw = new StateWeight(a.getInitialState(), a.getInitialFactor());
        initialSet.add(sw);
        determinize(a, initialSet);
    }

    /**
     * Determinizes the given automaton using the given set of initial states.
     */
    static void determinize(WeightedAutomaton a, Set<StateWeight> initialSet) {
        char[] points = a.getStartPoints();

        LinkedList<Set<StateWeight>> workList = new LinkedList<>();
        workList.add(initialSet);

        Map<Set<StateWeight>, Set<StateWeight>> sets = new HashMap<>();
        sets.put(initialSet, initialSet);

        Map<Set<StateWeight>, WeightedState> newState = new HashMap<>();
        a.initial = new WeightedState();
        newState.put(initialSet, a.initial);
        int factor = 0;
        for (StateWeight sw : initialSet) {
            if (sw.getState().isAccept()) {
                a.initial.setAccept(true);
                factor += 1;
                a.setInitialFactor(factor);
            }
        }

        while (workList.size() > 0) {
            Set<StateWeight> s = workList.removeFirst();
            WeightedState r = newState.get(s);
            for (int n = 0; n < points.length; n++) {
                Set<StateWeight> p = new HashSet<>();
                for (StateWeight q : s) {
                    Map<WeightedState, Integer> weightMap = new HashMap<>();
                    for (WeightedTransition t : q.getState().getTransitions()) {
                        if (t.getMin() <= points[n] &&
                            points[n] <= t.getMax()) {
                            WeightedState dest = t.getDest();
                            int weight = t.getWeightInt();
                            if (weightMap.containsKey(dest)) {
                                weight += weightMap.get(dest);
                            }
                            weightMap.put(dest, weight);
                        }
                    }
                    for (WeightedState dest : weightMap.keySet()) {
                        int newWeight = weightMap.get(dest) * q.getWeight();
                        StateWeight newSW = new StateWeight(dest, newWeight);
                        if (p.contains(newSW)) {
                            p.remove(newSW);
                            p.add(new StateWeight(dest, newWeight * 2));
                        } else {
                            p.add(newSW);
                        }
                    }
                }
                if (!sets.containsKey(p)) {
                    sets.put(p, p);
                    workList.add(p);
                    newState.put(p, new WeightedState());
                }
                WeightedState q = newState.get(p);
                for (StateWeight sw : p) {
                    if (sw.getState().isAccept()) {
                        q.setAccept(true);
                        break;
                    }
                }
                char min = points[n];
                char max;
                if (n + 1 < points.length) {
                    max = (char) (points[n + 1] - 1);
                } else {
                    max = Character.MAX_VALUE;
                }
                if (q.isAccept()) {
                    int weight = 0;
                    for (StateWeight sw : p) {
                        weight += sw.getWeight();
                    }
                    r.getTransitions().add(new WeightedTransition(min, max, q, weight));
                } else {
                    r.getTransitions().add(new WeightedTransition(min, max, q));
                }
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
     * Returns an automaton that accepts the intersection of
     * the languages of the given automata.
     * Never modifies the input automata languages.
     * <p>
     * Complexity: quadratic in number of states.
     */
    static public WeightedAutomaton intersection(WeightedAutomaton a1,
                                                 WeightedAutomaton a2) {
        if (a1.isEmpty() || a2.isEmpty()) {
            return BasicWeightedAutomata.makeEmpty();
        }
        if (a1.isSingleton()) {
            if (a2.run(a1.singleton)) {
                return a1.cloneIfRequired();
            } else {
                return BasicWeightedAutomata.makeEmpty();
            }
        }
        if (a2.isSingleton()) {
            if (a1.run(a2.singleton)) {
                WeightedAutomaton clone = a2.cloneIfRequired();
                clone.setNumEmptyStrings(a1.getNumEmptyStrings());
                clone.setInitialFactor(a1.getInitialFactor());
                return clone;
            } else {
                return BasicWeightedAutomata.makeEmpty();
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
        c.setInitialFactor(a1.getInitialFactor());
        c.setNumEmptyStrings(a1.getNumEmptyStrings());
        LinkedList<WeightedStatePair> workList = new LinkedList<>();
        HashMap<WeightedStatePair, WeightedStatePair> newStates =
                new HashMap<>();
        WeightedStatePair p =
                new WeightedStatePair(c.initial, a1.initial, a2.initial);
        workList.add(p);
        newStates.put(p, p);
        while (workList.size() > 0) {
            p = workList.removeFirst();
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
                        WeightedStatePair q =
                                new WeightedStatePair(t1[n1].getDest(),
                                                      t2[n2].getDest());
                        WeightedStatePair r = newStates.get(q);
                        if (r == null) {
                            q.s = new WeightedState();
                            workList.add(q);
                            newStates.put(q, q);
                            r = q;
                        }
                        char min = t1[n1].getMin() > t2[n2].getMin() ?
                                   t1[n1].getMin() :
                                   t2[n2].getMin();
                        char max = t1[n1].getMax() < t2[n2].getMax() ?
                                   t1[n1].getMax() :
                                   t2[n2].getMax();
                        int weight = t1[n1].getWeightInt();
                        p.s.getTransitions()
                           .add(new WeightedTransition(min, max, r.s, weight));
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
     * Returns true if the given automaton accepts no strings.
     */
    public static boolean isEmpty(WeightedAutomaton a) {
        if (a.isSingleton()) {
            return false;
        }
        return !a.initial.isAccept() && a.initial.getTransitions().isEmpty();
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
            return BasicWeightedAutomata.makeEmpty();
        }
        if (a2.isEmpty()) {
            return a1.cloneIfRequired();
        }
        if (a1.isSingleton()) {
            if (a2.run(a1.singleton)) {
                return BasicWeightedAutomata.makeEmpty();
            } else {
                return a1.cloneIfRequired();
            }
        }
        return intersection(a1, a2.complement());
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
            return BasicWeightedAutomata.makeEmpty();
        }
        max -= min;
        a.expandSingleton();
        WeightedAutomaton b;
        if (min == 0) {
            b = BasicWeightedAutomata.makeEmptyString();
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
                StateWeight q = p.step(s.charAt(i));
                if (q == null) {
                    return false;
                }
                p = q.getState();
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
        LinkedList<WeightedStatePair> worklist =
                new LinkedList<WeightedStatePair>();
        HashSet<WeightedStatePair> visited = new HashSet<WeightedStatePair>();
        WeightedStatePair p = new WeightedStatePair(a1.initial, a2.initial);
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
                    WeightedStatePair q =
                            new WeightedStatePair(t1[n1].getDest(),
                                                  t2[n2].getDest());
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
    public static WeightedAutomaton union(WeightedAutomaton a1, WeightedAutomaton a2) {
        if (a1 == a2) {
            a1 = a1.cloneExpanded();
            a2 = a2.cloneExpanded();
        } else {
            a1 = a1.cloneExpandedIfRequired();
            a2 = a2.cloneExpandedIfRequired();
        }

        int numEmptyStrings = 0;
        if (a1.initial.isAccept()) {
            numEmptyStrings += (a1.getInitialFactor() * a1.getNumEmptyStrings());
        }
        if (a2.initial.isAccept()) {
            numEmptyStrings += (a2.getInitialFactor() * a2.getNumEmptyStrings());
        }

        WeightedState s = new WeightedState();
        s.addEpsilon(a1.initial);
        s.addEpsilon(a2.initial);

        a1.initial = s;
        if (numEmptyStrings > 0) {
            a1.setNumEmptyStrings(numEmptyStrings);
        }
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
        Set<Integer> ids = new HashSet<>();
        for (WeightedAutomaton a : l) {
            ids.add(System.identityHashCode(a));
        }
        boolean has_aliases = ids.size() != l.size();
        WeightedState s = new WeightedState();
        int numEmptyStrings = 0;
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
            if (bb.initial.isAccept()) {
                numEmptyStrings += (bb.getInitialFactor() * bb.getNumEmptyStrings());
            }
            s.addEpsilon(bb.initial);
        }
        WeightedAutomaton a = new WeightedAutomaton();
        a.initial = s;
        if (numEmptyStrings > 0) {
            a.setNumEmptyStrings(numEmptyStrings);
        }
        a.deterministic = false;
        a.clearHashCode();
        a.checkMinimizeAlways();
        return a;
    }
}
