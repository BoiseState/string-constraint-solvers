package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import java.util.Set;

public class UnboundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private final int initialBoundLength;

    private UnboundedAutomatonModelFactory(Alphabet alphabet,
                                           int initialBoundLength) {
        this.alphabet = alphabet;
        this.initialBoundLength = initialBoundLength;
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new UnboundedAutomatonModelFactory(alphabet,
                                                      initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {
        throw new UnsupportedOperationException(
                "An UnboundedAutomatonModel can not have a bounding length");
    }

    @Override
    public AutomatonModel createAnyString() {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();

        // return model from automaton
        return new UnboundedAutomatonModel(anyString,
                                           this.alphabet,
                                           this.initialBoundLength);
    }

    @Override
    public AutomatonModel createEmptyString() {

        // create empty string automaton
        Automaton emptyString = BasicAutomata.makeEmptyString();

        // return model from automaton
        return new UnboundedAutomatonModel(emptyString,
                                           this.alphabet,
                                           this.initialBoundLength);
    }

    @Override
    public AutomatonModel createString(String string) {

        // create string automaton
        Automaton stringAutomaton = BasicAutomata.makeString(string);

        // return model from automaton
        return new UnboundedAutomatonModel(stringAutomaton,
                                           this.alphabet,
                                           this.initialBoundLength);
    }
}
