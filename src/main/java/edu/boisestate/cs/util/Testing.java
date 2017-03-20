package edu.boisestate.cs.util;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;

import java.math.BigInteger;
import java.util.*;

public class Testing {

    public static void main(String[] args) {

        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();
        WeightedState q3 = new WeightedState();

        // set accept state
        q3.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('C', q1, 1));
        q0.addTransition(new WeightedTransition('D', q1, 1));
        q0.addTransition(new WeightedTransition('A', q2, 1));
        q0.addTransition(new WeightedTransition('A', q2, 1));
        q0.addTransition(new WeightedTransition('A', q2, 1));
        q0.addTransition(new WeightedTransition('C', q2, 1));
        q0.addTransition(new WeightedTransition('C', q2, 1));
        q0.addTransition(new WeightedTransition('C', q2, 1));
        q0.addTransition(new WeightedTransition('D', q2, 1));
        q0.addTransition(new WeightedTransition('D', q2, 1));
        q0.addTransition(new WeightedTransition('D', q2, 1));
        q1.addTransition(new WeightedTransition('A', q3, 1));
        q1.addTransition(new WeightedTransition('A', q3, 1));
        q1.addTransition(new WeightedTransition('C', q3, 1));
        q1.addTransition(new WeightedTransition('D', q3, 1));
        q2.addTransition(new WeightedTransition('A', q3, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);
        automaton.setDeterministic(false);

        char [] points = automaton.getStartPoints();
        System.out.print("Start Points: ");
        System.out.printf("'%c'", points[0]);
        for (int i = 1; i < points.length; i++) {
            System.out.printf(", '%c'", points[i]);
        }
        System.out.print("\n");

        BigInteger mc = StringModelCounter.ModelCount(automaton);
        System.out.printf("Model Count Before: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(automaton.toDot(), "temp-before");

        BasicWeightedOperations.determinize(automaton);

        mc = StringModelCounter.ModelCount(automaton);
        System.out.printf("Model Count After: %d\n", mc.intValue());
        DotToGraph.outputDotFileAndPng(automaton.toDot(), "temp-after");
    }
}
