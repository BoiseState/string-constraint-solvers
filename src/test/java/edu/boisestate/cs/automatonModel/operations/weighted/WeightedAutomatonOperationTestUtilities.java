package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeightedAutomatonOperationTestUtilities {

    public static WeightedAutomaton getConcreteWeightedAutomaton(Alphabet alphabet, String string) {
        WeightedAutomaton concrete = BasicWeightedAutomata.makeString(string);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return concrete;
    }

    public static WeightedAutomaton getNonUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton nonUniform = getNonUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    private static WeightedAutomaton getNonUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        char c = alphabet.getCharSet().charAt(0);
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar(c)) .concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return nonUniform;
    }

    public static WeightedAutomaton getUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = getUniformUnboundedWeightedAutomaton(alphabet);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    private static WeightedAutomaton getUniformUnboundedWeightedAutomaton(Alphabet
                                                                          alphabet) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        uniform.minimize();
        return uniform;
    }

    public static WeightedAutomaton getWeightedAutomaton(Type type,
                                                         Bounding bounding,
                                                         Balance balance,
                                                         int length,
                                                         int index) {
        return null;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_NonUniform_WeightedAutomaton_0() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();
        WeightedState q3 = new WeightedState();

        // set accept state
        q3.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('B', 'D', q2, 1));
        q1.addTransition(new WeightedTransition('A', 'D', q3, 1));
        q2.addTransition(new WeightedTransition('A', q3, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);
        automaton.setInitialFactor(4);

        // return automaton
        return automaton;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_NonUniform_WeightedAutomaton_1() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();
        WeightedState q3 = new WeightedState();

        // set accept state
        q3.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', q1, 4));
        q0.addTransition(new WeightedTransition('B', 'D', q2, 4));
        q1.addTransition(new WeightedTransition('A', 'D', q3, 1));
        q2.addTransition(new WeightedTransition('A', q3, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);

        // return automaton
        return automaton;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_NonUniform_WeightedAutomaton_2() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();
        WeightedState q3 = new WeightedState();

        // set accept state
        q3.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('B', 'D', q2, 1));
        q1.addTransition(new WeightedTransition('A', 'D', q3, 4));
        q2.addTransition(new WeightedTransition('A', q3, 4));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);

        // return automaton
        return automaton;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_Uniform_WeightedAutomaton_0() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();

        // set accept state
        q2.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', 'D', q1, 1));
        q1.addTransition(new WeightedTransition('A', 'D', q2, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);
        automaton.setInitialFactor(4);

        // return automaton
        return automaton;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_Uniform_WeightedAutomaton_1() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();

        // set accept state
        q2.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', 'D', q1, 4));
        q1.addTransition(new WeightedTransition('A', 'D', q2, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);

        // return automaton
        return automaton;
    }

    @SuppressWarnings("Duplicates")
    public static WeightedAutomaton unbalanced_Uniform_WeightedAutomaton_2() {
        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();

        // set accept state
        q2.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', 'D', q1, 1));
        q1.addTransition(new WeightedTransition('A', 'D', q2, 4));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);

        // return automaton
        return automaton;
    }

    public enum Balance {
        BALANCED,
        UNBALANCED
    }

    public enum Bounding {
        BOUNDED,
        UNBOUNDED
    }

    public enum Type {
        EMPTY,
        EMPTY_STRING,
        CONCRETE,
        UNIFORM,
        NON_UNIFORM
    }
}
