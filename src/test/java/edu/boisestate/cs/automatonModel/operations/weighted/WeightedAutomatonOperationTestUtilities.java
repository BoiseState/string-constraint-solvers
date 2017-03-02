package edu.boisestate.cs.automatonModel.operations.weighted;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAutomatonOperationTestUtilities {

    public static WeightedAutomaton getConcreteWeightedAutomaton(Alphabet alphabet, String string) {
        WeightedAutomaton concrete = BasicWeightedAutomata.makeString(string);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return concrete;
    }

    public static WeightedAutomaton getNonUniformBoundedWeightedAutomaton(Alphabet alphabet,
                                                          int boundLength) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar('A')) .concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return nonUniform;
    }

    public static WeightedAutomaton getNonUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar('A')) .concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return nonUniform;
    }

    public static WeightedAutomaton getUniformBoundedWeightedAutomaton(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        WeightedAutomaton bounding = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return uniform;
    }

    public static WeightedAutomaton getUniformUnboundedWeightedAutomaton(Alphabet alphabet) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()) .repeat();
        uniform.minimize();
        return uniform;
    }
}
