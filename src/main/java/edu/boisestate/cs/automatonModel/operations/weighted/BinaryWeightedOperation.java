package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.automaton.WeightedAutomaton;

abstract public class BinaryWeightedOperation extends WeightedOperation {

    /**
     * Binary operation on automata.
     *
     * @param baseAutomaton first input automaton, should not be modified
     * @param argAutomaton second input automaton, should not be modified
     * @return output automaton
     */
    abstract public WeightedAutomaton op(WeightedAutomaton baseAutomaton,
                                         WeightedAutomaton argAutomaton);
}
