package edu.boisestate.cs.reporting;

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

    protected Reporter(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                       Parser parser,
                       ExtendedSolver solver,
                       boolean debug) {

        this.graph = graph;
        this.parser = parser;
        this.debug = debug;
        this.solver = solver;
        operationsMap = new HashMap<>();
    }

    public void run() {

        // output header
        this.outputHeader();

        // initialize sets
        Set<PrintConstraint> removeSet = new HashSet<>();
        Set<PrintConstraint> processedSet = new HashSet<>();
        Set<PrintConstraint> ends = new HashSet<>();
        Set<PrintConstraint> roots = new HashSet<>();

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

                // add end
                boolean isBoolFunc = parser.addEnd(constraint);

                if (isBoolFunc) {
                    this.calculateStats(constraint);
                }

            }
            // if constraint is root node
            else if (roots.contains(constraint)) {

                // add root
                String init = parser.addRoot(constraint);

                // add initialization operation
                String[] ops = new String[] {init};
                this.operationsMap.put(constraintId, ops);

            }
            // constraint is op node
            else {

                // add operation
                String operation = parser.addOperation(constraint);

                // get previous operations
                String[] prevOps = this.operationsMap.get(targetId);

                // create ops array for current operation
                String[] currentOps = Arrays.copyOf(prevOps, prevOps.length + 1);
                currentOps[currentOps.length - 1] = operation;

                // add operations to map
                this.operationsMap.put(constraintId, currentOps);
            }
        }

        // shut down solver
        solver.shutDown();

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

    protected void addBooleanOperation(int base, int arg, String constName) {

        if (constName.equals("isEmpty")) {

            // update base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for current operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            newBaseOps[newBaseOps.length - 1] =
                    String.format("<S:%d>.isEmpty()", base);

            // add operations to map
            this.operationsMap.put(base, newBaseOps);

        } else {

            // get base constraint operations
            String[] baseOps = this.operationsMap.get(base);

            // create ops array for base operation
            String[] newBaseOps = Arrays.copyOf(baseOps, baseOps.length + 1);
            if (solver.isSingleton(arg)) {
                newBaseOps[newBaseOps.length - 1] =
                        String.format("<S:%d>.%s(\\\"%s\\\")",
                                      base,
                                      constName,
                                      Parser.actualVals.get(arg));
            } else {
                newBaseOps[newBaseOps.length - 1] =
                        String.format("<S:%d>.%s(<S:%d>)",
                                      base,
                                      constName,
                                      arg);
            }

            // update base operations
            this.operationsMap.put(base, newBaseOps);

            // get arg constraint operations
            String[] argOps = this.operationsMap.get(arg);

            // create ops array for arg operation
            String[] newArgOps = Arrays.copyOf(argOps, argOps.length + 1);
            newArgOps[newArgOps.length - 1] =
                    String.format("<S:%d>.%s(<S:%d>)", base, constName, arg);

            // update arg operations
            this.operationsMap.put(arg, newArgOps);

        }
    }

    protected abstract void outputHeader();

    protected abstract void calculateStats(PrintConstraint constraint);
}
