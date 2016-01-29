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
        DirectedGraph<PrintConstraint, SymbolicEdge> graph;
        graph = getGraph(fileName);

        // get solver from solver name
        ExtendedSolver solver = getSolver(solverName);

        // if graph or parser not loaded, abort program
        if (graph == null || solver == null) {
            return;
        }

        // create parser from solver and graph
        Parser parser = new Parser(solver, graph, debug);

        // run solver with specified graph and solver
        parser.runSolver();
    }

    static DirectedGraph<PrintConstraint, SymbolicEdge> getGraph
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

    static ExtendedSolver getSolver(String solverName) {

        // convert solver name to lowercase
        String lc = solverName.toLowerCase();

        // initialize extend solver as null
        ExtendedSolver solver = null;

        // create specified solver for parser
        if (lc.equals("blank")) {

            solver = new BlankSolver();

        } else if (lc.equals("ez3str") ||
                   lc.equals("z3str") ||
                   lc.equals("ez3strsolver") ||
                   lc.equals("z3strsolver")) {

            solver = new EZ3Str(5000,
                                "/usr/local/bin/Z3-str/Z3-str.py",
                                "str",
                                "tempZ3Str");

        } else if (lc.equals("estranger") ||
                   lc.equals("estrangersolver") ||
                   lc.equals("stranger") ||
                   lc.equals("strangersolver")) {

            solver = new EStranger();

        } else if (lc.equals("ejsa") ||
                   lc.equals("ejsasolver") ||
                   lc.equals("jsa") ||
                   lc.equals("jsasolver")) {

            solver = new EJSASolver();

        } else if (lc.equals("concrete")) {

            solver = new ConcreteSolver();

        }

        // return created parser
        return solver;
    }

}
