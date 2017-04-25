package edu.boisestate.cs.reporting;

import edu.boisestate.cs.BasicTimer;
import edu.boisestate.cs.Parser;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.PrintConstraintComparator;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.ExtendedSolver;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

@SuppressWarnings("Duplicates")
abstract public class Reporter {

    protected final DirectedGraph<PrintConstraint, SymbolicEdge> graph;
    protected final Parser parser;
    protected final boolean debug;
    protected final ExtendedSolver solver;
    protected final Map<Integer, String[]> operationsMap;
    protected final Map<Integer, Long> timerMap;

    protected Reporter(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                       Parser parser,
                       ExtendedSolver solver,
                       boolean debug) {

        this.graph = graph;
        this.parser = parser;
        this.debug = debug;
        this.solver = solver;
        operationsMap = new HashMap<>();
        timerMap = new HashMap<>();
    }

    public void run() {

        // output header
        this.outputHeader();

        // initialize sets
        Set<PrintConstraint> ends = new HashSet<>();
        Set<PrintConstraint> roots = new HashSet<>();
        Map<PrintConstraint, Set<PrintConstraint>> unfinishedOutEdges = new HashMap<>();
        Map<PrintConstraint, Set<PrintConstraint>> unfinishedInEdges = new HashMap<>();

        int maxId = 0;

        // populate root and end sets
        for (PrintConstraint constraint : graph.vertexSet()) {

            // if no in paths, node is a root node
            if (graph.inDegreeOf(constraint) == 0) {
                roots.add(constraint);
            }

            // if no out paths, node is an end node
            if (graph.outDegreeOf(constraint) == 0) {
                ends.add(constraint);
            }

            // update max id if necessary
            if (constraint.getId() > maxId) {
                maxId = constraint.getId();
            }
        }

        // set max id in parser
        this.parser.setMaxGraphId(maxId);

        // create priority queue structure for topological iteration
        Queue<PrintConstraint> queue =
                new PriorityQueue<>(1, new PrintConstraintComparator());

        // create topological iterator for graph
        TopologicalOrderIterator<PrintConstraint, SymbolicEdge> iterator =
                new TopologicalOrderIterator<>(this.graph, queue);

        // while processing constraints in topological order
        while (iterator.hasNext()) {

            // get constraint
            PrintConstraint constraint = iterator.next();
            int constraintId = constraint.getId();

//            System.out.printf("%d: %s\n", constraintId, constraint.getValue());

            // add to unfinished edges
            Set<PrintConstraint> unfinishedOutSet = new HashSet<>();
            for (SymbolicEdge e : graph.outgoingEdgesOf(constraint)) {
                PrintConstraint target = (PrintConstraint) e.getATarget();

                unfinishedOutSet.add(target);

                Set<PrintConstraint> unfinishedInSet = unfinishedInEdges.get(target);
                if (unfinishedInSet == null) {
                    unfinishedInSet = new HashSet<>();
                }
                unfinishedInSet.add(constraint);
                unfinishedInEdges.put(target, unfinishedInSet);
            }
            unfinishedOutEdges.put(constraint, unfinishedOutSet);

            // initialize constraint source map
            Map<String, Integer> sourceMap = new HashMap<>();

            // declare target id
            int targetId = 0;

            // for each incoming edge of the constraint
            for (SymbolicEdge edge : graph.incomingEdgesOf(constraint)) {

                // get the source constraint
                PrintConstraint source =
                        (PrintConstraint) edge.getASource();

                // add the edge type and source id to the source map
                sourceMap.put(edge.getType(), source.getId());

                // if source is target, set id
                if (edge.getType().equals("t")) {
                    targetId = source.getId();
                }
            }

            // set the constraint source map
            constraint.setSourceMap(sourceMap);

            // if constraint is an end node
            if (ends.contains(constraint)) {

//                System.out.printf("%d: Predicate\n", constraint.getId());

                // add end
                boolean isBoolFunc = parser.addEnd(constraint);

                if (isBoolFunc) {
//                    System.out.printf("%d: Predicate Calculating Stats\n", constraint.getId());

                    this.calculateStats(constraint);
                }

                finishEdges(unfinishedInEdges, unfinishedOutEdges, constraint);
            }
            // if constraint is root node
            else if (roots.contains(constraint)) {

//                System.out.printf("%d: Root\n", constraint.getId());

                // add root
                String init = parser.addRoot(constraint);

                long lastTime = BasicTimer.getRunTime();
                init += "{" + lastTime + "}";

                // add initialization operation
                String[] ops = new String[] {init};
                this.operationsMap.put(constraintId, ops);

                // add operation time to map
                timerMap.put(constraintId, lastTime);

                finishEdges(unfinishedInEdges, unfinishedOutEdges, constraint);
            }
            // constraint is op node
            else {

//                System.out.printf("%d: Operation\n", constraint.getId());

                // add operation
                String operation = parser.addOperation(constraint);

                // get previous operations
                String[] prevOps = this.operationsMap.get(targetId);

                long lastTime = BasicTimer.getRunTime();
                operation = String.format("[%s]%s{%d}", constraintId, operation, lastTime);

                // create ops array for current operation
                String[] currentOps = Arrays.copyOf(prevOps, prevOps.length + 1);
                currentOps[currentOps.length - 1] = operation;

                // add operations to map
                this.operationsMap.put(constraintId, currentOps);

                // get previous time
                long prevTime = 0;
                if (timerMap.containsKey(targetId)) {
                    prevTime = timerMap.get(targetId);
                }

                // add operation time to map
                long currTime = lastTime + prevTime;
                timerMap.put(constraintId, currTime);

                finishEdges(unfinishedInEdges, unfinishedOutEdges, constraint);
            }
        }

        // shut down solver
        solver.shutDown();
    }

    private void finishEdges(Map<PrintConstraint, Set<PrintConstraint>> inEdges,
                             Map<PrintConstraint, Set<PrintConstraint>> outEdges,
                             PrintConstraint vertex) {
        Set<PrintConstraint> parents = inEdges.get(vertex);
        inEdges.remove(vertex);
        if (parents != null) {
            for (PrintConstraint parent : parents) {
                Set<PrintConstraint> siblings = outEdges.get(parent);
                if (siblings == null) {
                    return;
                }
                siblings.remove(vertex);
                if (siblings.isEmpty()) {
                    siblings.remove(parent);
                    solver.remove(parent.getId());
                }
            }
        }
    }

    protected String joinStrings(Iterable<String> strings, String separator) {
        StringBuilder head = new StringBuilder();
        Iterator<String> iter = strings.iterator();
        head.append(iter.next());
        while (iter.hasNext()) {
            head.append(separator).append(iter.next());
        }
        return head.toString();
    }

    protected void addBooleanOperation(int base,
                                       int arg,
                                       String constName,
                                       int const_id,
                                       boolean argWasSingleton) {

        long accTime = 0;
        if (timerMap.containsKey(base)) {
            accTime = timerMap.get(base);
        }

        if (constName.equals("isEmpty")) {

            // update base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for current operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            newBaseOps[newBaseOps.length - 1] =
                    String.format("[%d]<S:%d>.isEmpty(){%s}", const_id, base, accTime);

            // add operations to map
            this.operationsMap.put(base, newBaseOps);

        } else {

            // get base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for base operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            if (argWasSingleton) {
                newBaseOps[newBaseOps.length - 1] =
                        String.format("[%d]<S:%d>.%s(\\\"%s\\\"){%d}",
                                      const_id,
                                      base,
                                      constName,
                                      Parser.actualVals.get(arg),
                                      accTime);
            } else {
                newBaseOps[newBaseOps.length - 1] =
                        String.format("[%d]<S:%d>.%s(<S:%d>){%d}",
                                      const_id,
                                      base,
                                      constName,
                                      arg,
                                      accTime);
            }

            // update base operations
            this.operationsMap.put(base, newBaseOps);

            // get arg constraint operations
            String[] argOps = this.operationsMap.get(arg);

            // create ops array for arg operation
            String[] newArgOps = Arrays.copyOf(argOps, argOps.length + 1);
            newArgOps[newArgOps.length - 1] =
                    String.format("[%d]<S:%d>.%s(<S:%d>){%d}", const_id, base, constName, arg, accTime);

            // update arg operations
            this.operationsMap.put(arg, newArgOps);

        }
    }

    protected abstract void outputHeader();

    protected abstract void calculateStats(PrintConstraint constraint);
}
