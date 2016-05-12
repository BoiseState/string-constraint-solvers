package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;

/**
 *
 */
public class AssertEndsWith
        extends Operation {

    private final AutomatonModel endingModel;

    /**
     * Construct endsWith operation.
     *
     * @param endingModel
     *         The model representing the substring string which is a suffix of
     *         the string.
     */
    public AssertEndsWith(AutomatonModel endingModel) {
        this.endingModel = endingModel;
    }

    /**
     * Execute the symbolic operation on the provided automaton model.
     *
     * @param model
     *         The automaton model on which the operation will be performed.
     *
     * @return An automaton model that results from the execution of the
     * operation.
     */
    @Override
    public AutomatonModel execute(AutomatonModel model) {
        return null;
    }

    /**
     * Produces a string representation of the operation signature with
     * appropriate parameter values
     *
     * @return The string representation of the operation.
     */
    @Override
    public String toString() {
        return null;
    }
}
