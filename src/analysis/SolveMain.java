/**
 * The processor. Traverses the inputted flow graph using a temporal depth
 * first search to create PCs and pass them to the constraint solvers
 * using the argument.
 *
 * @author Scott Kausler
 */
package analysis;

import extendedSolvers.*;
import org.jgrapht.DirectedGraph;
import stringSymbolic.SymbolicEdge;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
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
        DirectedGraph<PrintConstraint, SymbolicEdge> graph = GetGraph(fileName);

        // create parser object from specified solver
        Parser parser = getParser(solverName);

        // if graph or parser not loaded, abort program
        if (graph == null || parser == null) {
            return;
        }

        // set debug mode
        parser.setDebug(debug);

        // run solver with specified graph and solver
        runSolver(graph, parser);
    }

    private static Parser getParser(String solverName) {

        // convert solver name to lowercase
        String lc = solverName.toLowerCase();

        // initialize extened solver as null
        ExtendedSolver solver = null;

        // create specified solver for parser
        if (lc.equals("blank")) {

            solver = new BlankSolver();

        } else if (lc.equals("ez3str")) {

            solver = new EZ3Str(5000,
                                "/usr/local/bin/Z3-str/Z3-str.py",
                                "str",
                                "tempZ3Str");

        } else if (lc.equals("estranger")) {

            solver = new EStranger();

        } else if (lc.equals("ejsa")) {

            solver = new EJSASolver();

        } else if (lc.equals("concrete")) {

            solver = new ConcreteSolver();

        }

        // return created parser
        return new Parser(solver);
    }

    private static DirectedGraph<PrintConstraint, SymbolicEdge> GetGraph
            (String fileName) {

        // initialize graph object as null
        DirectedGraph<PrintConstraint, SymbolicEdge> graph = null;

        try {
            // get object stream from filename
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            FileInputStream fin = new FileInputStream(raf.getFD());
            ObjectInputStream in = new ObjectInputStream(fin);

            // get graph object from stream
            graph = (DirectedGraph<PrintConstraint, SymbolicEdge>)
                    in.readObject();

            // close stream objects
            in.close();
            fin.close();
            raf.close();

        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Graph not found");
            c.printStackTrace();
        }

        return graph;
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

            // for each vertex
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

            // Collections.sort(toBeAdded);
            if (toBeAdded.size() > 0) {
                PrintConstraint first = toBeAdded.get(0);
                if (ends.contains(first)) {
                    parser.addEnd(first.getSplitValue(),
                                  first.getActualVal(),
                                  first.getId(),
                                  first.getSourceMap());
                } else if (roots.contains(first)) {
                    parser.addRoot(first.getSplitValue(),
                                   first.getActualVal(),
                                   first.getId());
                } else {
                    parser.addOperation(first.getSplitValue(),
                                        first.getActualVal(),
                                        first.getId(),
                                        first.getSourceMap());
                }
                processedSet.add(first);
            }
        }

        // shut down parser
        parser.shutDown();
    }
}
