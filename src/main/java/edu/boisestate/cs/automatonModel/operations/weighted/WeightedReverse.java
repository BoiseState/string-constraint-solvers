package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedStatePair;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeightedReverse extends UnaryWeightedOperation{
    @Override
    public String toString() {
        return "reverse";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        WeightedAutomaton clone = new WeightedAutomaton();
        Map<WeightedState, WeightedState> map = new HashMap<>();
        for (WeightedState s : automaton.getStates()) {
            WeightedState ss = new WeightedState();
            map.put(s, ss);
        }
        WeightedState initial = new WeightedState();
        clone.setInitialState(initial);
        map.get(automaton.getInitialState()).setAccept(true);
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState s : automaton.getStates()) {
            WeightedState ss = map.get(s);
            if (s.isAccept()) {
                epsilons.add(new WeightedStatePair(initial, ss));
            }
            for (WeightedTransition t : s.getTransitions()) {
                WeightedState pp = map.get(t.getDest());
                pp.addTransition(new WeightedTransition(t.getMin(), t.getMax(), ss, t.getWeightInt()));
            }
        }
        clone.setDeterministic(false);
        clone.addEpsilons(epsilons);
        return clone;
    }
}
