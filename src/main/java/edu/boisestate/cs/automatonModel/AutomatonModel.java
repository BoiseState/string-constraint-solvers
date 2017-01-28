package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.Alphabet;

import java.math.BigInteger;
import java.util.Set;

public abstract class AutomatonModel
        implements Cloneable {

    protected final Alphabet alphabet;
    protected int boundLength;
    protected AutomatonModelManager modelManager;

    public abstract String getAcceptedStringExample();

    public int getBoundLength() {
        return boundLength;
    }

    public abstract Set<String> getFiniteStrings();

    public abstract boolean isEmpty();

    public abstract boolean isSingleton();

    public void setBoundLength(int boundLength) {
        this.boundLength = boundLength;
    }

    protected AutomatonModel(Alphabet alphabet,
                             int initialBoundLength) {

        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    public abstract AutomatonModel delete(int start, int end);

    public abstract boolean containsString(String actualValue);

    public abstract boolean equals(AutomatonModel arg);

    public abstract AutomatonModel union(AutomatonModel arg);

    public abstract AutomatonModel replaceChar();

    public abstract AutomatonModel replace(char find, char replace);

    public abstract AutomatonModel replace(String find, String replace);

    public abstract AutomatonModel reverse();

    public abstract AutomatonModel allPrefixes();

    public abstract AutomatonModel toLowercase();

    public abstract AutomatonModel toUppercase();

    public abstract AutomatonModel trim();

    public abstract BigInteger modelCount();

    public abstract AutomatonModel ignoreCase();

    public abstract AutomatonModel replaceFindKnown(char find);

    public abstract AutomatonModel replaceReplaceKnown(char replace);

    public abstract AutomatonModel clone();

    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {

        // create any string models
        AutomatonModel anyString1 = this.modelManager.createAnyString();
        AutomatonModel anyString2 = this.modelManager.createAnyString();

        // concatenate with contained model
        AutomatonModel x = anyString1.concatenate(containedModel)
                                     .concatenate(anyString2);

        // return intersection with model
        return this.intersect(x);
    }

    public abstract AutomatonModel intersect(AutomatonModel arg);

    public abstract AutomatonModel concatenate(AutomatonModel arg);

    public AutomatonModel assertEmpty() {

        // intersect model with empty string
        AutomatonModel empty = modelManager.createEmptyString();
        return this.intersect(empty);
    }

    public AutomatonModel assertEndsOther(AutomatonModel baseModel) {
        AutomatonModel suffixes = baseModel.allSuffixes();
        return this.intersect(suffixes);
    }

    public abstract AutomatonModel allSuffixes();

    public AutomatonModel assertContainedInOther(AutomatonModel containingModel) {

        // get all substrings for containing model
        AutomatonModel substrings = containingModel.allSubstrings();

        // intersect substrings with model
        return this.intersect(substrings);
    }

    public abstract AutomatonModel allSubstrings();

    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {

        // get end model by concatenating any string and ending model
        AutomatonModel end =
                this.modelManager.createAnyString()
                                 .concatenateIndividual(endingModel);

        // return intersection of model and end
        return this.intersect(end);
    }

    public AutomatonModel assertEquals(AutomatonModel equalModel) {

        // return intersection of model with equal model
        return this.intersect(equalModel);
    }

    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        AutomatonModel ignoreCase = equalModel.ignoreCase();
        return this.intersect(ignoreCase);
    }

    public AutomatonModel assertHasLength(int min, int max) {

        // check min and max
        if (min > max) {
            return modelManager.createEmpty();
        }

        // get any string with length between min and max
        AutomatonModel minMaxString = modelManager.createAnyString(min, max);

        // return intersection of model and min max string
        return this.intersect(minMaxString);
    }

    public AutomatonModel assertNotContainedInOther(AutomatonModel
                                                            notContainingModel) {

        // get all substrings for the not containing model
        AutomatonModel substrings = notContainingModel.allSubstrings();

        // get complement of substrings
        AutomatonModel complement = substrings.complement();

        // return intersection of model and complement
        return this.intersect(complement);
    }

    public abstract AutomatonModel complement();

    public AutomatonModel assertNotContainsOther(AutomatonModel
                                                         notContainedModel) {

        // concat not contained model with any strings
        AutomatonModel concat = modelManager.createAnyString()
                                            .concatenate(notContainedModel)
                                            .concatenate(modelManager
                                                                 .createAnyString());

        // return model minus concatenation
        return this.minus(concat);
    }

    public abstract AutomatonModel minus(AutomatonModel arg);

    public AutomatonModel assertNotEmpty() {

        // get empty string model
        AutomatonModel emptyString = modelManager.createEmptyString();

        // return model minus empty string
        return this.minus(emptyString);
    }

    public AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel) {
        AutomatonModel x = notEndingModel.allSuffixes();
        AutomatonModel argComplement = this.complement();
        return x.intersect(argComplement);
    }

    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        AutomatonModel subtract = this.modelManager.createAnyString();
        subtract = subtract.concatenateIndividual(notEndingModel);
        return this.minus(subtract);
    }

    public abstract AutomatonModel concatenateIndividual(AutomatonModel arg);

    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {

        // subtract not equal model from model
        return this.minus(notEqualModel);
    }

    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        AutomatonModel ignoreCase = notEqualModel.ignoreCase();
        return this.minus(ignoreCase);
    }

    public AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel) {
        AutomatonModel baseComplement = notStartingModel.complement();
        return this.intersect(baseComplement);
    }

    public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
        AutomatonModel x = this.modelManager.createAnyString();
        x = notStartsModel.concatenateIndividual(x);
        return this.minus(x);
    }

    public AutomatonModel assertStartsOther(AutomatonModel startingModel) {
        AutomatonModel x = startingModel.allPrefixes();
        return this.intersect(x);
    }

    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {

        // get start model by concatenating starting model and any string
        AutomatonModel anyString = this.modelManager.createAnyString();
        AutomatonModel start = startingModel.concatenateIndividual(anyString);

        // return intersection of model and start
        return this.intersect(start);
    }

    public AutomatonModel substring(int start, int end) {

        // check start and end indices
        if (start == end) {
            return this.modelManager.createEmptyString();
        } else if (start == 0) {
            return this.prefix(end);
        }

        // get suffix
        AutomatonModel suffix = this.suffix(start);

        // return prefix from suffix as substring
        return suffix.prefix(end - start);
    }

    public abstract AutomatonModel prefix(int end);

    public abstract AutomatonModel suffix(int start);

    protected Automaton performUnaryOperation(Automaton automaton,
                                              UnaryOperation operation) {

        // use operation
        Automaton result = operation.op(automaton);

        // bound automaton to alphabet
        String charSet = this.alphabet.getCharSet();
        Automaton alphabet = BasicAutomata.makeCharSet(charSet).repeat();
        result = result.intersection(alphabet);

        // return resulting automaton
        return result;
    }
}
