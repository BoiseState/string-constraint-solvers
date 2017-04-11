package edu.boisestate.cs.solvers;

import java.util.Set;

public interface ModelCountSolver {

    /**
     * Get the number of solutions represented by the symbolic string model.
     *
     * @param id
     *         the identifier of the symbolic string
     *
     * @return number of solutions for a given node in the graph
     */
    long getModelCount(int id);

//    /**
//     * Get a single string value if it exists from the solutions represented by
//     * the symbolic string.
//     *
//     * @param id
//     *         the identifier of the symbolic string
//     *
//     * @return the single string value if it exists, otherwise null.
//     */
//    String getSingleValue(int id);

    /**
     * Enumerates all possible values for a given symbolic string
     *
     * @param id
     *         the identifier of the symbolic string
     *
     * @return a set of all possible string values represented by the symbolic
     * string
     */
    Set<String> getAllVales(int id);


}
