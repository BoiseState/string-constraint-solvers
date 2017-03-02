package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.BasicWeightedAutomata;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automatonModel.operations.weighted.UnaryWeightedOperation;


import edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedAllSubstrings;

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

    @Override
    public String getAcceptedStringExample() {
        return null;
    }

    @Override
    public Set<String> getFiniteStrings() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
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

    @Override
    public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
        return null;
    }

    @Override
    public AutomatonModel assertEmpty() {
        return null;
    }

    @Override
    public AutomatonModel assertEndsOther(AutomatonModel baseModel) {
        return null;
    }

    @Override
    public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertEquals(AutomatonModel equalModel) {
        return null;
    }

    @Override
    public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
        return null;
    }

    @Override
    public AutomatonModel assertHasLength(int min, int max) {
        return null;
    }

    @Override
    public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotContainsOther(AutomatonModel
                                                             notContainedModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotEmpty() {
        return null;
    }

    @Override
    public AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
        return null;
    }

    @Override
    public AutomatonModel assertStartsOther(AutomatonModel startingModel) {
        return null;
    }

    @Override
    public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
        return null;
    }

    @Override
    public AutomatonModel concatenate(AutomatonModel arg) {
        return null;
    }

    @Override
    public boolean containsString(String actualValue) {
        return false;
    }

    @Override
    public AutomatonModel delete(int start, int end) {
        return null;
    }

    @Override
    public boolean equals(AutomatonModel arg) {
        return false;
    }

    @Override
    public AutomatonModel intersect(AutomatonModel arg) {
        return null;
    }

    @Override
    public AutomatonModel insert(int offset, AutomatonModel argModel) {
        return null;
    }

    @Override
    public BigInteger modelCount() {
        return null;
    }

    @Override
    public AutomatonModel replace(char find, char replace) {
        return null;
    }

    @Override
    public AutomatonModel replace(String find, String replace) {
        return null;
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
        return null;
    }

    @Override
    public AutomatonModel substring(int start, int end) {
        return null;
    }

    @Override
    public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
        return null;
    }

    @Override
    public AutomatonModel setLength(int length) {
        return null;
    }

    @Override
    public AutomatonModel suffix(int start) {
        return null;
    }

    @Override
    public AutomatonModel toLowercase() {
        return null;
    }

    @Override
    public AutomatonModel toUppercase() {
        return null;
    }

    @Override
    public AutomatonModel trim() {
        return null;
    }

    @Override
    public AutomatonModel clone() {
        return null;
    }
}
