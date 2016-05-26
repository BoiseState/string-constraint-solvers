package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;

/**
 *
 */
public class AssertContainsOther
        extends Operation {

    private final AutomatonModel containedModel;
    private final AutomatonModelFactory modelFactory;

    /**
     * Constructs operation for true contains string operation.
     *
     * @param containedModel
     *         The model representing the contained string.
     * @param modelFactory
     *         The model factory for producing any string automata.
     */
    public AssertContainsOther(AutomatonModel containedModel,
                               AutomatonModelFactory modelFactory) {
        this.containedModel = containedModel;
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

        // create any string models
        AutomatonModel anyString1 = this.modelFactory.createAnyString();
        AutomatonModel anyString2 = this.modelFactory.createAnyString();

        // concatenate with contained model
        AutomatonModel x = anyString1.concatenate(this.containedModel)
                                     .concatenate(anyString2);

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
