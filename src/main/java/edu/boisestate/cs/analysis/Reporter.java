package edu.boisestate.cs.analysis;

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

    protected Reporter(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                       Parser parser,
                       ExtendedSolver solver,
                       boolean debug) {

        this.graph = graph;
        this.parser = parser;
        this.debug = debug;
        this.solver = solver;
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

            // initialize constraint source map
            Map<String, Integer> sourceMap = new HashMap<>();

            // for each incoming edge of the constraint
            for (SymbolicEdge edge : graph.incomingEdgesOf(constraint)) {

                // get the source constraint
                PrintConstraint source =
                        (PrintConstraint) edge.getASource();

                // add the edge type and source id to the source map
                sourceMap.put(edge.getType(), source.getId());
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

            } else if (roots.contains(constraint)) {

                // add root
                parser.addRoot(constraint);

            } else {

                // add operation
                parser.addOperation(constraint);
            }
        }

        // shut down solver
        solver.shutDown();

    }

    protected abstract void outputHeader();

    protected abstract void calculateStats(PrintConstraint constraint);
}
