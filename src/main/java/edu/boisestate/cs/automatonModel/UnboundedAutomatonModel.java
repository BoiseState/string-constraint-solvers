package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.Set;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class UnboundedAutomatonModel
        extends AutomatonModel {

    private Automaton automaton;

    private void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    UnboundedAutomatonModel(Automaton automaton,
                            Alphabet alphabet,
                            int initialBoundLength) {
        super(alphabet, initialBoundLength);

        // set automaton from parameter
        this.automaton = automaton;

        this.modelManager = new UnboundedAutomatonModelManager(alphabet,
                                                               initialBoundLength);
    }

    @Override
    public AutomatonModel allPrefixes() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = BasicAutomata.makeEmpty();
        if (!automaton.isEmpty()) {
            result = this.performUnaryOperation(automaton, new Prefix());
        }

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    Automaton getAutomaton() {
        return this.automaton;
    }

    @Override
    public AutomatonModel allSubstrings() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = BasicAutomata.makeEmpty();
        if (!automaton.isEmpty()) {
            result = this.performUnaryOperation(automaton, new Substring());
        }

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel allSuffixes() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Postfix());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel complement() {

        // get complement of automaton
        Automaton complement = this.automaton.complement();

        // get any string automaton from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet).repeat();

        // get intersection of complement and alphabet any string
        Automaton automaton = complement.intersection(anyString);

        // return new model from complement automaton
        return new UnboundedAutomatonModel(automaton,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel concatenateIndividual(AutomatonModel arg) {
        return concatenate(arg);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel arg) {

        // check if automaton model is unbounded
        if (!(arg instanceof UnboundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an UnboundedAutomatonModel can be concatenated to " +
                    "another UnboundedAutomatonModel.");
        }

        // cast arg model
        UnboundedAutomatonModel argModel = (UnboundedAutomatonModel) arg;

        // get concatenation of automata
        Automaton result = this.automaton.concatenate(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength + argModel.boundLength;

        // return unbounded model from automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public boolean containsString(String actualValue) {
        return this.automaton.run(actualValue);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel delete(int start, int end) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
      //System.out.println("start " + start + " end " + end +"\n Old \n" + automaton);
        Automaton result =
                this.performUnaryOperation(automaton,
                                           new PreciseDelete(start,end));

        // determine new bound length
        int newBoundLength;
        if (this.boundLength < start) {
            // automaton should already be empty after delete operation
            // algorithm
            newBoundLength = 0;
        } else if (this.boundLength < end) {
            newBoundLength = start;
        } else {
            int charsDeleted = end - start;
            newBoundLength = this.boundLength - charsDeleted;
        }
        //System.out.println("new bound length " + newBoundLength + " vs " + boundLength + " " + result.toString());
        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           newBoundLength);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {

        // create new model from existing automata
        Automaton cloneAutomaton = this.automaton.clone();
        return new UnboundedAutomatonModel(cloneAutomaton,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public boolean equals(AutomatonModel arg) {

        // check if arg model is unbounded automaton model
        if (arg instanceof UnboundedAutomatonModel) {

            // cast arg model
            UnboundedAutomatonModel argModel = (UnboundedAutomatonModel) arg;

            // check underlying automaton models for equality
            return this.automaton.equals(argModel.automaton);
        }

        return false;
    }

    @Override
    public String getAcceptedStringExample() {
        return this.automaton.getShortestExample(true);
    }

    @Override
    public Set<String> getFiniteStrings() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // get bounding automaton from current bound length
        int length = this.getBoundLength();
        Automaton bounding = BasicAutomata.makeAnyChar().repeat(0, length);

        // get bounded automaton
        Automaton bounded = automaton.intersection(bounding);

        // return finite strings from bounded automaton
        return bounded.getFiniteStrings();
    }

    @Override
    public AutomatonModel ignoreCase() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new IgnoreCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.getBoundLength());
    }

    @Override
    public boolean isEmpty() {
        return this.automaton.isEmptyString();
    }

    @Override
    public boolean isSingleton() {

        // get one finite string, null if more
        Set<String> strings = this.automaton.getFiniteStrings(1);

        // return if single non-null string in automaton
        return strings != null &&
               strings.size() == 1 &&
               strings.iterator().next() != null;
    }

    @Override
    public AutomatonModel minus(AutomatonModel arg) {

        // check if automaton model is unbounded
        if (!(arg instanceof UnboundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an UnboundedAutomatonModel can be subtracted from " +
                    "another UnboundedAutomatonModel.");
        }

        // cast arg model
        UnboundedAutomatonModel argModel = (UnboundedAutomatonModel) arg;

        // get intersection of automata
        Automaton result = this.automaton.minus(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // return unbounded model from automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public BigInteger modelCount() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // get bound length from model
        int length = this.boundLength;

        // return model count of automaton
        return StringModelCounter.ModelCount(automaton, length);
    }

    @Override
    public AutomatonModel prefix(int end) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new PrecisePrefix(end));

        // determine new bound length
        int newBoundLength = end;
        if (this.boundLength < newBoundLength) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           newBoundLength);
    }

    @Override
    public AutomatonModel replace(char find, char replace) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton,
                                                      new Replace1(find,
                                                                   replace));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton,
                                                      new Replace6(find,
                                                                   replace));

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int newBoundLength = this.boundLength - boundDiff;

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace4());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace2(find));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace3(replace));

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.getBoundLength());
    }

    @Override
    public AutomatonModel reverse() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Reverse());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel suffix(int start) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new PreciseSuffix(start));

        // determine new bound length
        int boundLength = this.getBoundLength() - start;

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel toLowercase() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new ToLowerCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel toUppercase() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new ToUpperCase());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel trim() {

        // workaround for trim bug
        AutomatonModel hasLength = this.assertHasLength(1, 1);
        AutomatonModel temp = this.intersect(hasLength);

        if (temp.equals(this)) {

            // return union of temp and empty string
            return temp.union(this.modelManager.createEmptyString());

        }

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // return automaton model from trim operation
        Automaton result = this.performUnaryOperation(automaton, new Trim());

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result,
                                           this.alphabet,
                                           this.boundLength);
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {

        // check if automaton model is unbounded
        if (!(arg instanceof UnboundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an UnboundedAutomatonModel can be intersected with " +
                    "another UnboundedAutomatonModel.");
        }

        // cast arg model
        UnboundedAutomatonModel argModel = (UnboundedAutomatonModel) arg;

        // get intersection of automata
        Automaton result = this.automaton.intersection(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength < this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return unbounded model from automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel union(AutomatonModel arg) {

        // check if automaton model is unbounded
        if (!(arg instanceof UnboundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an UnboundedAutomatonModel can be unioned with " +
                    "another UnboundedAutomatonModel.");
        }

        // cast arg model
        UnboundedAutomatonModel argModel = (UnboundedAutomatonModel) arg;

        // get union of automata
        Automaton result = this.automaton.union(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength > this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return unbounded model from automaton
        return new UnboundedAutomatonModel(result, this.alphabet, boundLength);
    }
}
