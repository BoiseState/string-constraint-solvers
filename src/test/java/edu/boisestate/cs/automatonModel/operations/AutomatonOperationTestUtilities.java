package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

class AutomatonOperationTestUtilities {

    static Automaton getConcreteAutomaton(Alphabet alphabet, String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return concrete;
    }

    static Automaton getNonUniformBoundAutomaton(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    static Automaton getNonUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return nonUniform;
    }

    static Automaton getUniformBoundedAutomaton(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    static Automaton getUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        uniform.minimize();
        return uniform;
    }
}
