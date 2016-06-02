package edu.boisestate.cs.solvers;

import edu.boisestate.cs.automaton.AutomatonModel;
import edu.boisestate.cs.automaton.AutomatonModelManager;

import java.math.BigInteger;
import java.util.Set;

public class MCAutomatonModelSolver
        extends AutomatonModelSolver
        implements ModelCountSolver {

    public MCAutomatonModelSolver(AutomatonModelManager modelFactory) {
        super(modelFactory);
    }

    public MCAutomatonModelSolver(AutomatonModelManager modelFactory,
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

        // get model from id
        AutomatonModel model = this.symbolicStringMap.get(id);

        // return finite strings of model
        return this.modelManager.getFiniteStrings(model);
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

        // get model from id
        AutomatonModel model = this.symbolicStringMap.get(id);

        // get model count as big integer
        BigInteger count = this.modelManager.modelCount(model);

        // return model count as integer
        return count.intValue();
    }
}
