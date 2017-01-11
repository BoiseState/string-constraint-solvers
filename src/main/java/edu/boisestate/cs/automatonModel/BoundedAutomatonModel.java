package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automatonModel.operations.*;

import java.math.BigInteger;
import java.util.Set;

public class BoundedAutomatonModel
        extends AutomatonModel {

    private Automaton automaton;

    Automaton getAutomaton() {
        return this.automaton;
    }

    BoundedAutomatonModel(Automaton automaton,
                          Alphabet alphabet,
                          int boundLength) {
        super(alphabet, boundLength);

        this.automaton = automaton;

        this.modelManager =
                new BoundedAutomatonModelManager(alphabet, boundLength);
    }

    BoundedAutomatonModel(Automaton automaton, Alphabet alphabet) {
        super(alphabet, 0);

        this.automaton = automaton;
    }

    @Override
    public AutomatonModel allPrefixes() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Prefix());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel allSubstrings() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Substring());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
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
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public AutomatonModel clone() {

        // create new model from existing automata
        Automaton cloneAutomaton = this.automaton.clone();
        return new BoundedAutomatonModel(cloneAutomaton,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel complement() {

        // get complement of automaton
        Automaton complement = this.automaton.complement();

        // get any string automaton up to current bound length from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat(0, this.boundLength);

        // get intersection of complement and alphabet any string
        Automaton automaton = complement.intersection(anyString);

        // return new model from complement automaton
        return new BoundedAutomatonModel(automaton,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel concatenateIndividual(AutomatonModel arg) {
        return concatenate(arg);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel arg) {

        // check if automaton model is bounded
        if (!(arg instanceof BoundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an BoundedAutomatonModel can be concatenated to " +
                    "another BoundedAutomatonModel.");
        }

        // cast arg model
        BoundedAutomatonModel argModel = (BoundedAutomatonModel) arg;

        // get concatenation of automata
        Automaton result = this.automaton.concatenate(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength + argModel.boundLength;

        // return bounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public boolean containsString(String actualValue) {
        return this.automaton.run(actualValue);
    }

    @Override
    public AutomatonModel delete(int start, int end) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton,
                                           new PreciseDelete(start, end));

        // determine new bound length
        int newBoundLength;
        if (this.boundLength < start) {
            newBoundLength = 0;
        } else if (this.boundLength < end) {
            newBoundLength = start;
        } else {
            int charsDeleted = end - start;
            newBoundLength = this.boundLength - charsDeleted;
        }

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public boolean equals(AutomatonModel arg) {

        // check if arg model is bounded automaton model
        if (arg instanceof BoundedAutomatonModel) {

            // cast arg model
            BoundedAutomatonModel argModel = (BoundedAutomatonModel) arg;

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

        // return finite strings from automaton
        return automaton.getFiniteStrings();
    }

    @Override
    public AutomatonModel ignoreCase() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new IgnoreCase());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {

        // check if automaton model is bounded
        if (!(arg instanceof BoundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an BoundedAutomatonModel can be intersected with " +
                    "another BoundedAutomatonModel.");
        }

        // cast arg model
        BoundedAutomatonModel argModel = (BoundedAutomatonModel) arg;

        // get intersection of automata
        Automaton result = this.automaton.intersection(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength < this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return bounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, boundLength);
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

        // check if automaton model is bounded
        if (!(arg instanceof BoundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an BoundedAutomatonModel can be subtracted from " +
                    "another BoundedAutomatonModel.");
        }

        // cast arg model
        BoundedAutomatonModel argModel = (BoundedAutomatonModel) arg;

        // get intersection of automata
        Automaton result = this.automaton.minus(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // return bounded model from automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public BigInteger modelCount() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // return model count of automaton
        return StringModelCounter.ModelCount(automaton);
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
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
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
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Replace6 replaceOp = new Replace6(find, replace);
        Automaton result = this.performUnaryOperation(automaton, replaceOp);

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int newBoundLength = this.boundLength - boundDiff;

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new Replace4());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
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
        return new BoundedAutomatonModel(result,
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
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel reverse() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result = this.performUnaryOperation(automaton, new Reverse());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
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
        int newBoundLength = this.boundLength - start;

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel toLowercase() {

        // get automaton from model
        Automaton automaton = this.getAutomaton();

        // perform operation
        Automaton result =
                this.performUnaryOperation(automaton, new ToLowerCase());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
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
        return new BoundedAutomatonModel(result,
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
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel union(AutomatonModel arg) {

        // check if automaton model is bounded
        if (!(arg instanceof BoundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "Only an BoundedAutomatonModel can be unioned with " +
                    "another BoundedAutomatonModel.");
        }

        // cast arg model
        BoundedAutomatonModel argModel = (BoundedAutomatonModel) arg;

        // get union of automata
        Automaton result = this.automaton.union(argModel.automaton);

        // minimize result automaton
        result.minimize();

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength > this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return bounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, boundLength);
    }
}
