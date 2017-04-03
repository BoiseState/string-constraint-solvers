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
 * Automata operations involving shuffling.
 */
final public class ShuffleOperations {

    private ShuffleOperations() {
    }

    private static void add(Character suspend_shuffle,
                            Character resume_shuffle,
                            LinkedList<ShuffleConfiguration> pending,
                            Set<ShuffleConfiguration> visited,
                            ShuffleConfiguration c,
                            int i1,
                            WeightedTransition t1,
                            WeightedTransition t2,
                            char min,
                            char max) {
        final char HIGH_SURROGATE_BEGIN = '\uD800';
        final char HIGH_SURROGATE_END = '\uDBFF';
        if (suspend_shuffle != null &&
            min <= suspend_shuffle &&
            suspend_shuffle <= max &&
            min != max) {
            if (min < suspend_shuffle) {
                add(suspend_shuffle,
                    resume_shuffle,
                    pending,
                    visited,
                    c,
                    i1,
                    t1,
                    t2,
                    min,
                    (char) (suspend_shuffle - 1));
            }
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                suspend_shuffle,
                suspend_shuffle);
            if (suspend_shuffle < max) {
                add(suspend_shuffle,
                    resume_shuffle,
                    pending,
                    visited,
                    c,
                    i1,
                    t1,
                    t2,
                    (char) (suspend_shuffle + 1),
                    max);
            }
        } else if (resume_shuffle != null &&
                   min <= resume_shuffle &&
                   resume_shuffle <= max &&
                   min != max) {
            if (min < resume_shuffle) {
                add(suspend_shuffle,
                    resume_shuffle,
                    pending,
                    visited,
                    c,
                    i1,
                    t1,
                    t2,
                    min,
                    (char) (resume_shuffle - 1));
            }
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                resume_shuffle,
                resume_shuffle);
            if (resume_shuffle < max) {
                add(suspend_shuffle,
                    resume_shuffle,
                    pending,
                    visited,
                    c,
                    i1,
                    t1,
                    t2,
                    (char) (resume_shuffle + 1),
                    max);
            }
        } else if (min < HIGH_SURROGATE_BEGIN && max >= HIGH_SURROGATE_BEGIN) {
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                min,
                (char) (HIGH_SURROGATE_BEGIN - 1));
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                HIGH_SURROGATE_BEGIN,
                max);
        } else if (min <= HIGH_SURROGATE_END && max > HIGH_SURROGATE_END) {
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                min,
                HIGH_SURROGATE_END);
            add(suspend_shuffle,
                resume_shuffle,
                pending,
                visited,
                c,
                i1,
                t1,
                t2,
                (char) (HIGH_SURROGATE_END + 1),
                max);
        } else {
            ShuffleConfiguration nc = new ShuffleConfiguration(c,
                                                               i1,
                                                               t1.getDest(),
                                                               t2.getDest(),
                                                               min);
            if (suspend_shuffle != null && min == suspend_shuffle) {
                nc.shuffle_suspended = true;
                nc.suspended1 = i1;
            } else if (resume_shuffle != null && min == resume_shuffle) {
                nc.shuffle_suspended = false;
            }
            if (min >= HIGH_SURROGATE_BEGIN && min <= HIGH_SURROGATE_BEGIN) {
                nc.shuffle_suspended = true;
                nc.suspended1 = i1;
                nc.surrogate = true;
            }
            if (!visited.contains(nc)) {
                pending.add(nc);
                visited.add(nc);
            }
        }
    }

    /**
     * Returns an automaton that accepts the shuffle (interleaving) of the
     * languages of the given automata. As a side-effect, both automata are
     * determinized, if not already deterministic. Never modifies the input
     * automata languages.
     * <p>
     * Complexity: quadratic in number of states (if already deterministic).
     * <p>
     * <dl><dt><b>Author:</b></dt><dd>Torben Ruby &lt;<a
     * href="mailto:ruby@daimi.au.dk">ruby@daimi.au.dk</a>&gt;</dd></dl>
     */
    public static WeightedAutomaton shuffle(WeightedAutomaton a1,
                                            WeightedAutomaton a2) {
        a1.determinize();
        a2.determinize();
        WeightedTransition[][] transitions1 =
                WeightedAutomaton.getSortedTransitions(a1.getStates());
        WeightedTransition[][] transitions2 =
                WeightedAutomaton.getSortedTransitions(a2.getStates());
        WeightedAutomaton c = new WeightedAutomaton();
        LinkedList<WeightedStatePair> worklist = new LinkedList<WeightedStatePair>();
        HashMap<WeightedStatePair, WeightedStatePair> newstates =
                new HashMap<WeightedStatePair, WeightedStatePair>();
        WeightedState s = new WeightedState();
        c.initial = s;
        WeightedStatePair p = new WeightedStatePair(s, a1.initial, a2.initial);
        worklist.add(p);
        newstates.put(p, p);
        while (worklist.size() > 0) {
            p = worklist.removeFirst();
            p.s.setAccept(p.s1.isAccept() && p.s2.isAccept());
            WeightedTransition[] t1 = transitions1[p.s1.getNumber()];
            for (int n1 = 0; n1 < t1.length; n1++) {
                WeightedStatePair q = new WeightedStatePair(t1[n1].getDest(), p.s2);
                WeightedStatePair r = newstates.get(q);
                if (r == null) {
                    q.s = new WeightedState();
                    worklist.add(q);
                    newstates.put(q, q);
                    r = q;
                }
                p.s.getTransitions()
                   .add(new WeightedTransition(t1[n1].getMin(),
                                               t1[n1].getMax(),
                                               r.s));
            }
            WeightedTransition[] t2 = transitions2[p.s2.getNumber()];
            for (int n2 = 0; n2 < t2.length; n2++) {
                WeightedStatePair q = new WeightedStatePair(p.s1, t2[n2].getDest());
                WeightedStatePair r = newstates.get(q);
                if (r == null) {
                    q.s = new WeightedState();
                    worklist.add(q);
                    newstates.put(q, q);
                    r = q;
                }
                p.s.getTransitions()
                   .add(new WeightedTransition(t2[n2].getMin(),
                                               t2[n2].getMax(),
                                               r.s));
            }
        }
        c.deterministic = false;
        c.removeDeadTransitions();
        c.checkMinimizeAlways();
        return c;
    }

    /**
     * Returns a string that is an interleaving of strings that are accepted by
     * <code>ca</code> but not by <code>a</code>. If no such string exists,
     * null
     * is returned. As a side-effect, <code>a</code> is determinized, if not
     * already deterministic. Only interleavings that respect the
     * suspend/resume
     * markers (two BMP private code points) are considered if the markers are
     * non-null. Also, interleavings never split surrogate pairs.
     * <p>
     * Complexity: proportional to the product of the numbers of states (if
     * <code>a</code> is already deterministic).
     */
    public static String shuffleSubsetOf(Collection<WeightedAutomaton> ca,
                                         WeightedAutomaton a,
                                         Character suspend_shuffle,
                                         Character resume_shuffle) {
        if (ca.size() == 0) {
            return null;
        }
        if (ca.size() == 1) {
            WeightedAutomaton a1 = ca.iterator().next();
            if (a1.isSingleton()) {
                if (a.run(a1.singleton)) {
                    return null;
                } else {
                    return a1.singleton;
                }
            }
            if (a1 == a) {
                return null;
            }
        }
        a.determinize();
        WeightedTransition[][][] ca_transitions =
                new WeightedTransition[ca.size()][][];
        int i = 0;
        for (WeightedAutomaton a1 : ca) {
            ca_transitions[i++] =
                    WeightedAutomaton.getSortedTransitions(a1.getStates());
        }
        WeightedTransition[][] a_transitions =
                WeightedAutomaton.getSortedTransitions(a.getStates());
        WeightedTransitionComparator
                tc = new WeightedTransitionComparator(false);
        ShuffleConfiguration init = new ShuffleConfiguration(ca, a);
        LinkedList<ShuffleConfiguration> pending =
                new LinkedList<ShuffleConfiguration>();
        Set<ShuffleConfiguration> visited = new HashSet<ShuffleConfiguration>();
        pending.add(init);
        visited.add(init);
        while (!pending.isEmpty()) {
            ShuffleConfiguration c = pending.removeFirst();
            boolean good = true;
            for (int i1 = 0; i1 < ca.size(); i1++) {
                if (!c.ca_states[i1].isAccept()) {
                    good = false;
                    break;
                }
            }
            if (c.a_state.isAccept()) {
                good = false;
            }
            if (good) {
                StringBuilder sb = new StringBuilder();
                while (c.prev != null) {
                    sb.append(c.min);
                    c = c.prev;
                }
                StringBuilder sb2 = new StringBuilder();
                for (int j = sb.length() - 1; j >= 0; j--) {
                    sb2.append(sb.charAt(j));
                }
                return sb2.toString();
            }
            WeightedTransition[] ta2 = a_transitions[c.a_state.getNumber()];
            for (int i1 = 0; i1 < ca.size(); i1++) {
                if (c.shuffle_suspended) {
                    i1 = c.suspended1;
                }
                loop:
                for (WeightedTransition t1 : ca_transitions[i1][c
						.ca_states[i1].getNumber()]) {
                    List<WeightedTransition> lt =
                            new ArrayList<WeightedTransition>();
                    int j = Arrays.binarySearch(ta2, t1, tc);
                    if (j < 0) {
                        j = -j - 1;
                    }
                    if (j > 0 && ta2[j - 1].getMax() >= t1.getMin()) {
                        j--;
                    }
                    while (j < ta2.length) {
                        WeightedTransition t2 = ta2[j++];
                        char min = t1.getMin();
                        char max = t1.getMax();
                        if (t2.getMin() > min) {
                            min = t2.getMin();
                        }
                        if (t2.getMax() < max) {
                            max = t2.getMax();
                        }
                        if (min <= max) {
                            add(suspend_shuffle,
                                resume_shuffle,
                                pending,
                                visited,
                                c,
                                i1,
                                t1,
                                t2,
                                min,
                                max);
                            lt.add(new WeightedTransition(min, max, null));
                        } else {
                            break;
                        }
                    }
                    WeightedTransition[] at =
                            lt.toArray(new WeightedTransition[lt.size()]);
                    Arrays.sort(at, tc);
                    char min = t1.getMin();
                    for (int k = 0; k < at.length; k++) {
                        if (at[k].getMin() > min) {
                            break;
                        }
                        if (at[k].getMax() >= t1.getMax()) {
                            continue loop;
                        }
                        min = (char) (at[k].getMax() + 1);
                    }
                    ShuffleConfiguration nc =
                            new ShuffleConfiguration(c, i1, t1.getDest(), min);
                    StringBuilder sb = new StringBuilder();
                    ShuffleConfiguration b = nc;
                    while (b.prev != null) {
                        sb.append(b.min);
                        b = b.prev;
                    }
                    StringBuilder sb2 = new StringBuilder();
                    for (int m = sb.length() - 1; m >= 0; m--) {
                        sb2.append(sb.charAt(m));
                    }
                    if (c.shuffle_suspended) {
                        sb2.append(BasicWeightedOperations.getShortestExample(nc
                                                                              .ca_states[c.suspended1],
                                                                              true));
                    }
                    for (i1 = 0; i1 < ca.size(); i1++) {
                        if (!c.shuffle_suspended || i1 != c.suspended1) {
                            sb2.append(BasicWeightedOperations.getShortestExample(nc
                                                                                  .ca_states[i1],
                                                                                  true));
                        }
                    }
                    return sb2.toString();
                }
                if (c.shuffle_suspended) {
                    break;
                }
            }
        }
        return null;
    }

    static class ShuffleConfiguration {

        WeightedState a_state;
        WeightedState[] ca_states;
        int hash;
        char min;
        ShuffleConfiguration prev;
        boolean shuffle_suspended;
        boolean surrogate;
        int suspended1;

        @SuppressWarnings("unused")
        private ShuffleConfiguration() {
        }

        ShuffleConfiguration(Collection<WeightedAutomaton> ca,
                             WeightedAutomaton a) {
            ca_states = new WeightedState[ca.size()];
            int i = 0;
            for (WeightedAutomaton a1 : ca) {
                ca_states[i++] = a1.getInitialState();
            }
            a_state = a.getInitialState();
            computeHash();
        }

        private void computeHash() {
            hash = 0;
            for (int i = 0; i < ca_states.length; i++) {
                hash ^= ca_states[i].hashCode();
            }
            hash ^= a_state.hashCode() * 100;
            if (shuffle_suspended || surrogate) {
                hash += suspended1;
            }
        }

        ShuffleConfiguration(ShuffleConfiguration c,
                             int i1,
                             WeightedState s1,
                             char min) {
            prev = c;
            ca_states = c.ca_states.clone();
            a_state = c.a_state;
            ca_states[i1] = s1;
            this.min = min;
            computeHash();
        }

        ShuffleConfiguration(ShuffleConfiguration c,
                             int i1,
                             WeightedState s1,
                             WeightedState s2,
                             char min) {
            prev = c;
            ca_states = c.ca_states.clone();
            a_state = c.a_state;
            ca_states[i1] = s1;
            a_state = s2;
            this.min = min;
            if (!surrogate) {
                shuffle_suspended = c.shuffle_suspended;
                suspended1 = c.suspended1;
            }
            computeHash();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ShuffleConfiguration) {
                ShuffleConfiguration c = (ShuffleConfiguration) obj;
                return shuffle_suspended == c.shuffle_suspended &&
                       surrogate == c.surrogate &&
                       suspended1 == c.suspended1 &&
                       Arrays.equals(ca_states, c.ca_states) &&
                       a_state == c.a_state;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
