package edu.boisestate.cs.reporting;

import edu.boisestate.cs.Parser;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.ExtendedSolver;
import edu.boisestate.cs.solvers.ModelCountSolver;
import org.jgrapht.DirectedGraph;

import java.util.*;

public class MCReporter
        extends Reporter {

    private final ModelCountSolver modelCountSolver;

    public MCReporter(DirectedGraph<PrintConstraint, SymbolicEdge>
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

        // get percentages
        float truePercent = 100 * (float) trueModelCount / (float) initialCount;
        float falsePercent =
                100 * (float) falseModelCount / (float) initialCount;

        // get constraint function name
        String constName = constraint.getSplitValue().split("!!")[0];

        if (constName.equals("isEmpty")) {

            // update base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for current operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            newBaseOps[newBaseOps.length - 1] = "<this>.isEmpty()";

            // add operations to map
            this.operationsMap.put(base, newBaseOps);

        } else {

            // get base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for base operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            newBaseOps[newBaseOps.length - 1] =
                    String.format("<this>.%s(<String>)", constName);

            // update base operations
            this.operationsMap.put(base, newBaseOps);

            // get arg constraint operations
            String[] argOps = this.operationsMap.get(arg);

            // create ops array for arg operation
            String[] newArgOps = Arrays.copyOf(argOps, argOps.length + 1);
            newArgOps[newArgOps.length - 1] =
                    String.format("<String>.%s(<this>)", constName);

            // update arg operations
            this.operationsMap.put(arg, newArgOps);

        }

        // get operations
        String[] opsArray = this.operationsMap.get(base);
        String ops = joinStrings(Arrays.asList(opsArray), " -> ");

        // gather column data in list
        List<String> columns = new ArrayList<>();
        // id
        columns.add(String.valueOf(constraint.getId()));
        // actual value
        columns.add(String.format("\"%s\"", constraint.getActualVal()));
        // is singleton?
        columns.add(String.valueOf(isSingleton));
        // true sat?
        columns.add(String.valueOf(trueSat));
        // false sat?
        columns.add(String.valueOf(falseSat));
        // disjoint?
        columns.add(String.format("%8s", disjoint));
        // id of initial model
        columns.add(String.valueOf(base));
        // initial model count
        columns.add(String.valueOf(initialCount));
        // true model count
        columns.add(String.valueOf(trueModelCount));
        // true model count percent
        columns.add(String.format("%.1f", truePercent));
        // false model count
        columns.add(String.valueOf(falseModelCount));
        // false model count percent
        columns.add(String.format("%.1f", falsePercent));
        // overlap count
        columns.add(String.valueOf(overlap));
        // previous operations
        columns.add(ops);

        // generate row string
        String row = joinStrings(columns, "\t");

        // output row
        System.out.println(row);
    }

    private String joinStrings(Iterable<String> strings, String separator) {
        StringBuilder head = new StringBuilder();
        Iterator<String> iter = strings.iterator();
        head.append(iter.next());
        while (iter.hasNext()) {
            head.append(separator).append(iter.next());
        }
        return head.toString();
    }

    @Override
    protected void outputHeader() {

        // gather headers in list
        List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("ACT VAL");
        headers.add("SING");
        headers.add("TSAT");
        headers.add("FSAT");
        headers.add("DISJOINT");
        headers.add("IN ID");
        headers.add("IN COUNT");
        headers.add("T COUNT");
        headers.add("T PER");
        headers.add("F COUNT");
        headers.add("F PER");
        headers.add("OVERLAP");
        headers.add("PREV OPS");

        // generate headers string
        String header = joinStrings(headers, "\t");

        // output header
        System.out.println(header);
    }
}
