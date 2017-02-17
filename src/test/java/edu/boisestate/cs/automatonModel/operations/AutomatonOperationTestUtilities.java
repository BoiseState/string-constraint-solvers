package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class AutomatonOperationTestUtilities {

    public static Automaton getConcreteAutomaton(Alphabet alphabet,
                                                 String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.determinize();
        concrete.minimize();
        return concrete;
    }

    public static Automaton getNonUniformUnboundAutomaton(Alphabet alphabet,
                                                          int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.determinize();
        nonUniform.minimize();
        return nonUniform;
    }

    public static Automaton getNonUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A'))
                                     .concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.determinize();
        nonUniform.minimize();
        return nonUniform;
    }

    public static Automaton getUniformBoundedAutomaton(Alphabet alphabet,
                                                       int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                          .repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.determinize();
        uniform.minimize();
        return uniform;
    }

    public static Automaton getUniformUnboundedAutomaton(Alphabet alphabet) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet())
                                         .repeat();
        uniform.determinize();
        uniform.minimize();
        return uniform;
    }
}
