package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

import java.util.HashSet;
import java.util.Set;

public class AutomatonTestUtilities {
    public static AggregateAutomataModel getConcreteAggregateModel(Alphabet alphabet, String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        concrete.minimize();
        Automaton chars = BasicAutomata.makeCharSet(alphabet.getCharSet());
        Automaton[] concreteAutomata = new Automaton[string.length() + 1];
        for (int i = 0; i <= string.length(); i++) {
            concreteAutomata[i] = concrete.intersection(chars.repeat(i, i));
        }
        return new AggregateAutomataModel(concreteAutomata, alphabet, string.length());
    }

    public static BoundedAutomatonModel getConcreteBoundedModel(Alphabet alphabet, String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, string.length());
        concrete = concrete.intersection(bounding);
        concrete.minimize();
        return new BoundedAutomatonModel(concrete, alphabet, string.length());
    }

    public static UnboundedAutomatonModel getConcreteUnboundedModel(Alphabet alphabet, String string) {
        Automaton concrete = BasicAutomata.makeString(string);
        concrete.minimize();
        return new UnboundedAutomatonModel(concrete, alphabet, string.length());
    }

    public static WeightedAutomatonModel getConcreteWeightedModel(Alphabet alphabet, String string) {
        WeightedAutomaton concrete = BasicWeightedAutomata.makeString(string);
        concrete.minimize();
        WeightedAutomaton chars = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet());
        WeightedAutomaton[] concreteAutomata = new WeightedAutomaton[string.length() + 1];
        for (int i = 0; i <= string.length(); i++) {
            concreteAutomata[i] = concrete.intersection(chars.repeat(i, i));
        }
        return new WeightedAutomatonModel(concreteAutomata, alphabet, string.length());
    }

    public static AggregateAutomataModel getEmptyAggregateModel(Alphabet alphabet) {
        Automaton[] emptyAutomata = new Automaton[]{BasicAutomata.makeEmpty()};
        return new AggregateAutomataModel(emptyAutomata, alphabet, 0);
    }

    public static BoundedAutomatonModel getEmptyBoundedModel(Alphabet alphabet) {
        Automaton empty = BasicAutomata.makeEmpty();
        return new BoundedAutomatonModel(empty, alphabet, 0);
    }

    public static AggregateAutomataModel getEmptyStringAggregateModel(Alphabet alphabet) {
        Automaton[] emptyStringAutomata = new Automaton[]{BasicAutomata.makeEmptyString()};
        return new AggregateAutomataModel(emptyStringAutomata, alphabet, 0);
    }

    public static BoundedAutomatonModel getEmptyStringBoundedModel(Alphabet alphabet) {
        Automaton emptyString = BasicAutomata.makeEmptyString();
        return new BoundedAutomatonModel(emptyString, alphabet, 0);
    }

    public static UnboundedAutomatonModel getEmptyStringUnboundedModel(Alphabet alphabet) {
        Automaton emptyString = BasicAutomata.makeEmptyString();
        return new UnboundedAutomatonModel(emptyString, alphabet, 0);
    }

    public static WeightedAutomatonModel getEmptyStringWeightedModel(Alphabet alphabet) {
        WeightedAutomaton[] emptyStringAutomata = new WeightedAutomaton[]{BasicWeightedAutomata.makeEmptyString()};
        return new WeightedAutomatonModel(emptyStringAutomata, alphabet, 0);
    }

    public static UnboundedAutomatonModel getEmptyUnboundedModel(Alphabet alphabet) {
        Automaton empty = BasicAutomata.makeEmpty();
        return new UnboundedAutomatonModel(empty, alphabet, 0);
    }

    public static WeightedAutomatonModel getEmptyWeightedModel(Alphabet alphabet) {
        WeightedAutomaton[] emptyStringAutomata = new WeightedAutomaton[]{BasicWeightedAutomata.makeEmpty()};
        return new WeightedAutomatonModel(emptyStringAutomata, alphabet, 0);
    }

    public static AggregateAutomataModel getNonUniformAggregateModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        Automaton chars = BasicAutomata.makeCharSet(alphabet.getCharSet());
        Automaton[] nonUniformAutomata = new Automaton[boundLength + 1];
        for (int i = 0; i <= boundLength; i++) {
            nonUniformAutomata[i] = nonUniform.intersection(chars.repeat(i, i));
            nonUniformAutomata[i].determinize();
        }
        return new AggregateAutomataModel(nonUniformAutomata, alphabet, boundLength);
    }

    public static BoundedAutomatonModel getNonUniformBoundedModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        nonUniform = nonUniform.intersection(bounding);
        nonUniform.minimize();
        return new BoundedAutomatonModel(nonUniform, alphabet, boundLength);
    }

    public static UnboundedAutomatonModel getNonUniformUnboundedModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton intersect = uniform.concatenate(BasicAutomata.makeChar('A')).concatenate(uniform);
        Automaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        return new UnboundedAutomatonModel(nonUniform, alphabet, boundLength);
    }

    public static WeightedAutomatonModel getNonUniformWeightedModel(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        WeightedAutomaton intersect = uniform.concatenate(BasicWeightedAutomata.makeChar('A')).concatenate(uniform);
        WeightedAutomaton nonUniform = uniform.intersection(intersect);
        nonUniform.minimize();
        WeightedAutomaton chars = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet());
        WeightedAutomaton[] nonUniformAutomata = new WeightedAutomaton[boundLength + 1];
        for (int i = 0; i <= boundLength; i++) {
            nonUniformAutomata[i] = nonUniform.intersection(chars.repeat(i, i));
            nonUniformAutomata[i].determinize();
        }
        return new WeightedAutomatonModel(nonUniformAutomata, alphabet, boundLength);
    }

    public static Set<String> getStrings(Automaton automaton, int minLength, int maxLength) {
        // initialize return set
        Set<String> stringSet = new HashSet<>();
        // add all strings between lengths to set
        for (int i = minLength; i <= maxLength; i++) {
            stringSet.addAll(automaton.getStrings(i));
        }
        // return string set
        return stringSet;
    }

    public static AggregateAutomataModel getUniformAggregateModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        uniform.minimize();
        Automaton chars = BasicAutomata.makeCharSet(alphabet.getCharSet());
        Automaton[] uniformAutomata = new Automaton[boundLength + 1];
        for (int i = 0; i <= boundLength; i++) {
            uniformAutomata[i] = uniform.intersection(chars.repeat(i, i));
            uniformAutomata[i].determinize();
        }
        return new AggregateAutomataModel(uniformAutomata, alphabet, boundLength);
    }

    public static BoundedAutomatonModel getUniformBoundedModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, boundLength);
        uniform = uniform.intersection(bounding);
        uniform.minimize();
        return new BoundedAutomatonModel(uniform, alphabet, boundLength);
    }

    public static UnboundedAutomatonModel getUniformUnboundedModel(Alphabet alphabet, int boundLength) {
        Automaton uniform = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        uniform.minimize();
        return new UnboundedAutomatonModel(uniform, alphabet, boundLength);
    }

    public static WeightedAutomatonModel getUniformWeightedModel(Alphabet alphabet, int boundLength) {
        WeightedAutomaton uniform = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet()).repeat();
        uniform.minimize();
        WeightedAutomaton chars = BasicWeightedAutomata.makeCharSet(alphabet.getCharSet());
        WeightedAutomaton[] uniformAutomata = new WeightedAutomaton[boundLength + 1];
        for (int i = 0; i <= boundLength; i++) {
            uniformAutomata[i] = uniform.intersection(chars.repeat(i, i));
            uniformAutomata[i].determinize();
        }
        return new WeightedAutomatonModel(uniformAutomata, alphabet, boundLength);
    }
}
