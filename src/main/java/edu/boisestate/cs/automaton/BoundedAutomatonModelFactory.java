package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class BoundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private BoundedAutomatonModelFactory(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    static void setInstance(Alphabet alphabet) {
        instance = new BoundedAutomatonModelFactory(alphabet);
    }

    @Override
    public AutomatonModel createString(String string) {

        // create string automaton
        Automaton stringAutomaton = BasicAutomata.makeString(string);

        // return model from automaton
        return new UnboundedAutomatonModel(stringAutomaton);
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {

        // create any string automaton
        Automaton anyString = BasicAutomata.makeAnyString();

        // create bounded automaton
        Automaton boundedAutomaton = anyString.repeat(0, boundingLength);

        // return model from bounded automaton
        return new UnboundedAutomatonModel(boundedAutomaton);
    }

    @Override
    public AutomatonModel createAnyString() {
        throw new UnsupportedOperationException(
                "An BoundedAutomatonModel must have a bounding length");
    }

    @Override
    public AutomatonModel createEmptyString() {

        // create empty string automaton
        Automaton emptyString = BasicAutomata.makeEmptyString();

        // return model from automaton
        return new UnboundedAutomatonModel(emptyString);
    }
}
