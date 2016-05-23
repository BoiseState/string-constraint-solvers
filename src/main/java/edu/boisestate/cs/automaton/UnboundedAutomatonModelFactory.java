package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class UnboundedAutomatonModelFactory
        extends AutomatonModelFactory {

    static void setInstance(Alphabet alphabet) {
        instance = new UnboundedAutomatonModelFactory(alphabet);
    }

    private UnboundedAutomatonModelFactory(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {
        throw new UnsupportedOperationException(
                "An UnboundedAutomatonModel can not have a bounding length");
    }

    @Override
    public AutomatonModel createAnyString() {

        // create any string automaton
        Automaton anyString = BasicAutomata.makeAnyString();

        // return model from automaton
        return new UnboundedAutomatonModel(anyString);
    }

    @Override
    public AutomatonModel createEmptyString() {

        // create empty string automaton
        Automaton emptyString = BasicAutomata.makeEmptyString();

        // return model from automaton
        return new UnboundedAutomatonModel(emptyString);
    }

    @Override
    public AutomatonModel createString(String string) {

        // create string automaton
        Automaton stringAutomaton = BasicAutomata.makeString(string);

        // return model from automaton
        return new UnboundedAutomatonModel(stringAutomaton);
    }
}
