/**
 * An extended EJSA operation for a more precise substring operation.
 */
package edu.boisestate.cs.automatonModel.operations.weighted;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.StatePair;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.PrecisePrefix;
import edu.boisestate.cs.automatonModel.operations.PreciseSuffix;

import java.math.BigInteger;
import java.util.*;

import static edu.boisestate.cs.automatonModel.operations.StringModelCounter
        .pseudoModelCount;

public class WeightedPreciseSubstring
        extends UnaryWeightedOperation {
    private int end;
    private int start;

    public WeightedPreciseSubstring(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public WeightedAutomaton op(WeightedAutomaton automaton) {
        return null;
    }

    @Override
    public String toString() {
        return "substring(" + start + ", " + end + ")";
    }

}
