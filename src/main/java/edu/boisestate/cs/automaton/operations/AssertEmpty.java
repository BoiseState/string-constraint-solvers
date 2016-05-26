package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;

/**
 *
 */
public class AssertEmpty extends Operation {

    private final AutomatonModelFactory automatonModelFactory;

    /**
     * Constructs operation for true is empty string operation.
     *
     * @param automatonModelFactory
     *         The model factory for producing empty string automata.
     */
    public AssertEmpty(AutomatonModelFactory automatonModelFactory) {
        this.automatonModelFactory = automatonModelFactory;
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

        // intersect model with empty string
        AutomatonModel empty = this.automatonModelFactory.createEmptyString();
        return model.intersect(empty);
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
