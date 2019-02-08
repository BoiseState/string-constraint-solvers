package edu.boisestate.cs.reporting;

import edu.boisestate.cs.BasicTimer;
import edu.boisestate.cs.Parser;
import edu.boisestate.cs.automatonModel.AcyclicWeightedAutomatonModel;
import edu.boisestate.cs.automatonModel.WeightedAutomatonModel;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.ExtendedSolver;
import edu.boisestate.cs.solvers.ModelCountSolver;
import edu.boisestate.cs.util.DotToGraph;
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
        long tTime, fTime, inMCTime, tMCTime, fMCTime = 0;

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
        boolean argIsSingleton = false;
        if(arg != -1) {
        	argIsSingleton = solver.isSingleton(sourceMap.get("s1"));
        }
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null || argIsSingleton)) {
            isSingleton = true;
        }

        //System.out.printf("%d Predicate: %s\n", base, constraint.getSplitValue().split("!!")[0]);

        //System.out.printf("Calculating IN MC for Constraint %d\n", base);

        long initialCount = this.modelCountSolver.getModelCount(base);
        inMCTime = BasicTimer.getRunTime();

        // store symbolic string values
        solver.setLast(base, arg);

        //System.out.printf("Asserting True Predicate for Constraint %d\n", base);

        // test if true branch is SAT
        parser.assertBooleanConstraint(true, constraint);
        tTime = BasicTimer.getRunTime();
        if (solver.isSatisfiable(base)) {
            trueSat = true;
        }

       // System.out.printf("Calculating T MC for Constraint %d\n", base);

        long trueModelCount = this.modelCountSolver.getModelCount(base);
        tMCTime = BasicTimer.getRunTime();

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        //System.out.printf("Asserting False Predicate for Constraint %d\n", base);

        // test if false branch is SAT
        parser.assertBooleanConstraint(false, constraint);
        fTime = BasicTimer.getRunTime();
        if (solver.isSatisfiable(base)) {
            falseSat = true;
        }

       // System.out.printf("Calculating F MC for Constraint %d\n", base);

        long falseModelCount = this.modelCountSolver.getModelCount(base);
        fMCTime = BasicTimer.getRunTime();

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

        //System.out.printf("Asserting Predicate to determine disjoint branches for Constraint %d\n", base);

        // branches disjoint?
        parser.assertBooleanConstraint(result, constraint);

        // update accumulated timer for base
        long prevTime = 0;
        if (timerMap.containsKey(base)) {
            prevTime = timerMap.get(base);
        }
        long lastTime = BasicTimer.getRunTime();
        timerMap.put(base, lastTime + prevTime);

        // update accumulated timer for arg
        prevTime = 0;
        if (timerMap.containsKey(arg)) {
            prevTime = timerMap.get(arg);
        }
        timerMap.put(arg, lastTime + prevTime);

        // store symbolic string values
        solver.setLast(base, arg);

      // System.out.printf("Asserting Negation of Predicate to determine disjoint branches for Constraint %d\n", base);

        parser.assertBooleanConstraint(!result, constraint);

        //System.out.printf("After assering for Constraint %d\n", base);
        // set yes or no for disjoint branches
        String disjoint = "yes";
        if (solver.isSatisfiable(base)) {
            disjoint = "no";
        }

       //System.out.printf("Calculating Disjoint MC for Constraint %d\n", base);

        // set yes or no for disjoint branches
        long overlap = this.modelCountSolver.getModelCount(base);

        // revert symbolic string values
        solver.revertLastPredicate();

        // get percentages
//        float truePercent = 100 * (float) trueModelCount / (float) initialCount;
//        float falsePercent = 100 * (float) falseModelCount / (float) initialCount;

        // get accumulated time
        long accTime = 0;
        if (timerMap.containsKey(base)) {
            accTime = timerMap.get(base);
        }

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
        // actual value
        columns.add(String.format("\\\"%s\\\"", constraint.getActualVal()));
        // is singleton?
        columns.add(String.valueOf(isSingleton));
        // true sat?
        columns.add(String.valueOf(trueSat));
        // false sat?
        columns.add(String.valueOf(falseSat));
        // disjoint?
        columns.add(String.format("%8s", disjoint));
        // accumulated time
        columns.add(String.valueOf(accTime));
        // id of initial model
        columns.add(String.valueOf(base));
        // initial model count
        columns.add(String.valueOf(initialCount));
        // initial model count time
        columns.add(String.valueOf(inMCTime));
        // true model count
        columns.add(String.valueOf(trueModelCount));
        // true model count time
        columns.add(String.valueOf(tMCTime));
        // true predicate time
        columns.add(String.valueOf(tTime));
        // false model count
        columns.add(String.valueOf(falseModelCount));
        // false model count time
        columns.add(String.valueOf(fMCTime));
        // false predicate time
        columns.add(String.valueOf(fTime));
        // overlap count
        columns.add(String.valueOf(overlap));
        // previous operations
        columns.add(ops);

        // generate row string
        String row = joinStrings(columns, "\t");

        // output row
        System.out.println(row);
        
//        System.out.println(((AcyclicWeightedAutomatonModel) solver.getValue(base)).getAutomaton());
//        System.out.println(((AcyclicWeightedAutomatonModel) solver.getValue(arg)).getAutomaton());
//        System.exit(2);
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
        headers.add("ACC IN TIME");
        headers.add("IN ID");
        headers.add("IN COUNT");
        headers.add("IN TIME");
        headers.add("T COUNT");
        headers.add("T MC TIME");
        headers.add("T PRED TIME");
        headers.add("F COUNT");
        headers.add("F MC TIME");
        headers.add("F PRED TIME");
        headers.add("OVERLAP");
        headers.add("PREV OPS");

        // generate headers string
        String header = joinStrings(headers, "\t");

        // output header
        System.out.println(header);
    }
}
