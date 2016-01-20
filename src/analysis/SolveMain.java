/**
 * The processor. Traverses the inputted flow graph using a temporal depth
 * first search to create PCs and pass them to the constraint solvers
 * using the argument.
 *
 * @author Scott Kausler
 */
package analysis;

import org.jgrapht.DirectedGraph;
import stringSymbolic.SymbolicEdge;

import java.util.*;

@SuppressWarnings({"Duplicates", "unchecked"})
public class SolveMain {

    public static void main(String[] args) {

        // set default options
        String fileName = "../graphs/compact1.ser";
        String solverName = "ejsa";
        boolean debug = false;

        if (args.length > 0) {

            // get command line arguments as a list
            LinkedList<String> list =
                    new LinkedList<String>(Arrays.asList(args));

            // remove option for processing
            String options = list.removeLast();

            if (options.startsWith("-")) {

                // print help/usage information
                if (options.contains("u") || options.contains("h")) {
                    System.out.println(
                            "Usage: <graph file> <solver name> (-<solvers>(s)" +
                            " <usage>u <debugMode>d)");
                    System.out.println(
                            "Example: sootOutput/graph.ser StrangerSolver -sd");
                }

                // print solver information
                if (options.contains("s")) {
                    System.out.println("Solvers:" +
                                       " EJSA" +
                                       " EStranger" +
                                       " EZ3Str" +
                                       " Concrete" +
                                       " Blank");
                }

                // set debug mode
                if (options.contains("d")) {
                    debug = true;
                }
            } else {

                // replace removed option
                list.addLast(options);
            }

            // get graph file name
            if (list.size() > 0) {
                fileName = list.removeFirst();
            }

            // get solver name
            if (list.size() > 0) {
                solverName = list.removeFirst();
            }
        }

        // get constraint graph from filename
        DirectedGraph<PrintConstraint, SymbolicEdge> graph;
        graph = CommandLineUtilities.getGraph(fileName);

        // create parser object from specified solver
        Parser parser = CommandLineUtilities.getParser(solverName);

        // if graph or parser not loaded, abort program
        if (graph == null || parser == null) {
            return;
        }

        // set debug mode
        parser.setDebug(debug);

        // run solver with specified graph and solver
        runSolver(graph, parser);
    }

    /**
     * Traverses the flow graph and solves PCs
     *
     * @param graph  The graph to traverse.
     * @param parser The solver to use.
     */
    public static void runSolver(DirectedGraph<PrintConstraint, SymbolicEdge>
                                         graph,
                                 Parser parser) {

        // initialize sets
        Set<PrintConstraint> removeSet = new HashSet<PrintConstraint>();
        Set<PrintConstraint> processedSet = new HashSet<PrintConstraint>();
        Set<PrintConstraint> ends = new HashSet<PrintConstraint>();
        Set<PrintConstraint> roots = new HashSet<PrintConstraint>();

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
        }

        // initialize list of constraints
        List<PrintConstraint> toBeAdded = new LinkedList<PrintConstraint>();

        // initialize sorted list of vertices
        List<PrintConstraint> vertices = new ArrayList<PrintConstraint>(graph.vertexSet());
        Collections.sort(vertices);

        //Topological progression...
        while (vertices.size() > 0) {

            // remove constraints from collections
            graph.removeAllVertices(removeSet);
            vertices.removeAll(removeSet);
            vertices.removeAll(toBeAdded);
            processedSet.removeAll(removeSet);

            // clear collections
            removeSet.clear();
            toBeAdded.clear();

            // for each vertex constraint
            for (PrintConstraint vertex : vertices) {

                // initialize source map
                Map<String, Integer> sourceMap = new HashMap<String, Integer>();

                // if vertex not in processed set
                if (!processedSet.contains(vertex)) {

                    // set flag
                    boolean readyToProcess = true;

                    // for each incoming edge of the current vertex
                    for (SymbolicEdge edge : graph.incomingEdgesOf(vertex)) {

                        // get source of incoming edge
                        PrintConstraint source =
                                (PrintConstraint) edge.getASource();

                        // if source vertex not in processed set
                        if (!processedSet.contains(source)) {

                            // unset flag
                            readyToProcess = false;

                            // break out of incoming edges loop
                            break;

                        } else {

                            // update source map with edge information
                            String edgeType = edge.getType();
                            int sourceId = source.getId();
                            sourceMap.put(edgeType, sourceId);
                        }
                    }

                    // if flag is still set
                    if (readyToProcess) {

                        // set the source map for the vertex
                        vertex.setSourceMap(sourceMap);

                        // current vertex queued for addition
                        toBeAdded.add(vertex);

                        // break out of the vertices loop
                        break;
                    }

                } else {

                    // set flag
                    boolean deleteNode = true;

                    // for each outgoing edge from the current vertex
                    for (SymbolicEdge edge : graph.outgoingEdgesOf(vertex)) {

                        // get the target vertex of the outgoing edge
                        PrintConstraint target = (PrintConstraint) edge.getATarget();

                        // if target vertex not in processed set
                        if (!processedSet.contains(target)) {

                            // unset flag
                            deleteNode = false;

                            // break out of outgoing edges loop
                            break;
                        }
                    }

                    // if flag set
                    if (deleteNode) {

                        // current vertex queued for removal
                        removeSet.add(vertex);

                        // remove vertex from parser
                        int vertexId = vertex.getId();
                        parser.remove(vertexId);
                    }
                }
            }

            // sort addition list before processing
            // Collections.sort(toBeAdded);

            // if addition list contains constraints
            if (toBeAdded.size() > 0) {

                // get first constraint in list
                PrintConstraint first = toBeAdded.get(0);

                // get constraint data
                String string = first.getSplitValue();
                String value = first.getActualVal();
                int id = first.getId();
                Map<String, Integer> sourceMap = first.getSourceMap();

                // if constraint is an end node
                if (ends.contains(first)) {

                    // add end
                    parser.addEnd(string, value, id, sourceMap);

                } else if (roots.contains(first)) {

                    // add root
                    parser.addRoot(string, value, id);

                } else {

                    // add operation
                    parser.addOperation(string, value, id, sourceMap);
                }

                // add constraint to processed list
                processedSet.add(first);
            }
        }

        // shut down parser
        parser.shutDown();
    }
}
