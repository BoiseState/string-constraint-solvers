package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.*;

public class PreciseTrim
        extends UnaryOperation {
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PreciseTrim;
    }

    @Override
    public Automaton op(Automaton a) {
        Automaton b = a.clone();
        b.setDeterministic(false);
        Map<State, Set<State>> normal_prevs = new HashMap<>();
        Map<State, Set<State>> special_prevs = new HashMap<>();
        findPrevs(b, normal_prevs, special_prevs);
        Set<State> pre = findPreSet(b);
        Set<State> post = findPostSet(b, special_prevs);
        boolean initial_accept = post.contains(b.getInitialState());
        State initial = new State();
        b.setInitialState(initial);
        for (State s : pre) {
            for (Transition t : new ArrayList<>(s.getTransitions())) {
                char min = t.getMin();
                char max = t.getMax();
                if (min <= '\u0020') {
                    min = '\u0021';
                }
                if (min <= max) {
                    initial.addTransition(new Transition(min, max, t.getDest()));
                    Set<State> prevset = normal_prevs.get(t.getDest());
                    if (prevset == null) {
                        prevset = new HashSet<>();
                        normal_prevs.put(t.getDest(), prevset);
                    }
                    prevset.add(initial);
                }
            }
        }
        State accept = new State();
        accept.setAccept(true);
        for (State s : b.getAcceptStates()) {
            s.setAccept(false);
        }
        if (initial_accept) {
            initial.setAccept(true);
        }
        for (State s : post) {
            Set<State> prevset = normal_prevs.get(s);
            if (prevset != null) {
                for (State p : prevset) {
                    for (Transition t : new ArrayList<>(p.getTransitions())) {
                        if (t.getDest() == s) {
                            char min = t.getMin();
                            char max = t.getMax();
                            if (min <= '\u0020') {
                                min = '\u0021';
                            }
                            if (min <= max) {
                                p.addTransition(new Transition(min, max, accept));
                            }
                        }
                    }
                }
            }
        }
        b.minimize();
        return b;
    }

    @Override
    public CharSet charsetTransfer(CharSet a) {
        return a;
    }

    @Override
    public String toString() {
        return "trim";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    private Set<State> findPostSet(Automaton b, Map<State, Set<State>> special_prevs) {
        Set<State> post = new HashSet<>();
        TreeSet<State> pending = new TreeSet<>();
        pending.addAll(b.getAcceptStates());
        while (!pending.isEmpty()) {
            State p = pending.first();
            pending.remove(p);
            post.add(p);
            Set<State> prevset = special_prevs.get(p);
            if (prevset != null) {
                for (State q : prevset) {
                    if (!post.contains(q)) {
                        pending.add(q);
                    }
                }
            }
        }
        return post;
    }

    private Set<State> findPreSet(Automaton b) {
        Set<State> pre = new HashSet<>();
        TreeSet<State> pending = new TreeSet<>();
        pending.add(b.getInitialState());
        while (!pending.isEmpty()) {
            State p = pending.first();
            pending.remove(p);
            pre.add(p);
            for (Transition t : p.getTransitions()) {
                if (t.getMin() <= '\u0020') {
                    State q = t.getDest();
                    if (!pre.contains(q)) {
                        pending.add(q);
                    }
                }
            }
        }
        return pre;
    }

    private void findPrevs(Automaton b, Map<State, Set<State>> normal_prevs, Map<State, Set<State>> special_prevs) {
        for (State s : b.getStates()) {
            for (Transition t : s.getTransitions()) {
                char min = t.getMin();
                char max = t.getMax();
                State dest = t.getDest();
                if (min <= '\u0020') {
                    Set<State> prevset = special_prevs.get(dest);
                    if (prevset == null) {
                        prevset = new HashSet<>();
                        special_prevs.put(dest, prevset);
                    }
                    prevset.add(s);
                }
                if (max > '\u0020') {
                    Set<State> prevset = normal_prevs.get(dest);
                    if (prevset == null) {
                        prevset = new HashSet<>();
                        normal_prevs.put(dest, prevset);
                    }
                    prevset.add(s);
                }
            }
        }
    }
}
