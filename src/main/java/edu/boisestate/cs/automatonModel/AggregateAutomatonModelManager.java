package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class AggregateAutomatonModelManager
        extends AutomatonModelManager {

    private final int boundLength;

    AggregateAutomatonModelManager(Alphabet alphabet, int initialBoundLength) {
        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;

        // set automaton minimization as huffman
        Automaton.setMinimization(0);
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new AggregateAutomatonModelManager(alphabet,
                                                      initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int initialBound) {
        return this.createAnyString(0, initialBound);
    }

    @Override
    public AutomatonModel createAnyString(int min, int max) {
        // create automata array from bounding length size
        int size = max - min + 1;
        Automaton[] automata = new Automaton[size];

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyChar = BasicAutomata.makeCharSet(charSet);

        // fill automata array with appropriately length automata
        for (int i = min; i <= max; i++) {
            Automaton boundedAutomaton = anyChar.repeat(i, i);
            automata[i] = boundedAutomaton;
        }

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata, this.alphabet, max);
    }

    @Override
    public AutomatonModel createAnyString() {
        return createAnyString(0, boundLength);
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
