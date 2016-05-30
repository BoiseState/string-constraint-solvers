package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.Postfix;
import edu.boisestate.cs.Alphabet;

public class UnboundedAutomatonModelManager
        extends AutomatonModelManager {

    private final int boundLength;

    private UnboundedAutomatonModelManager(Alphabet alphabet,
                                           int boundLength) {
        this.alphabet = alphabet;
        this.boundLength = boundLength;
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new UnboundedAutomatonModelManager(alphabet,
                                                      initialBoundLength);
    }

    @Override
    public AutomatonModel createAnyString(int max) {
        return this.createAnyString(0, max);
    }

    @Override
    public AutomatonModel allSuffixes(AutomatonModel model) {

        // get unbounded automaton from model
        Automaton automaton = getAutomatonFromModel(model);

        // use postfix operation
        Postfix postfix = new Postfix();
        Automaton postfixAutomaton = postfix.op(automaton);

        // return new automaton model from postfix automaton
        return new UnboundedAutomatonModel(postfixAutomaton,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    private static Automaton getAutomatonFromModel(AutomatonModel model) {
        return ((UnboundedAutomatonModel)model).getAutomaton();
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
        return new UnboundedAutomatonModel(boundedAutomaton,
                                           this.alphabet,
                                           this.boundLength);
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
                                           this.boundLength);
    }

    @Override
    public AutomatonModel createEmpty() {
        return null;
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
        return new UnboundedAutomatonModel(emptyString,
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
        return new UnboundedAutomatonModel(stringAutomaton,
                                           this.alphabet,
                                           this.boundLength);
    }
}
