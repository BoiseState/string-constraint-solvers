package edu.boisestate.cs.solvers;

import edu.boisestate.cs.automaton.AutomatonModelFactory;

import java.util.Set;

public class MCAutomatonModelSolver
        extends AutomatonModelSolver
        implements ModelCountSolver {

    public MCAutomatonModelSolver(AutomatonModelFactory modelFactory) {
        super(modelFactory);
    }

    public MCAutomatonModelSolver(AutomatonModelFactory modelFactory,
                                  int initialBound) {
        super(modelFactory, initialBound);
    }

    /**
     * Enumerates all possible values for a given symbolic string
     *
     * @param id
     *         the identifier of the symbolic string
     *
     * @return a set of all possible string values represented by the symbolic
     * string
     */
    @Override
    public Set<String> getAllVales(int id) {
        return null;
    }

    /**
     * Get the number of solutions represented by the symbolic string model.
     *
     * @param id
     *         the identifier of the symbolic string
     *
     * @return number of solutions for a given node in the graph
     */
    @Override
    public int getModelCount(int id) {
        return 0;
    }
}
