/**
 * The processor. Traverses the inputted flow graph using a temporal depth
 * first search to create PCs and pass them to the constraint solvers
 * using the argument.
 *
 * @author Scott Kausler
 */
package analysis;

import extendedSolvers.BlankSolver;
import extendedSolvers.ConcreteSolver;
import extendedSolvers.EStranger;
import extendedSolvers.EZ3Str;
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
                    System.out.println("Solvers: EJSA EStranger EZ3Str");
                }

                // set debug mode
                if (options.contains("d")) {
                    debug = true;
                }
            } else {
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

        // initialize parser as null
        Parser parser = null;

        // convert solver name to lowercase
        String lc = solverName.toLowerCase();

        // create parser for specified solver
        if (lc.equals("blanksolver")) {
            parser = new Parser(new BlankSolver());
        } else if (lc.equals("ez3str")) {
            parser = new Parser(new EZ3Str(5000,
                                           "/usr/local/bin/Z3-str/Z3-str.py",
                                           "str",
                                           "tempZ3Str"));
        } else if (lc.equals("strangersolver")) {
            parser = new Parser(new EStranger());
        } else if (lc.equals("jsasolver")) {
            parser = new Parser(new ConcreteSolver());
        } else if (lc.equals("concretesolver")) {
            parser = new Parser(new ConcreteSolver());
        }

        return parser;
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
     * @param graph The graph to traverse.
     * @param parser The solver to use.
     */
    public static void runSolver(DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                                 Parser parser) {

        // initialize sets
        Set<PrintConstraint> removeSet = new HashSet<PrintConstraint>();
        Set<PrintConstraint> processedSet = new HashSet<PrintConstraint>();
        Set<PrintConstraint> ends = new HashSet<PrintConstraint>();
        Set<PrintConstraint> roots = new HashSet<PrintConstraint>();

        // populate root and end sets
        for(PrintConstraint constraint : graph.vertexSet()){

            // if no in paths, node is a root node
            if (graph.inDegreeOf(constraint) == 0) {
                roots.add(constraint);
            }

            // if no out paths, node is an end node
            if (graph.outDegreeOf(constraint) == 0) {
                ends.add(constraint);
            }
        }

        LinkedList<PrintConstraint> toBeAdded = new LinkedList<PrintConstraint>();
        ArrayList<PrintConstraint> vertexSet = new ArrayList<PrintConstraint>(graph.vertexSet());
        Collections.sort(vertexSet);

        //Topological progression...
        while (vertexSet.size() > 0) {
            graph.removeAllVertices(removeSet);
            vertexSet.removeAll(removeSet);
            vertexSet.removeAll(toBeAdded);
            processedSet.removeAll(removeSet);
            removeSet = new HashSet<PrintConstraint>();

            toBeAdded = new LinkedList<PrintConstraint>();
            for (int i = 0; i < vertexSet.size(); i++) {
                PrintConstraint next = vertexSet.get(i);
                HashMap<String, Integer> sourceMap =
                        new HashMap<String, Integer>();
                if (!processedSet.contains(next)) {
                    boolean readyToProcess = true;
                    Iterator<SymbolicEdge> edgeIt =
                            graph.incomingEdgesOf(next).iterator();
                    while (edgeIt.hasNext()) {
                        SymbolicEdge edge = edgeIt.next();
                        PrintConstraint source =
                                (PrintConstraint) edge.getASource();
                        if (!processedSet.contains(source)) {
                            readyToProcess = false;
                            break;
                        } else {
                            sourceMap.put(edge.getType(), source.getId());
                        }
                    }
                    if (readyToProcess) {
                        next.setSourceMap(sourceMap);
                        toBeAdded.add(next);
                        break;
                    } else {
                    }
                } else {
                    Iterator<SymbolicEdge> edgeIt =
                            graph.outgoingEdgesOf(next).iterator();
                    boolean deleteNode = true;
                    while (edgeIt.hasNext()) {
                        SymbolicEdge edge = edgeIt.next();
                        PrintConstraint target =
                                (PrintConstraint) edge.getATarget();
                        if (!processedSet.contains(target)) {
                            deleteNode = false;
                            break;
                        }
                    }
                    if (deleteNode) {
                        removeSet.add(next);
                        parser.remove(next.getId());
                    }

                }
            }
            // Collections.sort(toBeAdded);
            if (toBeAdded.size() > 0) {
                PrintConstraint first = toBeAdded.getFirst();
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
        parser.shutDown();
    }
}
