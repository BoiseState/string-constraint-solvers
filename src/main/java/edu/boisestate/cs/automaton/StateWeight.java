package edu.boisestate.cs.automaton;

/**
 *
 */
public class StateWeight implements Comparable<StateWeight> {
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

    @Override
    public int compareTo(StateWeight o) {
        int stateDiff = state.compareTo(o.getState());
        if (stateDiff != 0) {
            return stateDiff;
        }

        return weight - o.getWeight();
    }
}
