package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.*;

public class WeightedPreciseTrim extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "trim()";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        // TODO: adjust transition weights

        WeightedAutomaton b = a.clone();
        b.setDeterministic(false);
        Map<WeightedState, Set<WeightedState>> normalPrevs = new HashMap<>();
        Map<WeightedState, Set<WeightedState>> specialPrevs = new HashMap<>();
        findPrevious(b, normalPrevs, specialPrevs);
        Set<WeightedState> pre = findPreSet(b);
        Set<WeightedState> post = findPostSet(b, specialPrevs);
        boolean initial_accept = post.contains(b.getInitialState());
        WeightedState initial = new WeightedState();
        b.setInitialState(initial);
        for (WeightedState s : pre) {
            for (WeightedTransition t : new ArrayList<>(s.getTransitions())) {
                char min = t.getMin();
                char max = t.getMax();
                if (min <= '\u0020') {
                    min = '\u0021';
                }
                if (min <= max) {
                    initial.addTransition(new WeightedTransition(min, max, t.getDest(), t.getWeightInt()));
                    Set<WeightedState> prevSet = normalPrevs.get(t.getDest());
                    if (prevSet == null) {
                        prevSet = new HashSet<>();
                        normalPrevs.put(t.getDest(), prevSet);
                    }
                    prevSet.add(initial);
                }
            }
        }
        WeightedState accept = new WeightedState();
        accept.setAccept(true);
        for (WeightedState s : b.getAcceptStates()) {
            s.setAccept(false);
        }
        if (initial_accept) {
            initial.setAccept(true);
        }
        for (WeightedState s : post) {
            Set<WeightedState> prevset = normalPrevs.get(s);
            if (prevset != null) {
                for (WeightedState p : prevset) {
                    for (WeightedTransition t : new ArrayList<>(p.getTransitions())) {
                        if (t.getDest() == s) {
                            char min = t.getMin();
                            char max = t.getMax();
                            if (min <= '\u0020') {
                                min = '\u0021';
                            }
                            if (min <= max) {
                                p.addTransition(new WeightedTransition(min, max, accept, t.getWeightInt()));
                            }
                        }
                    }
                }
            }
        }
        b.minimize();
        return b;
    }

    private Set<WeightedState> findPostSet(WeightedAutomaton b, Map<WeightedState, Set<WeightedState>> s_prev) {
        Set<WeightedState> post = new HashSet<>();
        TreeSet<WeightedState> pending = new TreeSet<>();
        pending.addAll(b.getAcceptStates());
        while (!pending.isEmpty()) {
            WeightedState p = pending.first();
            pending.remove(p);
            post.add(p);
            Set<WeightedState> prevSet = s_prev.get(p);
            if (prevSet != null) {
                for (WeightedState q : prevSet) {
                    if (!post.contains(q)) {
                        pending.add(q);
                    }
                }
            }
        }
        return post;
    }

    private Set<WeightedState> findPreSet(WeightedAutomaton b) {
        Set<WeightedState> pre = new HashSet<>();
        TreeSet<WeightedState> pending = new TreeSet<>();
        pending.add(b.getInitialState());
        while (!pending.isEmpty()) {
            WeightedState p = pending.first();
            pending.remove(p);
            pre.add(p);
            for (WeightedTransition t : p.getTransitions()) {
                if (t.getMin() <= '\u0020') {
                    WeightedState q = t.getDest();
                    if (!pre.contains(q)) {
                        pending.add(q);
                    }
                }
            }
        }
        return pre;
    }

    private void findPrevious(WeightedAutomaton b,
                              Map<WeightedState, Set<WeightedState>> n_prev,
                              Map<WeightedState, Set<WeightedState>> s_prev) {
        for (WeightedState s : b.getStates()) {
            for (WeightedTransition t : s.getTransitions()) {
                char min = t.getMin();
                char max = t.getMax();
                WeightedState dest = t.getDest();
                if (min <= '\u0020') {
                    Set<WeightedState> prevSet = s_prev.get(dest);
                    if (prevSet == null) {
                        prevSet = new HashSet<>();
                        s_prev.put(dest, prevSet);
                    }
                    prevSet.add(s);
                }
                if (max > '\u0020') {
                    Set<WeightedState> prevSet = n_prev.get(dest);
                    if (prevSet == null) {
                        prevSet = new HashSet<>();
                        n_prev.put(dest, prevSet);
                    }
                    prevSet.add(s);
                }
            }
        }
    }
}
