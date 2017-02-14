package edu.boisestate.cs.automaton;

/**
 *
 */
public class StateWeight {
    private final WeightedState state;
    private final int weight;

    public WeightedState getState() {
        return state;
    }

    public int getWeight() {
        return weight;
    }

    public StateWeight(WeightedState state, int weight) {
        this.state = state;
        this.weight = weight;
    }
}
