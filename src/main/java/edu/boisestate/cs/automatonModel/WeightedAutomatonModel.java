package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.weighted.*;


import java.math.BigInteger;
import java.util.Set;

public class WeightedAutomatonModel extends AutomatonModel {

    private WeightedAutomaton automaton;

    WeightedAutomatonModel(WeightedAutomaton automaton,
                           Alphabet alphabet,
                           int boundLength) {
        super(alphabet, boundLength);

        this.automaton = automaton;
        this.modelManager = new WeightedAutomatonModelManager(alphabet,
                                                              boundLength);
    }

    WeightedAutomatonModel(WeightedAutomaton automaton, Alphabet alphabet) {
        super(alphabet, 0);

        this.automaton = automaton;
    }

    @Override
    public AutomatonModel assertContainedInOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(containingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all substrings
        WeightedAutomaton substrings = performUnaryOperation(containing, new WeightedAllSubstrings(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(substrings);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    private static WeightedAutomaton performUnaryOperation(WeightedAutomaton automaton,
                                                           UnaryWeightedOperation operation,
                                                           Alphabet alphabet) {
        // use operation
        WeightedAutomaton result = operation.op(automaton);

        // bound resulting automaton to alphabet
        String charSet = alphabet.getCharSet();
        WeightedAutomaton anyChar = BasicWeightedAutomata.makeCharSet(charSet).repeat();
        result = result.intersection(anyChar);
        result.minimize();

        // return resulting automaton
        return result;
    }

    private void ensureWeightedModel(AutomatonModel arg) {
        // check if automaton model is bounded
        if (!(arg instanceof WeightedAutomatonModel)) {

            throw new UnsupportedOperationException(
                    "The WeightedAutomatonModel only supports binary " +
                    "operations with other WeightedAutomatonModel.");
        }
    }

    private static WeightedAutomaton getAutomatonFromWeightedModel(AutomatonModel model) {
        return ((WeightedAutomatonModel)model).automaton;
    }

    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        ensureWeightedModel(containedModel);

        // create any string automata
        WeightedAutomaton anyString1 = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        WeightedAutomaton anyString2 = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton contained = getAutomatonFromWeightedModel(containedModel);
        WeightedAutomaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.intersection(x);
        result.minimize();

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEmpty() {
        // get resulting automaton
        WeightedAutomaton result = this.automaton.intersection(BasicWeightedAutomata.makeEmptyString());

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, 0);
    }

    @Override
    public AutomatonModel assertEndsOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(containingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all suffixes
        WeightedAutomaton suffixes = performUnaryOperation(containing, new WeightedAllSuffixes(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(suffixes);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        ensureWeightedModel(endingModel);

        // create any string automata
        WeightedAutomaton anyString = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton end = getAutomatonFromWeightedModel(endingModel);
        WeightedAutomaton x = anyString.concatenate(end);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.intersection(x);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        ensureWeightedModel(equalModel);

        // concatenate with contained automaton
        WeightedAutomaton equal = getAutomatonFromWeightedModel(equalModel);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(equal);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        ensureWeightedModel(equalModel);

        // concatenate with contained automaton
        WeightedAutomaton equal = getAutomatonFromWeightedModel(equalModel);
        WeightedAutomaton equalIgnoreCase = performUnaryOperation(equal, new WeightedIgnoreCase(), alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(equalIgnoreCase);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        // check min and max
        if (min > max) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get any string with length between min and max
        WeightedAutomaton minMax = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat(min, max);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(minMax);

        // get new bound length
        int newBoundLength = max;
        if (this.boundLength < max) {
            newBoundLength = this.boundLength;
        }

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        ensureWeightedModel(notContainingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(notContainingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all substrings
        WeightedAutomaton substrings = performUnaryOperation(containing, new WeightedAllSubstrings(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.minus(substrings);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
        ensureWeightedModel(notContainedModel);

        // create any string automata
        WeightedAutomaton anyString1 = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();
        WeightedAutomaton anyString2 = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton contained = getAutomatonFromWeightedModel(notContainedModel);
        WeightedAutomaton x = anyString1.concatenate(contained).concatenate(anyString2);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.minus(x);
        result.minimize();

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        // get resulting automaton
        WeightedAutomaton result = this.automaton.minus(BasicWeightedAutomata.makeEmptyString());

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, 0);
    }

    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel) {
        ensureWeightedModel(notEndingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(notEndingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all suffixes
        WeightedAutomaton suffixes = performUnaryOperation(containing, new WeightedAllSuffixes(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.minus(suffixes);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        ensureWeightedModel(notEndingModel);

        // create any string automata
        WeightedAutomaton anyString = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton end = getAutomatonFromWeightedModel(notEndingModel);
        WeightedAutomaton x = anyString.concatenate(end);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.minus(x);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        ensureWeightedModel(notEqualModel);

        // concatenate with contained automaton
        WeightedAutomaton equal = getAutomatonFromWeightedModel(notEqualModel);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.minus(equal);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        ensureWeightedModel(notEqualModel);

        // concatenate with contained automaton
        WeightedAutomaton equal = getAutomatonFromWeightedModel(notEqualModel);
        WeightedAutomaton equalIgnoreCase = performUnaryOperation(equal, new WeightedIgnoreCase(), alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.minus(equalIgnoreCase);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel) {
        ensureWeightedModel(notStartingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(notStartingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all prefixes
        WeightedAutomaton prefixes = performUnaryOperation(containing, new WeightedAllPrefixes(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.minus(prefixes);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
        ensureWeightedModel(notStartsModel);

        // create any string automata
        WeightedAutomaton anyString = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton start = getAutomatonFromWeightedModel(notStartsModel);
        WeightedAutomaton x = start.concatenate(anyString);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.minus(x);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel assertStartsOther(AutomatonModel containingModel) {
        ensureWeightedModel(containingModel);

        // get containing automaton
        WeightedAutomaton containing = getAutomatonFromWeightedModel(containingModel);

        // if either automata is  empty
        if (this.automaton.isEmpty() || containing.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // get all prefixes
        WeightedAutomaton prefixes = performUnaryOperation(containing, new WeightedAllPrefixes(), this.alphabet);

        // get resulting automaton
        WeightedAutomaton result =  this.automaton.intersection(prefixes);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        ensureWeightedModel(startingModel);

        // create any string automata
        WeightedAutomaton anyString = BasicWeightedAutomata.makeCharSet(this.alphabet.getCharSet()).repeat();

        // concatenate with contained automaton
        WeightedAutomaton start = getAutomatonFromWeightedModel(startingModel);
        WeightedAutomaton x = start.concatenate(anyString);

        // get resulting automaton
        WeightedAutomaton result = this.automaton.intersection(x);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel clone() {
        WeightedAutomaton cloneAutomaton = this.automaton.clone();
        return new WeightedAutomatonModel(cloneAutomaton, alphabet, boundLength);
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton arg = getAutomatonFromWeightedModel(argModel);

        // get concatenation of automata
        WeightedAutomaton result = this.automaton.concatenate(arg);

        // calculate new bound length
        int boundLength = this.boundLength + argModel.boundLength;

        // return weighted model from automaton
        return new WeightedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public boolean containsString(String actualValue) {
        return this.automaton.run(actualValue);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public AutomatonModel delete(int start, int end) {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new PreciseWeightedDelete(start, end), this.alphabet);

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
        return new WeightedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public boolean equals(AutomatonModel arg) {

        // check if arg model is bounded automaton model
        if (arg instanceof WeightedAutomatonModel) {

            // cast arg model
            WeightedAutomatonModel argModel = (WeightedAutomatonModel) arg;

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
        return automaton.getFiniteStrings();
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton arg = getAutomatonFromWeightedModel(argModel);

        // get result of operation
        WeightedPreciseInsert insert = new WeightedPreciseInsert(offset);
        WeightedAutomaton result = insert.op(automaton, arg);

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength < this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return weighted model from automaton
        return new WeightedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel intersect(AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton arg = getAutomatonFromWeightedModel(argModel);

        // get result of operation
        WeightedAutomaton result = this.automaton.intersection(arg);

        // return weighted model from automaton
        return new WeightedAutomatonModel(result, this.alphabet, boundLength);
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
    public BigInteger modelCount() {
        return StringModelCounter.ModelCount(automaton);
    }

    @Override
    public AutomatonModel replace(char find, char replace) {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedReplaceChar(find, replace), this.alphabet);

        // return weighted model from automaton
        return new WeightedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel replace(String find, String replace) {
        // perform operation
        WeightedReplaceString replaceOp = new WeightedReplaceString(find, replace);
        WeightedAutomaton result = performUnaryOperation(automaton, replaceOp, this.alphabet);

        // determine new bound length
        int boundDiff = find.length() - replace.length();
        int newBoundLength = this.boundLength - boundDiff;

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel replaceChar() {
        return null;
    }

    @Override
    public AutomatonModel replaceFindKnown(char find) {
        return null;
    }

    @Override
    public AutomatonModel replaceReplaceKnown(char replace) {
        return null;
    }

    @Override
    public AutomatonModel reverse() {
        // if automaton is empty
        if (this.automaton.isEmpty()) {
            return new WeightedAutomatonModel(BasicWeightedAutomata.makeEmpty(), this.alphabet, 0);
        }

        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedReverse(), this.alphabet);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        ensureWeightedModel(argModel);

        // get arg automaton
        WeightedAutomaton arg = getAutomatonFromWeightedModel(argModel);

        // get result of operation
        WeightedSetCharAt setCharAt = new WeightedSetCharAt(offset);
        WeightedAutomaton result = setCharAt.op(automaton, arg);

        // return weighted model from automaton
        return new WeightedAutomatonModel(result, this.alphabet, boundLength);
    }

    @Override
    public AutomatonModel setLength(int length) {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedPreciseSetLength(length), this.alphabet);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel substring(int start, int end) {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedPreciseSubstring(start, end), this.alphabet);

        // determine new bound length
        int newBoundLength = end - start;

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel suffix(int start) {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedPreciseSuffix(start), this.alphabet);

        // determine new bound length
        int newBoundLength = this.boundLength - start;

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, newBoundLength);
    }

    @Override
    public AutomatonModel toLowercase() {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedToLowerCase(), this.alphabet);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel toUppercase() {
        // perform operation
        WeightedAutomaton result = performUnaryOperation(automaton, new WeightedToUpperCase(), this.alphabet);

        // return new model from resulting automaton
        return new WeightedAutomatonModel(result, this.alphabet, this.boundLength);
    }

    @Override
    public AutomatonModel trim() {
        return null;
    }
}
