package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D");

        Automaton m1 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();

        Automaton concat1 = m1.concatenate(BasicAutomata.makeChar('A'));

        Automaton concat2 = concat1.clone();

        Automaton concat3 = concat1.concatenate(concat2);

        concat3.determinize();

        concat3.minimize();

        DotToGraph.outputDotFileAndPng(concat3.toDot());
    }
}
