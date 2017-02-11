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
    public AutomatonModel assertContainedInOther(AutomatonModel containingModel) {
        ensureBoundedModel(containingModel);

        // get all substrings
        Automaton containing = getAutomatonFromBoundedModel(containingModel);
        Automaton substrings = performUnaryOperation(containing, new Substring(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(substrings);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    private void ensureBoundedModel(AutomatonModel arg) {
        // check if automaton model is bounded
        if (!(arg instanceof BoundedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "The BoundedAutomatonModel only supports binary " +
                    "operations with other BoundedAutomatonModels.");
        }
    }

    private static Automaton getAutomatonFromBoundedModel(AutomatonModel model) {
        return ((BoundedAutomatonModel)model).automaton;
    }

    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        ensureBoundedModel(containedModel);

        // create any string automata
        Automaton anyString1 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        Automaton anyString2 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton contained = getAutomatonFromBoundedModel(containedModel);
        Automaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(x);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automaton
        Automaton result = this.automaton.intersection(BasicAutomata.makeEmptyString());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, 0);
    }

    @Override
    public AutomatonModel assertEndsOther(AutomatonModel containingModel) {
        ensureBoundedModel(containingModel);

        // get all suffixes
        Automaton containing = getAutomatonFromBoundedModel(containingModel);
        Automaton suffixes = performUnaryOperation(containing, new Postfix(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(suffixes);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        ensureBoundedModel(endingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with ending automaton
        Automaton end = getAutomatonFromBoundedModel(endingModel);
        Automaton x = anyString.concatenate(end);

        // get bounded resulting automaton
        Automaton result =  this.automaton.intersection(x);
        result = boundAutomaton(result, boundLength, this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    private static Automaton boundAutomaton(Automaton automaton, int length, Alphabet alphabet) {
        // create bounding automaton
        Automaton bounding = BasicAutomata.makeCharSet(alphabet.getCharSet()).repeat(0, length);

        // return bounded automaton
        return automaton.intersection(bounding);
    }

    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureBoundedModel(equalModel);

        // concatenate with contained automaton
        Automaton equal = getAutomatonFromBoundedModel(equalModel);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(equal);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        ensureBoundedModel(equalModel);

        // concatenate with contained automaton
        Automaton equal = getAutomatonFromBoundedModel(equalModel);
        Automaton equalIgnoreCase = performUnaryOperation(equal, new IgnoreCase(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(equalIgnoreCase);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        ensureBoundedModel(notContainingModel);

        // get all substrings
        Automaton containing = getAutomatonFromBoundedModel(notContainingModel);
        Automaton substrings = performUnaryOperation(containing, new Substring(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.minus(substrings);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureBoundedModel(notContainedModel);

        // create any string automata
        Automaton anyString1 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        Automaton anyString2 =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with not contained automaton
        Automaton contained = getAutomatonFromBoundedModel(notContainedModel);
        Automaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automaton
        Automaton result =  this.automaton.minus(x);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automaton
        Automaton result = this.automaton.minus(BasicAutomata.makeEmptyString());

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notContainingModel) {
        ensureBoundedModel(notContainingModel);

        // get all suffixes
        Automaton containing = getAutomatonFromBoundedModel(notContainingModel);
        Automaton suffixes = performUnaryOperation(containing, new Postfix(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.minus(suffixes);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureBoundedModel(notEndingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with not ending automaton
        Automaton ending = getAutomatonFromBoundedModel(notEndingModel);
        Automaton x = anyString.concatenate(ending);

        // get resulting automaton
        Automaton result =  this.automaton.minus(x);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureBoundedModel(notEqualModel);

        // concatenate with contained automaton
        Automaton equal = getAutomatonFromBoundedModel(notEqualModel);

        // get resulting automaton
        Automaton result =  this.automaton.minus(equal);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureBoundedModel(notEqualModel);

        // concatenate with contained automaton
        Automaton equal = getAutomatonFromBoundedModel(notEqualModel);
        Automaton equalIgnoreCase = performUnaryOperation(equal, new IgnoreCase(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.minus(equalIgnoreCase);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notContainingModel) {
        ensureBoundedModel(notContainingModel);

        // get all prefixes
        Automaton containing = getAutomatonFromBoundedModel(notContainingModel);
        Automaton prefixes = performUnaryOperation(containing, new Prefix(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.minus(prefixes);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
        ensureBoundedModel(notStartsModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with not starts automaton
        Automaton start = getAutomatonFromBoundedModel(notStartsModel);
        Automaton x = start.concatenate(anyString);

        // get resulting automaton
        Automaton result =  this.automaton.minus(x);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertStartsOther(AutomatonModel containingModel) {
        ensureBoundedModel(containingModel);

        // get all prefixes
        Automaton containing = getAutomatonFromBoundedModel(containingModel);
        Automaton prefixes = performUnaryOperation(containing, new Prefix(), this.alphabet);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(prefixes);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        ensureBoundedModel(startingModel);

        // create any string automata
        Automaton anyString =
                BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        Automaton start = getAutomatonFromBoundedModel(startingModel);
        Automaton x = start.concatenate(anyString);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(x);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, this.boundLength);
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
    public AutomatonModel complement(int boundLength) {

        // get complement of automaton
        Automaton complement = this.automaton.complement();

        // set current bounding length
        int currentBoundLength = boundLength;
        if (this.boundLength > boundLength) {
            currentBoundLength = this.boundLength;
        }

        // get any string automaton up to current bound length from alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton anyString = BasicAutomata.makeCharSet(charSet)
                                           .repeat(0, currentBoundLength);

        // get intersection of complement and alphabet any string
        Automaton automaton = complement.intersection(anyString);

        // return new model from complement automaton
        return new BoundedAutomatonModel(automaton,
                                         this.alphabet,
                                         currentBoundLength);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {
        ensureBoundedModel(argModel);

        // get arg automaton
        Automaton arg = getAutomatonFromBoundedModel(argModel);

        // get concatenation of automata
        Automaton result = this.automaton.concatenate(arg);

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

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel delete(int start, int end) {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new PreciseDelete(start, end), this.alphabet);

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

        // return finite strings from automaton
        return automaton.getFiniteStrings();
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        ensureBoundedModel(argModel);

        // get automata for operations
        Automaton arg = getAutomatonFromBoundedModel(argModel);
        Automaton before = performUnaryOperation(this.automaton, new PrecisePrefix(offset), this.alphabet);
        Automaton after = performUnaryOperation(this.automaton, new PreciseSuffix(offset), this.alphabet);

        // get resulting automaton
        Automaton result = before.concatenate(arg).concatenate(after);

        // calculate new bound length
        int newBoundLength = this.boundLength + argModel.boundLength;

        // return unbounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
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
        ensureBoundedModel(arg);

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

        // return model count of automaton
        return StringModelCounter.ModelCount(automaton);
    }

    @Override
    public AutomatonModel prefix(int end) {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new PrecisePrefix(end), this.alphabet);

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

        // perform operation
        Automaton result = performUnaryOperation(automaton, new Replace1(find, replace), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {

        // perform operation
        Replace6 replaceOp = new Replace6(find, replace);
        Automaton result = performUnaryOperation(automaton, replaceOp, this.alphabet);

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int newBoundLength = this.boundLength - boundDiff;

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new Replace4(), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new Replace2(find), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new Replace3(replace), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel reverse() {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new Reverse(), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        ensureBoundedModel(argModel);

        // get automata for operations
        Automaton arg = getAutomatonFromBoundedModel(argModel);
        Automaton before = performUnaryOperation(this.automaton, new PrecisePrefix(offset), this.alphabet);
        Automaton after = performUnaryOperation(this.automaton, new PreciseSuffix(offset + 1), this.alphabet);

        // get resulting automaton
        Automaton result = before.concatenate(arg).concatenate(after);

        // calculate new bound length
        int newBoundLength = this.boundLength + 1;

        // return unbounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel setLength(int length) {
        // if length is 0, return empty string model
        if (length == 0) {
            return new BoundedAutomatonModel(BasicAutomata.makeEmptyString(),
                                               this.alphabet,
                                               0);
        }

        // create automata for operations
        Automaton nullChars = BasicAutomata.makeChar('\u0000').repeat(0,length);
        Automaton bounding = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat(0, length);

        // get resulting automaton
        Automaton result = this.automaton.concatenate(nullChars)
                                         .intersection(bounding);

        // return unbounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, length);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel substring(int start, int end) {
        // initialize result automaton
        Automaton result;

        // get resulting automaton
        if (start == end) {
            result = BasicAutomata.makeEmptyString();
        } else if (start == 0) {
            result = performUnaryOperation(this.automaton, new PrecisePrefix(end), this.alphabet);
        } else {
            Automaton suffix = performUnaryOperation(this.automaton, new PreciseSuffix(start), this.alphabet);
            result = performUnaryOperation(suffix, new PrecisePrefix(end - start), this.alphabet);
        }

        // get new bound length
        int newBoundLength = end - start;

        // return new model from resulting automaton
        return new UnboundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel suffix(int start) {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new PreciseSuffix(start), this.alphabet);

        // determine new bound length
        int newBoundLength = this.boundLength - start;

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel toLowercase() {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new ToLowerCase(), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel toUppercase() {

        // perform operation
        Automaton result = performUnaryOperation(automaton, new ToUpperCase(), this.alphabet);

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

        // return automaton model from trim operation
        Automaton result = performUnaryOperation(automaton, new Trim(), this.alphabet);

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result,
                                         this.alphabet,
                                         this.boundLength);
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        // check min and max
        if (min > max) {
            return new BoundedAutomatonModel(BasicAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get any string with length between min and max
        Automaton minMax = BasicAutomata.makeCharSet(this.alphabet.getCharSet()).repeat(min, max);

        // get resulting automaton
        Automaton result =  this.automaton.intersection(minMax);

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new BoundedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {
        ensureBoundedModel(arg);

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
    public AutomatonModel union(AutomatonModel argModel) {
        ensureBoundedModel(argModel);

        // get arg model automaton
        Automaton argAutomaton = getAutomatonFromBoundedModel(argModel);

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength > this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // get bounded resulting automaton
        Automaton result = this.automaton.union(argAutomaton);
        result.minimize();
        result = boundAutomaton(result, boundLength, this.alphabet);

        // return bounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, boundLength);
    }
}
