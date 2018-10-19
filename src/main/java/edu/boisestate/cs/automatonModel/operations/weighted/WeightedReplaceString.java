package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.string.util.MultiMap;
import edu.boisestate.cs.automaton.*;

import java.util.*;

public class WeightedReplaceString
        extends UnaryWeightedOperation {

    private final String find;
    private final String replace;

    public WeightedReplaceString(String find, String replace) {
        this.find = find;
        this.replace = replace;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {

        if (find.length() == 0)
            return emptyStringOp(automaton);

        WeightedAutomaton result = automaton.clone();

        assert result.isDeterministic();

        List<ConstrainedEpsilon> epsilons = new LinkedList<>();
        List<StateTransitionPair> killedTransitions = new LinkedList<>();
        List<StateTransitionPair> newTransitions = new LinkedList<>();
        String ghostString = find.substring(0, find.length() - 1);

        for (WeightedState origin : result.getStates()) {
            LinkedList<WeightedState> path = getPath(origin, find);

            if (path == null)
                continue;

            // create a path to read the replacement string
            LinkedList<WeightedState> replacement = makeString(replace);

            // create a path to read part of the search string, in case
            // only a prefix of the search string occurred
            LinkedList<WeightedState> ghost = makeString(ghostString);

            // connect to replacement string (head and tail)
            epsilons.add(new ConstrainedEpsilon(origin, replacement.getFirst()));
            epsilons.add(new ConstrainedEpsilon(replacement.getLast(), path.getLast()));

            // set accept states of first and last
            if (origin.isAccept()) {
                replacement.getFirst().setAccept(true);
                ghost.getFirst().setAccept(true);
            }
            if (path.getLast().isAccept()) {
                replacement.getLast().setAccept(true);
            }

            // connect to ghost string (head)
            epsilons.add(new ConstrainedEpsilon(origin, ghost.getFirst()));

            // connect to successive states in the ghost state,
            // and set accept states in the ghost
            Iterator<WeightedState> pathIt = path.iterator();
            Iterator<WeightedState> ghostIt = ghost.iterator();
            ghostIt.next(); // skip the initial state in the ghost path
            int index = 1;
            while (ghostIt.hasNext()) {
                assert pathIt.hasNext();
                WeightedState pathState = pathIt.next();
                WeightedState ghostState = ghostIt.next();

                // add an epsilon transition, but disallow reading the next
                // character from the search string if it is followed
                epsilons.add(new ConstrainedEpsilon(ghostState, pathState, find.charAt(index)));

                // set accept state
                if (pathState.isAccept()) {
                    ghostState.setAccept(true);
                }

                // next char in search string
                index++;
            }

            // remove the transition with the first character of
            // the search string from the origin state
            char first = find.charAt(0);
            for (WeightedTransition tr : origin.getTransitions()) {
                if (tr.getMin() <= first && tr.getMax() >= first) {
                    killedTransitions.add(new StateTransitionPair(origin, tr));

                    // add back the remaining characters from the interval
                    if (tr.getMin() < first) {
                        newTransitions.add(new StateTransitionPair(origin, new WeightedTransition(tr.getMin(), (char)(first-1), tr.getDest(), tr.getWeightInt())));
                    }
                    if (tr.getMax() > first) {
                        newTransitions.add(new StateTransitionPair(origin, new WeightedTransition((char)(first+1), tr.getMax(), tr.getDest(), tr.getWeightInt())));
                    }
                }
            }
        }

        // apply the first character removal first
        for (StateTransitionPair pair : killedTransitions) {
            pair.state.getTransitions().remove(pair.transition);
        }
        for (StateTransitionPair pair : newTransitions) {
            pair.state.addTransition(pair.transition);
        }

        // apply epsilons
        addConstrainedEpsilons(result, epsilons);
        result.reduce();
        result.minimize();

        return result;
    }

    @Override
    public String toString() {
        return "replace('" + find + "', '" + replace + "')";
    }

    /**
     * Creates states and transitions reading the specified string.
     * None of the states are made accept states. The states are returned
     * in a linked list in topological order.
     * @param s a string
     * @return a new list with all the states
     */
    private LinkedList<WeightedState> makeString(CharSequence s) {
        LinkedList<WeightedState> list = new LinkedList<WeightedState>();
        WeightedState first = new WeightedState();
        list.add(first);
        WeightedState last = first;
        for (int i=0; i<s.length(); i++) {
            WeightedState state = new WeightedState();
            last.addTransition(new WeightedTransition(s.charAt(i), state));
            list.add(state);
            last = state;
        }
        return list;
    }

    /**
     * Returns the states reached by feeding the specified input to
     * the automaton, starting at the specified state. The initial
     * state is not added to the path. The number of states in the
     * path equals the length of the string.
     * <p/>
     * Returns <tt>null</tt> if the path ends in the dead state.
     * @param start state to search path at.
     * @param string input to the automaton.
     * @return a new list of states, or <tt>null</tt>.
     */
    private LinkedList<WeightedState> getPath(WeightedState start, CharSequence string) {
        LinkedList<WeightedState> path = new LinkedList<>();
        WeightedState state = start;
        for (int i=0; i<string.length(); i++) {
            StateWeight sw = state.step(string.charAt(i));
            if (sw == null) {
                return null;
            }
            state = sw.getState();
            path.add(state);
        }
        return path;
    }

    /**
     * An epsilon transition with an optional "constrained" character.
     * See addConstrainedEpsilons.
     * Does NOT currently have equals() and hashCode()!
     */
    private static final class ConstrainedEpsilon {
        /** Origin of epsilon transition */
        public WeightedState from;

        /** Destination of epsilon transition */
        public WeightedState to;

        /** Illegal input after following epsilon, or null if not constrained */
        public Character illegalCharacter;

        /**
         * Returns the set of illegal characters, which is either empty or
         * a singleton set.
         */
        public Set<Character> illegalCharacters() {
            if (illegalCharacter == null)
                return Collections.emptySet();
            else
                return Collections.singleton(illegalCharacter);
        }

        public ConstrainedEpsilon(WeightedState from, WeightedState to, Character illegalCharacter) {
            this.from = from;
            this.to = to;
            this.illegalCharacter = illegalCharacter;
        }
        public ConstrainedEpsilon(WeightedState from, WeightedState to) {
            this.from = from;
            this.to = to;
            this.illegalCharacter = null;
        }

    }

    /**
     * A state and a transition. The transition will typically be
     * outgoing from the state, though it is not necessary.
     * Does NOT currently have equals() and hashCode()!
     */
    private static final class StateTransitionPair {
        public WeightedState state;
        public WeightedTransition transition;
        public StateTransitionPair(WeightedState state, WeightedTransition transition) {
            this.state = state;
            this.transition = transition;
        }
    }


    /**
     * Like adding normal epsilon transitions, except certain epsilon transition are
     * constrained, in that one specific character cannot be read after following that
     * transition (ie. reading that character goes to the dead rejecting state).
     * @param auto automaton to modify
     * @param epsilons constrained epsilon transitions to add.
     */
    private void addConstrainedEpsilons(WeightedAutomaton auto, Collection<ConstrainedEpsilon> epsilons) {
        // forward and backward contain all the epsilon transitions without illegal characters
        MultiMap<WeightedState, WeightedState> forward = new MultiMap<>();
        MultiMap<WeightedState, WeightedState> backward = new MultiMap<>();

        // 'all' contains key (s1,s2) if there is an epsilon transition from s1 to s2
        // (s1,s2) then maps to the set of illegal characters on that transition (can be empty)
        Map<WeightedStatePair, TreeSet<Character>> all = new HashMap<>();

        Set<WeightedStatePair> workset = new HashSet<>();

        // build the initial maps, without any closure
        for (ConstrainedEpsilon epsilon : epsilons) {
            // ignore degenerate epsilons
            if (epsilon.from == epsilon.to)
                continue;

            forward.add(epsilon.from, epsilon.to);
            backward.add(epsilon.to, epsilon.from);

            WeightedStatePair pair = new WeightedStatePair(epsilon.from, epsilon.to);
            if (!all.containsKey(pair))
                all.put(pair, new TreeSet<Character>(epsilon.illegalCharacters()));
            else
                all.get(pair).retainAll(epsilon.illegalCharacters());
            workset.add(pair);
        }

        // calculate the transitive closure.
        // for every eps.tr. (s1,s2), we find a transition (s2,s3), and create (s1,s3).
        // workset contains the (s1,s2) pairs that may yield a new transition like above.
        while (!workset.isEmpty()) {
            Iterator<WeightedStatePair> it = workset.iterator();
            WeightedStatePair pair = it.next();
            it.remove();

            WeightedState s1 = pair.getFirstState();
            WeightedState s2 = pair.getSecondState();

            for (WeightedState s3 : forward.getView(s2)) {
                // do not create degenerate epsilons
                if (s1 == s3)
                    continue;

                assert s1 != s2 && s2 != s3;

                TreeSet<Character> illegal = new TreeSet<Character>(all.get(pair));
                illegal.addAll(all.get(new WeightedStatePair(s2, s3)));

                boolean changed;
                WeightedStatePair p2 = new WeightedStatePair(s1, s3);
                if (!all.containsKey(p2)) {
                    all.put(p2, illegal);
                    forward.add(s1, s3); // note: we are not modifying the view being iterated because s1!=s2
                    backward.add(s3, s1);
                    changed = true;
                } else {
                    changed = all.get(p2).retainAll(illegal);
                }

                if (changed) {
                    workset.add(p2);
                    for (WeightedState s0 : backward.getView(s1)) {
                        workset.add(new WeightedStatePair(s0, s1));
                    }
                }
            }
        }

        // closure completed, time to add the transitions
        LinkedList<StateTransitionPair> transitions = new LinkedList<StateTransitionPair>();
        for (Map.Entry<WeightedStatePair, TreeSet<Character>> entry : all.entrySet()) {
            WeightedStatePair pair = entry.getKey();
            for (WeightedTransition tr : pair.getSecondState().getTransitions()) {
                char ch = tr.getMin();
                // TreeSet is sorted, so we visit the illegal characters in increasing order
                // at every illegal character, we add the interval below, since it must be a legal interval
                for (Character illegal : entry.getValue()) {
                    if (illegal < ch)
                        continue;
                    if (illegal > tr.getMax())
                        break;
                    if (illegal > ch) {
                        transitions.add(new StateTransitionPair(pair.getFirstState(), new WeightedTransition(ch, (char)(illegal - 1), tr.getDest(), tr.getWeightInt())));
                    }
                    ch = (char)(illegal + 1);
                }
                if (ch <= tr.getMax()) {
                    transitions.add(new StateTransitionPair(pair.getFirstState(), new WeightedTransition(ch, tr.getMax(), tr.getDest(), tr.getWeightInt())));
                }
            }
        }
        for (StateTransitionPair pair : transitions) {
            pair.state.addTransition(pair.transition);
        }
        auto.setDeterministic(false);
    }

    /**
     * If the search string is empty, the replace operation inserts the replacement string
     * between every character in the original string, and as prefix and suffix.
     * This does not generalize well, so the case is handled specifically by this method.
     * </p>
     * For every state <tt>s</tt>, we create a path to read the replacement. All outgoing
     * transitions from <tt>s</tt> are changed to go out from last state in the replacement path.
     * <tt>s</tt> then gets all the transitions from the first state in the replacement path.
     * If <tt>s</tt> was an accept state, the last state in its replacement path is made an accept
     * state, and <tt>s</tt> is no longer an accept state (because the replacement string is inserted
     * as suffix).
     * @param a input automaton. Will not be modified.
     * @return new automaton
     */
    private WeightedAutomaton emptyStringOp(WeightedAutomaton a) {
        WeightedAutomaton result = a.clone();

        for (WeightedState state : result.getStates()) {
            LinkedList<WeightedState> insert = makeString(replace);
            insert.getLast().getTransitions().addAll(state.getTransitions());
            state.getTransitions().clear();
            state.getTransitions().addAll(insert.getFirst().getTransitions());

            if (state.isAccept()) {
                insert.getLast().setAccept(true);
                state.setAccept(false);
            }
        }

        return result;
    }
}
