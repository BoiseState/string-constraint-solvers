package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

public class BoundedAutomatonModelManager
        extends AutomatonModelManager {

    private final int boundLength;

    private BoundedAutomatonModelManager(Alphabet alphabet,
                                         int initialBoundLength) {
        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance =
                new BoundedAutomatonModelManager(alphabet, initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int max) {
        return this.createAnyString(0, max);
    }

    @Override
    public AutomatonModel allSuffixes(AutomatonModel model) {
        return null;
    }

    @Override
    public AutomatonModel ignoreCase(AutomatonModel model) {
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
    public AutomatonModel replaceFindKnown(AutomatonModel model, char find) {
        return null;
    }

    @Override
    public AutomatonModel replaceReplaceKnown(AutomatonModel model,
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
    public AutomatonModel reverse(AutomatonModel baseModel) {
        return null;
    }

    @Override
    public AutomatonModel allPrefixes(AutomatonModel model) {
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
    public AutomatonModel createAnyString(int min, int max) {

        // create any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat();

        // create bounded automaton
        Automaton boundedAutomaton = anyString.repeat(min, max);

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
    public AutomatonModel createEmpty() {

        // create empty automaton
        Automaton empty = BasicAutomata.makeEmpty();

        // return model from automaton
        return new BoundedAutomatonModel(empty,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel allSubstrings(AutomatonModel model) {
        return null;
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
    public AutomatonModel prefix(AutomatonModel model, int end) {
        return null;
    }

    @Override
    public AutomatonModel suffix(AutomatonModel model, int suffix) {
        return null;
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
