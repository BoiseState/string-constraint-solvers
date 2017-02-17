package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.Trim;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class Testing {

    public static void main(String[] args) {
        Automaton automaton = BasicAutomata.makeChar('a');
        Trim trim = new Trim();
        Automaton trimmedAutomaton = trim.op(automaton);

        DotToGraph.outputDotFileAndPng(trimmedAutomaton.toDot(), "temp");
    }
}
