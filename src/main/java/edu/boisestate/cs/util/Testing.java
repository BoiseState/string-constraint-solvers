package edu.boisestate.cs.util;

import dk.brics.automaton.*;
import dk.brics.string.stringoperations.Replace1;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;
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

        // constuct unbalanced uniform bounded automata
        WeightedAutomaton a0 = anyChar.repeat(0,2);
        a0.setNumEmptyStrings(4);

        WeightedState q0_1 = new WeightedState();
        WeightedState q1_1 = new WeightedState();
        WeightedState q2_1 = new WeightedState();
        q1_1.setAccept(true);
        q2_1.setAccept(true);
        q0_1.addTransition(new WeightedTransition('A', 'D', q1_1, 4));
        q1_1.addTransition(new WeightedTransition('A', 'D', q2_1, 1));
        WeightedAutomaton a1 = new WeightedAutomaton();
        a1.setInitialState(q0_1);

        WeightedState q0_2 = new WeightedState();
        WeightedState q1_2 = new WeightedState();
        WeightedState q2_2 = new WeightedState();
        q2_2.setAccept(true);
        q0_2.addTransition(new WeightedTransition('A', 'D', q1_2, 1));
        q1_2.addTransition(new WeightedTransition('A', 'D', q2_2, 4));
        WeightedAutomaton a2 = new WeightedAutomaton();
        a2.setInitialState(q0_2);

        WeightedState q0_3 = new WeightedState();
        WeightedState q1_3 = new WeightedState();
        WeightedState q2_3 = new WeightedState();
        WeightedState q3_3 = new WeightedState();
        q1_3.setAccept(true);
        q3_3.setAccept(true);
        q0_3.addTransition(new WeightedTransition('A', q1_3, 1));
        q0_3.addTransition(new WeightedTransition('A', q2_3, 1));
        q0_3.addTransition(new WeightedTransition('C', 'D', q2_3, 1));
        q1_3.addTransition(new WeightedTransition('A', q3_3, 2));
        q1_3.addTransition(new WeightedTransition('C', 'D', q3_3, 1));
        q2_3.addTransition(new WeightedTransition('A', q3_3, 1));
        WeightedAutomaton a3 = new WeightedAutomaton();
        a3.setInitialState(q0_3);
        a0.setNumEmptyStrings(4);

        // add automata to map
        Map<String, WeightedAutomaton> automata = new HashMap<>();
        automata.put("Empty", empty);
        automata.put("Empty String", emptyString);
        automata.put("Concrete", concrete);
        automata.put("Uniform Unbounded", uniformUnbounded);
        automata.put("Non-Uniform Unbounded", nonUniformUnbounded);
        automata.put("Uniform Bounded", uniformBounded);
        automata.put("Non-Uniform Bounded", nonUniformBounded);
        automata.put("Uniform Unbalanced 0 Bounded", a0);
        automata.put("Uniform Unbalanced 1 Bounded", a1);
        automata.put("Uniform Unbalanced 2 Bounded", a2);
        automata.put("Non-Uniform Unbalanced 0 Bounded", a3);

        System.out.println("Start Points");
        for (char c : nonUniformBounded.getStartPoints()) {
            System.out.println(c);
        }

//        for (String str : automata.keySet()) {
//            WeightedAutomaton a = automata.get(str);
//            String fixedName = str.replace(' ', '_').replace('-', '_').toLowerCase();
//            DotToGraph.outputDotFileAndPng(a.toDot(), fixedName);
//            for (int i = 0; i < boundLength; i++) {
//                for (int j = i; j < boundLength; j++) {
//                    PreciseWeightedDelete delete = new PreciseWeightedDelete(i, j);
//                    WeightedAutomaton result = delete.op(a);
//                    String fileName = fixedName + "_" + i + "_" + j;
//                    DotToGraph.outputDotFileAndPng(result.toDot(), fileName);
//                    int length = boundLength - (j - i);
//                    BigInteger mc = StringModelCounter.ModelCount(result, length);
//                    System.out.printf("<%s Automaton>.delete(%d, %d): MC = %d\n", str, i, j, mc.intValue());
//                }
//            }
//        }
    }
}
