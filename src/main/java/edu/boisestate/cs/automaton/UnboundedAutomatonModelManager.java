package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.operations.IgnoreCase;
import edu.boisestate.cs.automaton.operations.PrecisePrefix;
import edu.boisestate.cs.automaton.operations.PreciseSuffix;
import edu.boisestate.cs.automaton.operations.StringModelCounter;

import java.math.BigInteger;
import java.util.Set;

public class UnboundedAutomatonModelManager
        extends AutomatonModelManager {

    private final int boundLength;

    private UnboundedAutomatonModelManager(Alphabet alphabet,
                                           int boundLength) {
        this.alphabet = alphabet;
        this.boundLength = boundLength;

        // set automaton minimization as huffman
        Automaton.setMinimization(0);
    }

    static void setInstance(Alphabet alphabet, int initialBoundLength) {
        instance = new UnboundedAutomatonModelManager(alphabet,
                                                      initialBoundLength);
    }

    @Override
    public AutomatonModel allPrefixes(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Prefix());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel allSubstrings(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Substring());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel allSuffixes(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Postfix());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel createAnyString(int max) {
        return this.createAnyString(0, max);
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

        // create empty automaton
        Automaton empty = BasicAutomata.makeEmpty();

        // create new model from automaton
        return new UnboundedAutomatonModel(empty,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public Set<String> getFiniteStrings(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // get bounding automaton from current bound length
        int length = model.getBoundLength();
        Automaton bounding = BasicAutomata.makeAnyChar().repeat(0, length);

        // get bounded automaton
        Automaton bounded = automaton.intersection(bounding);

        // return finite strings from bounded automaton
        return bounded.getFiniteStrings();
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

    @Override
    public AutomatonModel ignoreCase(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new IgnoreCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel prefix(AutomatonModel model, int end) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new PrecisePrefix(end));

        // determine new bound length
        int boundLength = end;
        if (model.getBoundLength() < boundLength) {
            boundLength = model.getBoundLength();
        }

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel replaceChar(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace4());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  char find,
                                  char replace) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton,
                                                      new Replace1(find,
                                                                   replace));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel replace(AutomatonModel model,
                                  String find,
                                  String replace) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton,
                                                      new Replace6(find,
                                                                   replace));

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int boundLength = model.getBoundLength() - boundDiff;

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel replaceFindKnown(AutomatonModel model, char find) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace2(find));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel replaceReplaceKnown(AutomatonModel model,
                                              char replace) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace3(replace));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel reverse(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Reverse());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel suffix(AutomatonModel model, int start) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new PreciseSuffix(start));

        // determine new bound length
        int boundLength = model.getBoundLength() - start;

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel toLowercase(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new ToLowerCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel toUppercase(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new ToUpperCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public AutomatonModel trim(AutomatonModel model) {

        // workaround for trim bug
        AutomatonModel hasLength = this.assertHasLength(model, 1, 1);
        AutomatonModel temp = model.intersect(hasLength);

        if (temp.equals(model)) {

            // return union of temp and empty string
            return temp.union(this.createEmptyString());

        }

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // return automaton model from trim operation
        Automaton result = this.performUnaryOperation(automaton, new Trim());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           model.getBoundLength());
    }

    @Override
    public BigInteger modelCount(AutomatonModel model) {

        // get automaton from model
        Automaton automaton = ((UnboundedAutomatonModel) model).getAutomaton();

        // get bound length from model
        int length = model.getBoundLength();

        // return model count of automaton
        return StringModelCounter.ModelCount(automaton, length);
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
}
