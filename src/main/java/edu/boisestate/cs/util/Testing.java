package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class Testing {

    public static void main(String[] args) {

        Automaton.setMinimizeAlways(true);
        Alphabet alphabet = new Alphabet("A-C");

        Automaton automaton = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton arg = BasicAutomata.makeChar('A');

        automaton = automaton.concatenate(arg);

        DotToGraph.outputDotFileAndPng(automaton.toDot(), "concat");

        PreciseDelete delete = new PreciseDelete(0, 2);
        automaton = delete.op(automaton);

        DotToGraph.outputDotFileAndPng(automaton.toDot(), "delete");

        BigInteger mc = StringModelCounter.ModelCount(automaton, 1);
        System.out.println("Model Count: " + mc.intValue());
    }
}
