package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import static edu.boisestate.cs.automaton.AutomatonOperations.boundAutomaton;

public class AggregateAutomatonModelFactory
        extends AutomatonModelFactory {

    static void setInstance(Alphabet alphabet) {
        instance = new AggregateAutomatonModelFactory(alphabet);
    }

    private AggregateAutomatonModelFactory(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {

        // create automata array from bounding length size
        Automaton[] automata = new Automaton[boundingLength];

        // fill automata array with appropriately length automata
        Automaton anyAutomaton = BasicAutomata.makeAnyString();
        for (int i = 0; i < boundingLength; i++) {
            Automaton boundedAutomaton = boundAutomaton(anyAutomaton, i);
            automata[i] = boundedAutomaton;
        }

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata);
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
        return new AggregateAutomataModel(automata);
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
        return new AggregateAutomataModel(automata);
    }
}
