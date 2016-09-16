package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;

import java.util.Set;

public class UnboundedAutomatonModel
        extends AutomatonModel {

    private Automaton automaton;

    Automaton getAutomaton() {
        return this.automaton;
    }

    private void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    UnboundedAutomatonModel(Automaton automaton,
                            Alphabet alphabet,
                            int initialBoundLength) {
        super(alphabet, initialBoundLength);

        // set automaton from parameter
        this.automaton = automaton;
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
