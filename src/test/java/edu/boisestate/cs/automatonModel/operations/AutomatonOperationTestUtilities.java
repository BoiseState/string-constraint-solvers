package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class AutomatonOperationTestUtilities {

    public static Automaton getConcreteAutomaton(Alphabet alphabet, String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return concrete;
    }

    public static Automaton getNonUniformBoundedAutomaton(Alphabet alphabet, int boundLength) {
        Automaton nonUniform = getNonUniformUnboundedAutomaton(alphabet);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    public static Automaton getNonUniformBoundedSubAutomaton(Alphabet alphabet, int length) {
        Automaton nonUniform = getNonUniformUnboundedAutomaton(alphabet);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(length, length);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    public static Automaton getNonUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = getUniformUnboundedAutomaton(alphabet);
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return nonUniform;
    }

    public static Automaton getUniformBoundedAutomaton(Alphabet alphabet, int boundLength) {
        Automaton uniform = getUniformUnboundedAutomaton(alphabet);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    public static Automaton getUniformBoundedSubAutomaton(Alphabet alphabet, int length) {
        Automaton uniform = getUniformUnboundedAutomaton(alphabet);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(length, length);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    public static Automaton getUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        uniform.minimize();
        return uniform;
    }
}
