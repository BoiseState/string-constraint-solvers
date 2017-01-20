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

        Alphabet alphabet = new Alphabet("A-D");
        int initialBoundLength = 3;

        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0,initialBoundLength);
        Automaton known = BasicAutomata.makeString("AB");
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);

        // bound automata
//        known = known.intersection(bounding);
//        uniform = uniform.intersection(bounding);
//        nonUniform = nonUniform.intersection(bounding);
        known.determinize();
        known.minimize();
        uniform.determinize();
        uniform.minimize();
        nonUniform.determinize();
        nonUniform.minimize();

        PreciseDelete delete = new PreciseDelete(2,2);

        Automaton temp = delete.op(nonUniform);

        DotToGraph.outputDotFileAndPng(temp.toDot(), "temp1");
    }
}
