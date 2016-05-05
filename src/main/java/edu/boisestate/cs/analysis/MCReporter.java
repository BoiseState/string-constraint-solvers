package edu.boisestate.cs.analysis;

import edu.boisestate.cs.solvers.ExtendedSolver;
import edu.boisestate.cs.solvers.ModelCountSolver;
import org.jgrapht.DirectedGraph;

import java.util.Map;

public class MCReporter
        extends Reporter {

    private final ModelCountSolver modelCountSolver;

    protected MCReporter(DirectedGraph<PrintConstraint, SymbolicEdge>
                                 graph,
                         Parser parser,
                         ExtendedSolver extendedSolver,
                         boolean debug,
                         ModelCountSolver modelCountSolver) {

        super(graph, parser, extendedSolver, debug);

        this.modelCountSolver = modelCountSolver;
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

        // initialize boolean flags
        boolean isSingleton = false;
        boolean trueSat = false;
        boolean falseSat = false;

        // determine if symbolic strings are singletons
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null ||
             solver.isSingleton(sourceMap.get("s1"), actualVal))) {
            isSingleton = true;
        }

        int initialCount = this.modelCountSolver.getModelCount(base);

        // store symbolic string values
        solver.setLast(base, arg);

        // test if true branch is SAT
        parser.assertBooleanConstraint(true, constraint);
        if (solver.isSatisfiable(base)) {
            trueSat = true;
        }

        int trueModelCount = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        // test if false branch is SAT
        parser.assertBooleanConstraint(false, constraint);
        if (solver.isSatisfiable(base)) {
            falseSat = true;
        }

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
        String disjoint = "yes";
        if (solver.isSatisfiable(base)) {
            disjoint = "no";
        }

        // set yes or no for disjoint branches
        int overlap = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // output stats
        float truePercent = 100 * (float) trueModelCount / (float) initialCount;
        float falsePercent = 100 * (float) falseModelCount / (float) initialCount;
        System.out.format(
                "%6d\t%5b\t%5b\t%5b\t%8s\t%9d\t%9d\t%5.1f\t%9d\t%5.1f\t%9d\n",
                constraint.getId(),
                isSingleton,
                trueSat,
                falseSat,
                disjoint,
                initialCount,
                trueModelCount,
                truePercent,
                falseModelCount,
                falsePercent,
                overlap);
    }

    @Override
    protected void outputHeader() {

        // output header
        System.out.println("    ID\t" +
                           " SING\t" +
                           " TSAT\t" +
                           " FSAT\t" +
                           "DISJOINT\t" +
                           " IN COUNT\t" +
                           "  T COUNT\t" +
                           "T PER\t" +
                           "  F COUNT\t" +
                           "F PER\t" +
                           "  OVERLAP");
    }
}
