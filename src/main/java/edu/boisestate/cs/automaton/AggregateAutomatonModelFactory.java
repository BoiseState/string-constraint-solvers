package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import static edu.boisestate.cs.automaton.AutomatonOperations.boundAutomaton;

public class AggregateAutomatonModelFactory
        extends AutomatonModelFactory {

    private final int boundLength;

    private AggregateAutomatonModelFactory(Alphabet alphabet,
                                           int initialBoundLength) {
        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new AggregateAutomatonModelFactory(alphabet,
                                                      initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {

        // create automata array from bounding length size
        Automaton[] automata = new Automaton[boundingLength + 1];

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();


        // fill automata array with appropriately length automata
        for (int i = 0; i <= boundingLength; i++) {
            Automaton boundedAutomaton = boundAutomaton(anyString, i);
            automata[i] = boundedAutomaton;
        }

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          boundingLength);
    }

    @Override
    public AutomatonModel createAnyString() {
        throw new UnsupportedOperationException(
                "An AggregateAutomataModel must have a maximum bounding " +
                "length");
    }

    @Override
    public AutomatonModel createEmptyString() {

        // create empty string automata array
        Automaton[] automata = new Automaton[]{BasicAutomata.makeEmptyString()};

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          this.boundLength);
    }

    @Override
    public AutomatonModel createString(String string) {

        // declare automata array
        Automaton[] automata;

        // string is null
        if (string == null) {

            // automata array is empty
            automata = new Automaton[0];

        }
        // empty string
        else if (string.equals("")) {

            // automata array contains single empty string automata
            automata = new Automaton[]{BasicAutomata.makeEmptyString()};
        }
        // normal string value
        else {

            // automata array for concrete string contains single automaton
            automata = new Automaton[]{BasicAutomata.makeString(string)};

        }

        // create automaton model from automata array
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          this.boundLength);
    }
}
