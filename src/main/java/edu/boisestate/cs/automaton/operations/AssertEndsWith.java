package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;

/**
 *
 */
public class AssertEndsWith
        extends Operation {

    private final AutomatonModel endingModel;
    private final AutomatonModelFactory modelFactory;

    /**
     * Construct endsWith operation.
     *
     * @param endingModel
     *         The model representing the substring string which is a suffix of
     *         the string.
     * @param modelFactory
     *         The model factory for producing any string automata.
     */
    public AssertEndsWith(AutomatonModel endingModel,
                          AutomatonModelFactory modelFactory) {
        this.endingModel = endingModel;
        this.modelFactory = modelFactory;
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

        // create any string model
        AutomatonModel anyString = this.modelFactory.createAnyString();

        // concatenate with ending model
        AutomatonModel x = anyString.concatenate(this.endingModel);

        // return intersection with model
        return model.intersect(x);
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
