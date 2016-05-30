package edu.boisestate.cs.automaton.operations;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelFactory;

/**
 *
 */
public class AssertNotContainsOther
        extends Operation {

    private final AutomatonModelFactory modelFactory;
    private final AutomatonModel notContainedModel;

    public AssertNotContainsOther(AutomatonModel notContainedModel,
                                  AutomatonModelFactory modelFactory) {
        this.notContainedModel = notContainedModel;
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
        AutomatonModel x = anyString1.concatenate(this.notContainedModel)
                                     .concatenate(anyString2);

        // return subtraction from model
        return model.minus(x);
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
