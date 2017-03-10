package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedStatePair;

import java.util.HashSet;
import java.util.Set;

public class WeightedAllPrefixes
        extends UnaryWeightedOperation {
    @Override
    public String toString() {
        return "allPrefixes";
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        if (automaton.isEmpty()) {
            return BasicWeightedAutomata.makeEmpty();
        }
        WeightedAutomaton clone = automaton.clone();
        WeightedState accept = new WeightedState();
        accept.setAccept(true);
        Set<WeightedStatePair> epsilons = new HashSet<>();
        for (WeightedState state : clone.getStates()) {
            epsilons.add(new WeightedStatePair(state, accept));
        }
        clone.addEpsilons(epsilons);
        clone.minimize();
        return clone;
    }
}
