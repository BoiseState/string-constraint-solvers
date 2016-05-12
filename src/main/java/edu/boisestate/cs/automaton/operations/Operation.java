package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;

/**
 *
 */
abstract public class Operation {

    /**
     * Execute the symbolic operation on the provided automaton model.
     *
     * @param model
     *         The automaton model on which the operation will be performed.
     *
     * @return An automaton model that results from the execution of the
     * operation.
     */
    abstract public AutomatonModel execute(AutomatonModel model);

    /**
     * Produces a string representation of the operation signature with
     * appropriate parameter values
     *
     * @return The string representation of the operation.
     */
    @Override
    abstract public String toString();
}
