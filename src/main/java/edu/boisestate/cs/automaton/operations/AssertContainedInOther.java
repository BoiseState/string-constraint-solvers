package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;

/**
 *
 */
public class AssertContainedInOther
        extends Operation{

    private final AutomatonModel containingModel;

    /**
     * Constructs operation for true contained in other string.
     * @param containingModel The model representing the string which containing string.
     */
    public AssertContainedInOther(AutomatonModel containingModel) {
        this.containingModel = containingModel;
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
        return "containedIn";
    }
}
