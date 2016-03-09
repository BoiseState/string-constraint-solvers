package edu.boisestate.cs.analysis;

import edu.boisestate.cs.solvers.ExtendedSolver;
import edu.boisestate.cs.stringSymbolic.SymbolicEdge;
import org.jgrapht.DirectedGraph;

import java.util.Map;

public class SATReporter extends Reporter {

    protected SATReporter(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                          Parser parser,
                          ExtendedSolver solver,
                          boolean debug) {

        super(graph, parser, solver, debug);
    }

    @Override
    protected void outputHeader() {

        // output header
        System.out.println("    ID\t" +
                           " SING\t" +
                           " TSAT\t" +
                           " FSAT\t" +
                           "DISJOINT");
    }


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

        // output constraint id
        stats.append(String.format("%6d\t", constraint.getId()));

        // determine if symbolic strings are singletons
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null ||
             solver.isSingleton(sourceMap.get("s1"), actualVal))) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

        // store symbolic string values
        solver.setLast(base, arg);

        // test if true branch is SAT
        parser.assertBooleanConstraint(true, constraint);
        if (solver.isSatisfiable(base)) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        // test if false branch is SAT
        parser.assertBooleanConstraint(false, constraint);
        if (solver.isSatisfiable(base)) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

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

        // add disjoint result to output string
        stats.append(disjoint);

        // revert symbolic string values
        solver.revertLastPredicate();

        // output stats
        System.out.println(stats);
    }
}
