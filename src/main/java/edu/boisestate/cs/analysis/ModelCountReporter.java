package edu.boisestate.cs.analysis;

import edu.boisestate.cs.solvers.ExtendedSolver;
import edu.boisestate.cs.solvers.ModelCountSolver;
import edu.boisestate.cs.stringSymbolic.SymbolicEdge;
import org.jgrapht.DirectedGraph;

import java.util.Map;

public class ModelCountReporter extends Reporter {

    private final ModelCountSolver modelCountSolver;

    protected ModelCountReporter(DirectedGraph<PrintConstraint, SymbolicEdge>
                                         graph,
                                 Parser parser,
                                 ModelCountSolver solver,
                                 boolean debug) {

        super(graph, parser, solver, debug);

        this.modelCountSolver = solver;
    }

    @Override
    protected void outputHeader() {

        // output header
        System.out.println("    ID\t" +
                           " IN COUNT\t" +
                           "  T COUNT\t" +
                           "  F COUNT\t" +
                           "  OVERLAP");
    }

    @Override
    protected void calculateStats(PrintConstraint constraint) {


        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        StringBuilder stats = new StringBuilder();
        String actualVal = constraint.getActualVal();
        int base = sourceMap.get("t");

        // get id of second symbolic string if it exists
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        int initialCount = this.modelCountSolver.getModelCount(base);

        // store symbolic string values
        solver.setLast(base, arg);

        // test if true branch is SAT
        parser.assertBooleanConstraint(true, constraint);

        int trueModelCount = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        // test if false branch is SAT
        parser.assertBooleanConstraint(false, constraint);

        int falseModelCount = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // if actual execution did not produce either true or false
        if (!actualVal.equals("true") && !actualVal.equals("false")) {

            System.err.println("warning constraint detected without " +
                               "true/false value");
            return;
        }

        // determine result of actual execution
        boolean result = true;
        if (actualVal.equals("false")) {
            result = false;
        }

        // branches disjoint?
        parser.assertBooleanConstraint(result, constraint);

        // store symbolic string values
        solver.setLast(base, arg);

        parser.assertBooleanConstraint(!result, constraint);

        // set yes or no for disjoint branches
        int overlap = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // output stats
        System.out.format("%6d\t%9d\t%9d\t%9d\t%9d\n",
                          constraint.getId(),
                          initialCount,
                          trueModelCount,
                          falseModelCount,
                          overlap);
    }
}
