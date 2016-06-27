package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import java.util.Set;

public class BoundedAutomatonModel
        extends AutomatonModel {

    private Automaton automaton;

    Automaton getAutomaton() {
        return this.automaton;
    }

    BoundedAutomatonModel(Automaton automaton, Alphabet alphabet, int boundLength) {
        super(alphabet, boundLength);

        this.automaton = automaton;
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

        // return bounded model from automaton
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

        // calculate new bound length
        int boundLength = this.boundLength;
        if (argModel.boundLength > this.boundLength) {
            boundLength = argModel.boundLength;
        }

        // return bounded model from automaton
        return new BoundedAutomatonModel(result, this.alphabet, boundLength);
    }
}
