/**
 * The processor. Traverses the inputted flow graph using a temporal depth
 * first search to create PCs and pass them to the constraint solvers
 * using the argument.
 *
 * @author Scott Kausler
 */
package edu.boisestate.cs.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.boisestate.cs.solvers.*;
import edu.boisestate.cs.stringSymbolic.SymbolicEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.*;
import java.util.*;

@SuppressWarnings({"Duplicates", "unchecked"})
public class SolveMain {

    public static final int BOUND = 10;

    public static void main(String[] args) {

        // set default options
        String fileName = "../graphs/beasties01.ser";
        String solverName = "ejsa";
        String reportType = "SAT";
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
                            "Usage: <graph file> <solver name> <report type> " +
                            "(-<solvers>(s) <usage>u <debugMode>d <report " +
                            "types>(r))");
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

                // print report information
                if (options.contains("r")) {
                    System.out.println("Report Types:" +
                                       " SAT" +
                                       " ModelCount");
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

            // get report type
            if (list.size() > 0) {
                reportType = list.removeFirst();
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

        // get parser
        Parser parser = getParser(solver, debug);

        // get reporter
        Reporter reporter =
                getReporter(reportType, graph, parser, solver, debug);

        // if reporter not loaded, abort program
        if (reporter == null) {
            return;
        }

        // run solver with specified graph and solver
        reporter.run();
    }

    static DirectedGraph<PrintConstraint, SymbolicEdge> getGraph
            (String fileName) {

        // initialize graph object as null
        DirectedGraph<PrintConstraint, SymbolicEdge> graph =
                new DefaultDirectedGraph<>(SymbolicEdge.class);

        // create json object mapper
        ObjectMapper mapper = new ObjectMapper();

        // get base file path for graph files
        String filePathBase = fileName.substring(0, fileName.lastIndexOf('.'));

        // initialize json file objects
        File edgesFile = new File(filePathBase + "_edges.json");
        File verticesFile = new File(filePathBase + "_vertices.json");

        // initialize lists for processing
        Map<Integer, PrintConstraint> constraintMap = new HashMap<>();
        Map<PrintConstraint, List<Integer>> sourceConstraintMap =
                new HashMap<>();

        try {

            // get constraint data from json file
            List<Map<String, Object>> vertexData =
                    mapper.readValue(verticesFile, List.class);

            for (Map<String, Object> obj : vertexData) {

                // get constraint vertex data
                int id = (Integer) obj.get("id");
                String actualValue = obj.get("actualValue").toString();
                int num = (Integer) obj.get("num");
                long timeStamp = (Long) obj.get("timeStamp");
                int type = (Integer) obj.get("type");
                String value = obj.get("value").toString();

                // create constraint from vertex data
                PrintConstraint constraint = new PrintConstraint(id,
                                                                 actualValue,
                                                                 num,
                                                                 timeStamp,
                                                                 type,
                                                                 value);

                // add constraint to map
                constraintMap.put(id, constraint);

                // get source constraint list and add to map
                List<Integer> sourceConstraints =
                        (List<Integer>) obj.get("sourceConstraints");
                sourceConstraintMap.put(constraint, sourceConstraints);
            }


            // set sourceConstraints for each constraint
            for (PrintConstraint constraint : sourceConstraintMap.keySet()) {

                // for each constraint id
                for (int id : sourceConstraintMap.get(constraint)) {

                    // get source constraint
                    PrintConstraint sourceConstraint = constraintMap.get(id);

                    // set source constraint as source for current constraint
                    constraint.setSource(sourceConstraint);
                }

                // add constraint to graph
                graph.addVertex(constraint);
            }

            // get edge data from json file
            List<Map<String, Object>> edgeData =
                    mapper.readValue(edgesFile, List.class);

            for (Map<String, Object> obj : edgeData) {

                // get symbolic edge data
                int sourceId = (Integer) obj.get("source");
                PrintConstraint source = constraintMap.get(sourceId);
                int targetId = (Integer) obj.get("target");
                PrintConstraint target = constraintMap.get(targetId);
                String type = obj.get("type").toString();

                // create symbolic edge in graph from data
                SymbolicEdge edge = graph.addEdge(source, target);
                edge.setType(type);
            }
//
        } catch (IOException i) {
            i.printStackTrace();
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

            solver = new EJSASolver(BOUND);

        } else if (lc.equals("concrete")) {

            solver = new ConcreteSolver(2);

        }

        // return created parser
        return solver;
    }

    private static Parser getParser(ExtendedSolver solver, boolean debug) {
        return new Parser(solver, debug);
    }

    private static Reporter getReporter(String reportType,
                                        DirectedGraph<PrintConstraint,
                                                SymbolicEdge> graph,
                                        Parser parser,
                                        ExtendedSolver solver,
                                        boolean debug) {

        // convert report type to lowercase
        String lc = reportType.toLowerCase();

        // initialize reporter as null
        Reporter reporter = null;

        if (lc.equals("modelcount") ||
            lc.equals("model count") ||
            lc.equals("mc") ||
            lc.equals("m c")) {

            // ensure solver is model count solver
            if (solver instanceof ModelCountSolver) {

                // cast solver
                ModelCountSolver mcSolver = (ModelCountSolver) solver;

                // create reporter from parameters
                reporter = new ModelCountReporter(graph,
                                                  parser,
                                                  mcSolver,
                                                  debug);
            }

        } else if (lc.equals("sat") ||
                   lc.equals("unsat") ||
                   lc.equals("satisfiable") ||
                   lc.equals("unsatisfiable") ||
                   lc.equals("satisfiability") ||
                   lc.equals("unsatisfiability")) {

            // create reporter from parameters
            reporter = new SATReporter(graph, parser, solver, debug);
        }

        // return reporter
        return reporter;
    }

}
