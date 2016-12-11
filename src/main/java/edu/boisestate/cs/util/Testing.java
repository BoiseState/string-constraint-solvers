package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.operations.StringModelCounter;

import java.math.BigInteger;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D");

        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0,2);

        Automaton m1 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
//        m1 = m1.intersection(bounding);

        Automaton concat1 = m1.concatenate(BasicAutomata.makeChar('A'));

        concat1.determinize();

        concat1.minimize();

        Automaton concat2 = concat1.clone();

        Automaton concat3 = concat1.concatenate(concat2);

        concat3.determinize();

        concat3.minimize();

//        DotToGraph.outputDotFileAndPng(concat3.toDot());

        BigInteger mc = StringModelCounter.ModelCount(concat3, 6);
        System.out.println(mc.toString());
    }
}
