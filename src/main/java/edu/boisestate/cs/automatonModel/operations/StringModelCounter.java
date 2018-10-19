package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;

import java.math.BigInteger;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class StringModelCounter {

    static public BigInteger ModelCount(WeightedState state) {

        // get model count
        BigInteger modelCount = ModelCount(state,
                                           -1,
                                           BigInteger.ONE,
                                           0);

        // account for empty string
        if (state.isAccept()) {
            modelCount = modelCount.add(BigInteger.ONE);
        }

        return modelCount;
    }

    static public BigInteger ModelCount(State state, int length) {

        // get model count
        BigInteger modelCount = ModelCount(state,
                                           length,
                                           BigInteger.ONE,
                                           0);

        // account for empty string
        if (state.isAccept()) {
            modelCount = modelCount.add(BigInteger.ONE);
        }

        return modelCount;
    }

    // default model counter for bounded automata
    static public BigInteger ModelCount(WeightedAutomaton automaton) {

        // initialize big integer value
        BigInteger factor = BigInteger.valueOf(automaton.getInitialFactor());

        // account for empty string
        if (automaton.isEmptyString()) {
            BigInteger emptyStrings = BigInteger.valueOf(automaton.getNumEmptyStrings());
            return factor.multiply(emptyStrings);
        }

        // get model count
        BigInteger modelCount = ModelCount(automaton.getInitialState(),
                                           -1,
                                           factor,
                                           0);

        // account for empty string
        if (automaton.getInitialState().isAccept()) {
            BigInteger emptyStrings = BigInteger.valueOf(automaton.getNumEmptyStrings());
            emptyStrings = factor.multiply(emptyStrings);
            modelCount = modelCount.add(emptyStrings);
        }
        return modelCount;
    }

    // default model counter for unbounded automata, count is specified
    static public BigInteger ModelCount(WeightedAutomaton automaton, int initialCount) {

        // initialize big integer value
        BigInteger factor = BigInteger.valueOf(automaton.getInitialFactor());

        // allow empty string
        if (initialCount == 0 && automaton.getInitialState().isAccept()) {
            BigInteger emptyStrings = BigInteger.valueOf(automaton.getNumEmptyStrings());
            return factor.multiply(emptyStrings);
        } else if (initialCount < 1) {
            return BigInteger.ZERO;
        }

        // get model count
        BigInteger modelCount = ModelCount(automaton.getInitialState(),
                                           initialCount,
                                           factor,
                                           0);

        // account for empty string
        if (automaton.getInitialState().isAccept()) {
            BigInteger emptyStrings = BigInteger.valueOf(automaton.getNumEmptyStrings());
            emptyStrings = factor.multiply(emptyStrings);
            modelCount = modelCount.add(emptyStrings);
        }

        return modelCount;
    }

    // recursive model counter algorithm
    static private BigInteger ModelCount(WeightedState state,
                                         int initialCounter,
                                         BigInteger initialModelCount,
                                         int initialDepth) {

        int depth = initialDepth + 1;

        // initialize return model count
        BigInteger returnModelCount = new BigInteger("0");

        // initialize counter to initialCounter
        int counter = initialCounter;

        // decrement counter if greater than 0
        if (initialCounter >= 0) {
            counter--;
        }

        // get all transitions from current state
        Set<WeightedTransition>transitions = state.getTransitions();

        // for each transition
        for (WeightedTransition transition : transitions) {

            // get min and max chars for transition
            char minChar = transition.getMin();
            char maxChar = transition.getMax();

            // get number of chars represented by transition
            int transitionCount = maxChar - minChar + 1;
            transitionCount = transitionCount * transition.getWeightInt();

            // multiply initial model count by transition count
            // to get current model count
            BigInteger currentModelCount = initialModelCount
                    .multiply(BigInteger.valueOf(transitionCount));

            // get destination state
            WeightedState destination = transition.getDest();

            // if destination is an accepting state, increment total count
            if (destination.isAccept()) {
                returnModelCount = returnModelCount.add(currentModelCount);
            }

            // if boundary has not been reached, make recursive call
            if (counter != 0) {
                BigInteger childModelCount = ModelCount(destination,
                                                        counter,
                                                        currentModelCount,
                                                        depth);

                // add child model count to return model count
                returnModelCount = returnModelCount.add(childModelCount);
            }
        }

        // return the valid model count from this level of the automaton
        return returnModelCount;
    }

    // default model counter for bounded automata
    static public BigInteger ModelCount(Automaton automaton) {

        // initialize big integer value
        BigInteger initialModelCount = new BigInteger("1");

        // account for empty string
        if (automaton.isEmptyString()) {
            return initialModelCount;
        }

        // get model count
        BigInteger modelCount = ModelCount(automaton.getInitialState(),
                                           -1,
                                           initialModelCount,
                                           0);

        // account for empty string
        if (automaton.getInitialState().isAccept()) {

            modelCount = modelCount.add(new BigInteger("1"));
        }
        return modelCount;
    }

    // default model counter for unbounded automata, count is specified
    static public BigInteger ModelCount(Automaton automaton, int initialCount) {

        // allow empty string
        if (initialCount == 0 && automaton.getInitialState().isAccept()) {
            return new BigInteger("1");
        } else if (initialCount < 1) {
            return new BigInteger("0");
        }

        // initialize big integer value
        BigInteger initialModelCount = new BigInteger("1");

        // get model count
        BigInteger modelCount = ModelCount(automaton.getInitialState(),
                                           initialCount,
                                           initialModelCount,
                                           0);

        // account for empty string
        if (automaton.getInitialState().isAccept()) {

            modelCount = modelCount.add(new BigInteger("1"));
        }

        return modelCount;
    }

    // recursive model counter algorithm
    static private BigInteger ModelCount(State state,
                                         int initialCounter,
                                         BigInteger initialModelCount,
                                         int initialDepth) {

        int depth = initialDepth + 1;

        // initialize return model count
        BigInteger returnModelCount = new BigInteger("0");

        // initialize counter to initialCounter
        int counter = initialCounter;

        // decrement counter if greater than 0
        if (initialCounter >= 0) {
            counter--;
        }

        // get all transitions from current state
        Set<Transition> transitions = state.getTransitions();

        // for each transition
        for (Transition transition : transitions) {

            // get min and max chars for transition
            char minChar = transition.getMin();
            char maxChar = transition.getMax();

            // get number of chars represented by transition
            int transitionCount = maxChar - minChar + 1;

            // multiply initial model count by transition count
            // to get current model count
            BigInteger currentModelCount = initialModelCount
                    .multiply(BigInteger.valueOf(transitionCount));

            // get destination state
            State destination = transition.getDest();

            // if destination is an accepting state, increment total count
            if (destination.isAccept()) {
                returnModelCount = returnModelCount.add(currentModelCount);
            }

            // if boundary has not been reached, make recursive call
            if (counter != 0) {
                BigInteger childModelCount = ModelCount(destination,
                                                        counter,
                                                        currentModelCount,
                                                        depth);

                // add child model count to return model count
                returnModelCount = returnModelCount.add(childModelCount);
            }
        }

        // return the valid model count from this level of the automaton
        return returnModelCount;
    }

    // recursive model counter algorithm
    static public BigInteger pseudoModelCount(WeightedState state,
                                               int initialCounter,
                                               BigInteger initialModelCount) {

        // initialize return model count
        BigInteger returnModelCount = new BigInteger("0");

        // initialize counter to initialCounter
        int counter = initialCounter;

        // decrement counter if greater than 0
        if (initialCounter >= 0) {
            counter--;
        }

        // get all transitions from current state
        Set<WeightedTransition> transitions = state.getTransitions();

        // for each transition
        for (WeightedTransition transition : transitions) {

            // get min and max chars for transition
            char minChar = transition.getMin();
            char maxChar = transition.getMax();

            // get number of chars represented by transition
            int transitionCount = maxChar - minChar + 1;

            // multiply initial model count by transition count
            // to get current model count
            BigInteger currentModelCount = initialModelCount
                    .multiply(BigInteger.valueOf(transitionCount));

            // get destination state
            WeightedState destination = transition.getDest();

            // if destination is an accepting state, increment total count
            if (destination.isAccept() || counter == 0) {
                returnModelCount = returnModelCount.add(currentModelCount);
            }

            // if boundary has not been reached, make recursive call
            if (counter != 0) {
                BigInteger childModelCount = pseudoModelCount(destination, counter, currentModelCount);

                // add child model count to return model count
                returnModelCount = returnModelCount.add(childModelCount);
            }
        }

        // return the valid model count from this level of the automaton
        return returnModelCount;
    }

}
