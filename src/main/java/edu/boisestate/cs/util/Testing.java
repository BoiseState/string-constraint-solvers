package edu.boisestate.cs.util;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedPreciseDelete;

import java.math.BigInteger;
import java.util.*;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;


        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar('A')).concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()).repeat(initialBoundLength, initialBoundLength);
        uniform = uniform.intersection(bounding);
        nonUniform = nonUniform.intersection(bounding);
        uniform.minimize();
        nonUniform.minimize();

        WeightedPreciseDelete operation = new WeightedPreciseDelete(0, 1);

        BigInteger mc = StringModelCounter.ModelCount(uniform);
        System.out.printf("Uniform MC Before: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(uniform.toDot(), "uniform-before");

        uniform = operation.op(uniform);

        mc = StringModelCounter.ModelCount(uniform);
        System.out.printf("Uniform MC After: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(uniform.toDot(), "uniform-after");

        mc = StringModelCounter.ModelCount(nonUniform);
        System.out.printf("Non-Uniform MC Before: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(nonUniform.toDot(), "nonUniform-before");

        nonUniform = operation.op(nonUniform);

        mc = StringModelCounter.ModelCount(nonUniform);
        System.out.printf("Non-Uniform MC After: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(nonUniform.toDot(), "nonUniform-after");
    }
}