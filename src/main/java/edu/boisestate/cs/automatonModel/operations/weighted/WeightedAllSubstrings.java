package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedStatePair;

import java.util.HashSet;
import java.util.Set;

public class WeightedAllSubstrings extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allSubstrings";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        if (automaton.isEmpty()) {
            return BasicWeightedAutomata.makeEmpty();
        }
        WeightedAutomaton clone = automaton.clone();
        WeightedState initial = new WeightedState();
        WeightedState accept = new WeightedState();
        accept.setAccept(true);
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState state : clone.getStates()) {
            epsilons.add(new WeightedStatePair(initial, state));
            epsilons.add(new WeightedStatePair(state, accept));
        }
        clone.setInitialState(initial);
        clone.addEpsilons(epsilons);
        clone.minimize();
        return clone;
    }
}
