package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;

/**
 *
 */
public class Substring
        extends Operation {

    private final int start;
    private final int end;

    /**
     * Constructs substring operation.
     *
     * @param start
     *         The starting index for the substring operation.
     * @param end
     *         The ending index for the substring operation.
     */
    public Substring(int start, int end) {

        this.start = start;
        this.end = end;

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
