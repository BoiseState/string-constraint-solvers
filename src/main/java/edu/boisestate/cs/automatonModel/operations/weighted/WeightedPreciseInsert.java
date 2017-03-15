/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;


import edu.boisestate.cs.automaton.*;

import java.util.*;

@SuppressWarnings("Duplicates")
public class WeightedPreciseInsert
        extends BinaryWeightedOperation {
    private int offset;

    public WeightedPreciseInsert(int offset) {
        this.offset = offset;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton base, WeightedAutomaton arg) {
        return null;
    }

    @Override
    public String toString() {
        return "insert(" + offset + ", <String>)";
    }

}
