package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeightedAutomatonOperationTestUtilities {

    public static WeightedAutomaton getConcreteWeightedAutomaton(Alphabet alphabet, String string) {
        WeightedAutomaton concrete = BasicWeightedAutomata.makeString(string);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return concrete;
    }

    public static WeightedAutomaton getNonUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton nonUniform = getNonUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    public static WeightedAutomaton getNonUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        char c = alphabet.getCharSet().charAt(0);
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar(c)) .concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return nonUniform;
    }

    public static WeightedAutomaton getUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    public static WeightedAutomaton getUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        uniform.minimize();
        return uniform;
    }

    public static WeightedAutomaton getUnbalancedUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = getUniformBoundedWeightedAutomaton(alphabet, boundLength - 1);
        uniform.setInitialFactor(alphabet.size());
        return uniform;
    }

    public static WeightedAutomaton getUnbalancedUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength, int index) {
        WeightedAutomaton uniform = getUniformBoundedWeightedAutomaton(alphabet, boundLength - 1);

        // walk to index
        Set<WeightedState> states = walkToIndex(index, uniform);

        // unbalance transitions from states
        for (WeightedState s: states) {
            for (WeightedTransition t : s.getTransitions()) {
                t.setWeight(alphabet.size());
            }
        }

        return uniform;
    }

    private static Set<WeightedState> walkToIndex(int index, WeightedAutomaton automaton) {
        Set<WeightedState> states = new HashSet<>();
        states.add(automaton.getInitialState());
        for (int i = 0; i < index; i++) {
            Set<WeightedState> nextStates = new HashSet<>();
            for (WeightedState s : states) {
                for (WeightedTransition t : s.getTransitions()) {
                    nextStates.add(t.getDest());
                }
            }
            states = nextStates;
        }
        return states;
    }

    public static WeightedAutomaton getUnbalancedUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        uniform.setInitialFactor(alphabet.size());
        return uniform;
    }

    public static WeightedAutomaton getUnbalancedUniformUnboundedWeightedAutomaton(Alphabet alphabet, int index) {
        if (index == 0) {
            return getUnbalancedUniformUnboundedWeightedAutomaton(alphabet);
        }

        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);

        // walk to index
        WeightedState initial = new WeightedState();
        Set<WeightedState> states = new HashSet<>();
        states.add(initial);
        Map<WeightedState, WeightedState> stateMap = new HashMap<>();
        stateMap.put(initial, uniform.getInitialState());
        states = walkCloneAndUnbalance(alphabet.size(), index, states, stateMap);

        // connect new states
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState s : states) {
            epsilons.add(new WeightedStatePair(s, stateMap.get(s)));
        }
        uniform.setInitialState(initial);
        uniform.addEpsilons(epsilons);

        return uniform;
    }

    private static Set<WeightedState> walkCloneAndUnbalance(int factor, int index, Set<WeightedState> states, Map<WeightedState, WeightedState> stateMap) {
        for (int i = 0; i < index; i++) {
            Set<WeightedState> nextStates = new HashSet<>();
            for (WeightedState s : states) {
                WeightedState originalState = stateMap.get(s);
                for (WeightedTransition t : originalState.getTransitions()) {
                    WeightedState dest = new WeightedState();
                    stateMap.put(dest, t.getDest());
                    nextStates.add(dest);
                    int weight = t.getWeight();
                    if (i == (index - 1)) {
                        weight = weight * factor;
                    }
                    s.addTransition(new WeightedTransition(t.getMin(),
                                                           t.getMax(),
                                                           dest,
                                                           weight));
                }
            }
            states = nextStates;
        }
        return states;
    }

    public static WeightedAutomaton getUnbalancedNonUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton nonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, boundLength - 1);

        WeightedState initial = new WeightedState();
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedTransition t : nonUniform.getInitialState().getTransitions()) {
            epsilons.add(new WeightedStatePair(initial, t.getDest()));
        }
        nonUniform.setInitialState(initial);
        nonUniform.addEpsilons(epsilons);
        nonUniform.setInitialFactor(alphabet.size());
        return nonUniform;
    }

    public static WeightedAutomaton getUnbalancedNonUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength, int index) {
        WeightedAutomaton nonUniform = getNonUniformBoundedWeightedAutomaton(alphabet, boundLength - 1);

        Set<WeightedState> states = walkToIndex(index - 1, nonUniform);
        Set<WeightedState> nextStates = new HashSet<>();
        for (WeightedState s : states) {
            for (WeightedTransition t : s.getTransitions()) {
                t.setWeight(t.getWeight() * alphabet.size());
                nextStates.add(t.getDest());
            }
        }
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState s : nextStates) {
            for (WeightedTransition t : s.getTransitions()) {
                epsilons.add(new WeightedStatePair(s, t.getDest()));
            }
        }
        nonUniform.addEpsilons(epsilons);

        return nonUniform;
    }
}
