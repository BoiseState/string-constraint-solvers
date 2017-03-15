package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAutomatonModelManager extends AutomatonModelManager {

    private final int boundLength;

    public WeightedAutomatonModelManager(Alphabet alphabet, int initialBoundLength) {
        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;

        // set automaton minimization as Brzozowski
        Automaton.setMinimization(1);
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new WeightedAutomatonModelManager(alphabet,
                                                     initialBoundLength);
    }

    @Override
    public AutomatonModel createString(String string) {
        // declare automata array
        WeightedAutomaton[] automata;

        // string is null
        if (string == null) {
            // automata array is empty
            automata = new WeightedAutomaton[0];
        }
        // empty string
        else if (string.equals("")) {
            // automata array contains single empty string automata
            automata = new WeightedAutomaton[]{BasicWeightedAutomata.makeEmptyString()};
        }
        // normal string value
        else {
            // automata array for concrete string contains single automaton
            automata = new WeightedAutomaton[]{BasicWeightedAutomata.makeString(string)};
        }

        // create automaton model from automata array
        return new WeightedAutomatonModel(automata, alphabet, boundLength);
    }

    @Override
    public AutomatonModel createAnyString(int initialBound) {
        return this.createAnyString(0, initialBound);
    }

    @Override
    public AutomatonModel createAnyString() {
        return createAnyString(0, boundLength);
    }

    @Override
    public AutomatonModel createAnyString(int min, int max) {
        // create automata array from bounding length size
        int size = max - min + 1;
        WeightedAutomaton[] automata = new WeightedAutomaton[size];

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        WeightedAutomaton anyChar = BasicWeightedAutomata.makeCharSet(charSet);

        // fill automata array with appropriately length automata
        for (int i = min; i <= max; i++) {
            WeightedAutomaton boundedAutomaton = anyChar.repeat(i, i);
            automata[i] = boundedAutomaton;
        }

        // return aggregate model from automata array
        return new WeightedAutomatonModel(automata, this.alphabet, max);
    }
}
