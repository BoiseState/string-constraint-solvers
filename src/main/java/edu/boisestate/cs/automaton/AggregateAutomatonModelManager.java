package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import java.math.BigInteger;
import java.util.Set;

public class AggregateAutomatonModelManager
        extends AutomatonModelManager {

    private final int boundLength;

    private AggregateAutomatonModelManager(Alphabet alphabet,
                                           int initialBoundLength) {
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
    public AutomatonModel allPrefixes(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel allSubstrings(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel allSuffixes(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel createAnyString(int max) {
        return this.createAnyString(0, max);
    }

    @Override
    public AutomatonModel createAnyString(int min, int max) {

        // create automata array from bounding length size
        int size = max - min + 1;
        Automaton[] automata = new Automaton[size];

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();


        // fill automata array with appropriately length automata
        for (int i = min; i <= max; i++) {
            Automaton boundedAutomaton =
                    BasicAutomata.makeAnyString().repeat(i, i);
            automata[i] = boundedAutomaton;
        }

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          max);
    }

    @Override
    public AutomatonModel createAnyString() {
        throw new UnsupportedOperationException(
                "An AggregateAutomataModel must have a maximum bounding " +
                "length");
    }

    @Override
    public AutomatonModel createEmpty() {

        // create empty automata array
        Automaton[] automata = new Automaton[]{BasicAutomata.makeEmpty()};

        // return aggregate model from automata array
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          this.boundLength);
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
    public Set<String> getFiniteStrings(AutomatonModel model) {
        return null;
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

    @Override
    public AutomatonModel ignoreCase(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel prefix(AutomatonModel model, int end) {
        return null;
    }

    @Override
    public AutomatonModel replace(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  char find,
                                  char replace) {
        return null;
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  String find,
                                  String replace) {
        return null;
    }

    @Override
    public AutomatonModel replaceFindKnown(AutomatonModel model, char find) {
        return null;
    }

    @Override
    public AutomatonModel replaceReplaceKnown(AutomatonModel model,
                                              char replace) {
        return null;
    }

    @Override
    public AutomatonModel reverse(AutomatonModel baseModel) {
        return null;
    }

    @Override
    public AutomatonModel suffix(AutomatonModel model, int suffix) {
        return null;
    }

    @Override
    public AutomatonModel toLowercase(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel toUppercase(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel trim(AutomatonModel model) {
        return null;
    }

    @Override
    public BigInteger modelCount(AutomatonModel model) {
        return null;
    }
}
