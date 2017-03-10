package edu.boisestate.cs.util;

import dk.brics.automaton.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.weighted
        .PreciseWeightedDelete;
import edu.boisestate.cs.solvers.ModelCountSolver;

import java.math.BigInteger;
import java.util.*;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D");
        int boundLength = 3;

        WeightedAutomaton empty = BasicWeightedAutomata.makeEmpty();
        WeightedAutomaton emptyString = BasicWeightedAutomata.makeEmptyString();
        WeightedAutomaton concrete = BasicWeightedAutomata.makeString("ABC");
        WeightedAutomaton anyChar = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet());
        WeightedAutomaton anyString = anyChar.repeat();
        WeightedAutomaton bounding = anyChar.repeat(0, boundLength);
        WeightedAutomaton uniformUnbounded = anyChar.repeat();
        WeightedAutomaton uniformBounded = uniformUnbounded.intersection(bounding);
        WeightedAutomaton x = anyString.concatenate(BasicWeightedAutomata.makeChar('A')).concatenate(anyString);
        WeightedAutomaton nonUniformUnbounded = uniformUnbounded.intersection(x);
        WeightedAutomaton nonUniformBounded = uniformBounded.intersection(x);
        uniformUnbounded.minimize();
        nonUniformUnbounded.minimize();
        nonUniformBounded.minimize();
        nonUniformBounded.minimize();

        Map<String, WeightedAutomaton> automata = new HashMap<>();
        automata.put("Empty", empty);
        automata.put("Empty String", emptyString);
        automata.put("Concrete", concrete);
        automata.put("Uniform Unbounded", uniformUnbounded);
        automata.put("Non-Uniform Unbounded", nonUniformUnbounded);
        automata.put("Uniform Bounded", uniformBounded);
        automata.put("Non-Uniform Bounded", nonUniformBounded);

        for (String str : automata.keySet()) {
            WeightedAutomaton a = automata.get(str);
            for (int i = 0; i <= boundLength; i++) {
                for (int j = i; j <= boundLength; j++) {
                    PreciseWeightedDelete delete = new PreciseWeightedDelete(i, j);
                    WeightedAutomaton result = delete.op(a);
                    String fixedName = str.replace(' ', '_').replace('-', '_').toLowerCase();
                    String fileName = fixedName + "_" + i + "_" + j;
                    DotToGraph.outputDotFileAndPng(result.toDot(), fileName);
                    int length = boundLength - (j - i);
                    BigInteger mc = StringModelCounter.ModelCount(result, length);
                    System.out.printf("<%s Automaton>.delete(%d, %d): MC = %d\n", str, i, j, mc.intValue());
                }
            }
        }
    }
}
