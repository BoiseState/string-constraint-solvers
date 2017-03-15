/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.automaton.*;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class WeightedPreciseSuffix
        extends UnaryWeightedOperation {

    public WeightedPreciseSuffix(int start) {
        this.start = start;
    }

    private int start;

    @Override
    public WeightedAutomaton op(WeightedAutomaton a) {
        return null;
    }

    @Override
    public String toString() {
        return "suffix(" + start + ")";
    }

}
