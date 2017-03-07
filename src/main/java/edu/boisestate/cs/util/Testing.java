package edu.boisestate.cs.util;

import dk.brics.automaton.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.AutomatonModel;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.solvers.ModelCountSolver;

import java.math.BigInteger;
import java.util.*;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D");

        Automaton empty = BasicAutomata.makeEmpty();
        Automaton emptyString = BasicAutomata.makeEmptyString();
        Automaton concrete = BasicAutomata.makeString("ABC");

        Automaton anyChar = BasicAutomata.makeCharSet(alphabet.getCharSet());
        Automaton anyString = anyChar.repeat();
        Automaton uniform = anyChar.repeat();
        Automaton x = anyString.concatenate(BasicAutomata.makeChar('A'))
                               .concatenate(anyString);
        Automaton nonUniform = uniform.intersection(x);

        uniform.minimize();
        nonUniform.minimize();

        // create before automaton
        State q00 = new State();
        State q01 = new State();
        State q02 = new State();
//        State q03 = new State();
//        q01.setAccept(true);
        q02.setAccept(true);
//        q03.setAccept(true);
        q00.addTransition(new Transition('A', 'D', q01));
        q01.addTransition(new Transition('A', 'D', q02));
//        q02.addTransition(new Transition('A', 'D', q03));

        Automaton insert = new Automaton();
        insert.setInitialState(q00);

        Map<State, State> after = new HashMap<>();

        // creat first after automaton
        State q10 = new State();
        q10.setAccept(true);
        q10.addTransition(new Transition('A', 'D', q10));

        after.put(q02, q10);
        Automaton after1 = new Automaton();
        after1.setInitialState(q10);

        // creat first after automaton
//        State q20 = new State();
//        State q21 = new State();
//        State q22 = new State();
//        q20.setAccept(true);
//        q21.setAccept(true);
//        q22.setAccept(true);
//        q20.addTransition(new Transition('A', 'D', q21));
//        q21.addTransition(new Transition('A', 'D', q22));
//
//        after.put(q03, q20);
//        Automaton after2 = new Automaton();
//        after2.setInitialState(q20);

        List<String> dotGraphs = new ArrayList<>();
        dotGraphs.add(insert.toDot());
        dotGraphs.add(after1.toDot());

        Set<StatePair> epsilons = new HashSet<>();
        for (State state : insert.getAcceptStates()) {
            state.setAccept(false);
            Automaton clone = nonUniform.clone();
            epsilons.add(new StatePair(state, clone.getInitialState()));
            for(State cloneState : clone.getAcceptStates()) {
                cloneState.setAccept(false);
                epsilons.add(new StatePair(cloneState, after.get(state)));
            }
        }
        insert.addEpsilons(epsilons);

        dotGraphs.add(insert.toDot());
        BigInteger mcBefore = StringModelCounter.ModelCount(insert, 6);
        System.out.printf("Model Count Before Minimization: %d\n", mcBefore.intValue());

        insert.minimize();
        dotGraphs.add(insert.toDot());
        BigInteger mcAfter = StringModelCounter.ModelCount(insert, 6);
        System.out.printf("Model Count After Minimization: %d\n", mcAfter.intValue());

        for (int i = 0; i < dotGraphs.size(); i++) {
            DotToGraph.outputDotFileAndPng(dotGraphs.get(i), String.format("temp%02d", i));
        }
    }
}
