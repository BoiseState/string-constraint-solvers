package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.PrecisePrefix;
import edu.boisestate.cs.automatonModel.operations.PreciseSuffix;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-B");

        Automaton bounding0 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0,0);
        Automaton bounding1 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(1,1);
        Automaton bounding2 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(2,2);
        Automaton bounding3 = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(3,3);

        Automaton m = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton m0 = m.intersection(bounding0);
        Automaton m1 = m.intersection(bounding1);
        Automaton m2 = m.intersection(bounding2);
        Automaton m3 = m.intersection(bounding3);

        PrecisePrefix prefix = new PrecisePrefix(1);
        PreciseSuffix suffix = new PreciseSuffix(2);

        Automaton prefix0 = prefix.op(m0);
        Automaton suffix0 = suffix.op(m0);
        Automaton prefix1 = prefix.op(m1);
        Automaton suffix1 = suffix.op(m1);
        Automaton prefix2 = prefix.op(m2);
        Automaton suffix2 = suffix.op(m2);
        Automaton prefix3 = prefix.op(m3);
        Automaton suffix3 = suffix.op(m3);

        Automaton delete0 = prefix0.concatenate(suffix0);
        Automaton delete1 = prefix1.concatenate(suffix1);
        Automaton delete2 = prefix2.concatenate(suffix2);
        Automaton delete3 = prefix3.concatenate(suffix3);

//        DotToGraph.outputDotFileAndPng(concat3.toDot());

        BigInteger mc0 = StringModelCounter.ModelCount(delete0);
        BigInteger mc1 = StringModelCounter.ModelCount(delete1);
        BigInteger mc2 = StringModelCounter.ModelCount(delete2);
        BigInteger mc3 = StringModelCounter.ModelCount(delete3);

        int mc = mc0.intValue() + mc1.intValue() + mc2.intValue() + mc3.intValue();
        Set<String> solutionSet = new HashSet<>();
        solutionSet.addAll(delete0.getFiniteStrings());
        solutionSet.addAll(delete1.getFiniteStrings());
        solutionSet.addAll(delete2.getFiniteStrings());
        solutionSet.addAll(delete3.getFiniteStrings());

        System.out.println("Model Count 0: " + mc0.intValue());
        System.out.println("Solution Set 0: " + delete0.getFiniteStrings());
        System.out.println("Model Count 1: " + mc1.intValue());
        System.out.println("Solution Set 1: " + delete1.getFiniteStrings());
        System.out.println("Model Count 2: " + mc2.intValue());
        System.out.println("Solution Set 2: " + delete2.getFiniteStrings());
        System.out.println("Model Count 3: " + mc3.intValue());
        System.out.println("Solution Set 3: " + delete3.getFiniteStrings());
        System.out.println("Model Count: " + mc);
        System.out.println("Solution Set: " + solutionSet);
    }
}
