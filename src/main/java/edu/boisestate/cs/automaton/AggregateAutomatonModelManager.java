package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.operations.*;

import java.math.BigInteger;
import java.util.HashSet;
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

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Prefix());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    private Automaton[] performUnaryOperations(Automaton[] automata,
                                               UnaryOperation operation) {

        // create automata to bound results to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton alphabet = BasicAutomata.makeCharSet(charSet).repeat();

        // initialize results array
        Automaton[] results = new Automaton[automata.length];

        //  for each index in the automata array
        for (int i = 0; i < automata.length; i++) {

            // perform operation
            Automaton result = operation.op(automata[i]);

            // bound result
            result = result.intersection(alphabet);

            // set appropriate index in results array
            results[i] = result;
        }

        // return results array
        return results;
    }

    @Override
    public AutomatonModel allSubstrings(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Substring());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel allSuffixes(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Postfix());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
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
        return new AggregateAutomataModel(automata,
                                          this.alphabet,
                                          max);
    }

    @Override
    public AutomatonModel createAnyString() {
        return this.createAnyString(0, this.boundLength);
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
                                          0);
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
    public Set<String> getFiniteStrings(AutomatonModel model) {

        // initialize set of strings
        Set<String> strings = new HashSet<>();

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // for each automaton
        for (Automaton automaton : automata) {

            // get finite strings from automaton
            Set<String> automatonStrings = automaton.getFiniteStrings();

            // if set is not null, add automaton strings to strings set
            if (automatonStrings != null) {
                strings.addAll(automatonStrings);
            }
        }

        // return string set
        return strings;
    }

    @Override
    public AutomatonModel ignoreCase(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new IgnoreCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public BigInteger modelCount(AutomatonModel model) {

        // initialize total model count as big integer
        BigInteger totalModelCount = new BigInteger("0");

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // for each automaton in automata array
        for (Automaton automaton : automata) {

            // get automaton model count
            BigInteger modelCount = StringModelCounter.ModelCount(automaton);

            // add automaton model count to total model count
            totalModelCount = totalModelCount.add(modelCount);
        }

        // return final model count
        return totalModelCount;
    }

    @Override
    public AutomatonModel delete(AutomatonModel model, int start, int end) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new PreciseDelete(start, end));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel prefix(AutomatonModel model, int end) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new PrecisePrefix(end));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel replaceChar(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace4());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  char find,
                                  char replace) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata,
                                            new Replace1(find, replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  String find,
                                  String replace) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata,
                                            new Replace6(find, replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel replaceFindKnown(AutomatonModel model, char find) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace2(find));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel replaceReplaceKnown(AutomatonModel model,
                                              char replace) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Replace3(replace));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel reverse(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Reverse());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel suffix(AutomatonModel model, int start) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new PreciseSuffix(start));

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel toLowercase(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new ToLowerCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel toUppercase(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new ToUpperCase());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }

    @Override
    public AutomatonModel trim(AutomatonModel model) {

        // get automata from model
        Automaton[] automata = ((AggregateAutomataModel) model).getAutomata();

        // perform operations
        Automaton[] results =
                this.performUnaryOperations(automata, new Substring());

        // return new model from resulting automata
        return new AggregateAutomataModel(results,
                                          this.alphabet,
                                          model.getBoundLength());
    }
}
