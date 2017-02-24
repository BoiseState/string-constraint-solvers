package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;

public class WeightedAutomatonModelManager extends AutomatonModelManager {

    private final int boundLength;

    public WeightedAutomatonModelManager(Alphabet alphabet, int boundLength) {
        this.alphabet = alphabet;
        this.boundLength = boundLength;

        // set automaton minimization as huffman
        Automaton.setMinimization(0);
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new WeightedAutomatonModelManager(alphabet,
                                                     initialBoundLength);
    }

    @Override
    public AutomatonModel createString(String string) {

        // create string automaton
        WeightedAutomaton stringAutomaton = BasicWeightedAutomata.makeString(string);

        // return model from automaton
        return new WeightedAutomatonModel(stringAutomaton,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel createAnyString(int initialBound) {
        return this.createAnyString(0, initialBound);
    }

    @Override
    public AutomatonModel createAnyString() {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        WeightedAutomaton anyString = BasicWeightedAutomata.makeCharSet(charSet)
                                                           .repeat();

        // return model from automaton
        return new WeightedAutomatonModel(anyString, this.alphabet);
    }

    @Override
    public AutomatonModel createAnyString(int min, int max) {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        WeightedAutomaton anyChar = BasicWeightedAutomata.makeCharSet(charSet);

        // create bounded automaton
        WeightedAutomaton boundedAutomaton = anyChar.repeat(min, max);

        // return model from bounded automaton
        return new WeightedAutomatonModel(boundedAutomaton,
                                          this.alphabet,
                                          this.boundLength);
    }
}
