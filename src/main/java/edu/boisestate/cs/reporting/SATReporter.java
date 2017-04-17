package edu.boisestate.cs.reporting;

import edu.boisestate.cs.Parser;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.ExtendedSolver;
import org.jgrapht.DirectedGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SATReporter extends Reporter {

    public SATReporter(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                       Parser parser,
                       ExtendedSolver solver,
                       boolean debug) {

        super(graph, parser, solver, debug);
    }

    @Override
    protected void outputHeader() {

        // gather headers in list
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("SING");
        headers.add("TSAT");
        headers.add("FSAT");
        headers.add("DISJOINT");
        headers.add("PREV OPS");

        // generate headers string
        String header = joinStrings(headers, "\t");

        // output header
        System.out.println(header);
    }


    protected void calculateStats(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
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
        boolean argIsSingleton = solver.isSingleton(sourceMap.get("s1"));
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null || argIsSingleton)) {
            isSingleton = true;
        }

        // store symbolic string values
        solver.setLast(base, arg);

        // test if true branch is SAT
        parser.assertBooleanConstraint(true, constraint);
        if (solver.isSatisfiable(base)) {
            trueSat = true;
        }

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        // test if false branch is SAT
        parser.assertBooleanConstraint(false, constraint);
        if (solver.isSatisfiable(base)) {
            falseSat = true;
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

        // revert symbolic string values
        solver.revertLastPredicate();

        // get constraint function name
        String constName = constraint.getSplitValue().split("!!")[0];

        // add boolean operation to operation list
        addBooleanOperation(base, arg, constName, constraint.getId(), argIsSingleton);

        // get operations
        String[] opsArray = this.operationsMap.get(base);
        String ops = joinStrings(Arrays.asList(opsArray), " -> ");

        // gather column data in list
        List<String> columns = new ArrayList<>();
        // id
        columns.add(String.valueOf(constraint.getId()));
        // is singleton?
        columns.add(String.valueOf(isSingleton));
        // true sat?
        columns.add(String.valueOf(trueSat));
        // false sat?
        columns.add(String.valueOf(falseSat));
        // disjoint?
        columns.add(String.valueOf(disjoint));
        // previous operations
        columns.add(ops);

        // generate row string
        String row = joinStrings(columns, "\t");

        // output row
        System.out.println(row);

    }
}
