package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedStatePair;

import java.util.HashSet;
import java.util.Set;

public class WeightedAllSuffixes
        extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allSuffixes";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        WeightedAutomaton clone = automaton.clone();
        WeightedState initial = new WeightedState();
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState state : clone.getStates()) {
            epsilons.add(new WeightedStatePair(initial, state));
        }
        clone.setInitialState(initial);
        clone.addEpsilons(epsilons);
        clone.minimize();
        return clone;
    }
}
