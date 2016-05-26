package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class BoundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private final int boundLength;

    private BoundedAutomatonModelFactory(Alphabet alphabet,
                                         int initialBoundLength) {
        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance =
                new BoundedAutomatonModelFactory(alphabet, initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();

        // create bounded automaton
        Automaton boundedAutomaton = anyString.repeat(0, boundingLength);

        // return model from bounded automaton
        return new BoundedAutomatonModel(boundedAutomaton, this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel createAnyString() {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();

        // return model from automaton
        return new BoundedAutomatonModel(anyString,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel createEmptyString() {

        // create empty string automaton
        Automaton emptyString = BasicAutomata.makeEmptyString();

        // return model from automaton
        return new BoundedAutomatonModel(emptyString,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel createString(String string) {

        // create string automaton
        Automaton stringAutomaton = BasicAutomata.makeString(string);

        // return model from automaton
        return new BoundedAutomatonModel(stringAutomaton,
                                         this.alphabet,
                                         this.boundLength);
    }
}
