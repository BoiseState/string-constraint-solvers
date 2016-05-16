/**
 * The processor. Traverses the inputted flow graph using a temporal depth first
 * search to create PCs and pass them to the constraint solvers using the
 * argument.
 *
 * @author Scott Kausler
 */
package edu.boisestate.cs;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.boisestate.cs.analysis.MCReporter;
import edu.boisestate.cs.analysis.Reporter;
import edu.boisestate.cs.analysis.SATReporter;
import edu.boisestate.cs.automaton.AutomatonModelFactory;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.*;
import org.apache.commons.cli.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"Duplicates", "unchecked"})
public class SolveMain {

    public static final int BOUND = 10;

    public static void main(String[] args) {

        Settings settings = processArgs(args);

        // ensure arguments processed properly before continuing
        if (settings == null) {
            return;
        }

        // get constraint graph from filename
        DirectedGraph<PrintConstraint, SymbolicEdge> graph;
        graph = getGraph(settings.getGraphFilePath());

        // get solver from solver name
        ExtendedSolver solver = getSolver(settings);

        // if graph or parser not loaded, abort program
        if (graph == null || solver == null) {
            return;
        }

        // get parser
        Parser parser = getParser(solver, settings.getDebug());

        // get reporter
        Reporter reporter = getReporter(settings.getReporter(),
                                        graph,
                                        parser,
                                        solver,
                                        settings.getDebug());

        // if reporter not loaded, abort program
        if (reporter == null) {
            return;
        }

        // run solver with specified graph and solver
        reporter.run();
    }

    private static DirectedGraph<PrintConstraint, SymbolicEdge> getGraph
            (String filePath) {

        // initialize graph object as null
        DirectedGraph<PrintConstraint, SymbolicEdge> graph =
                new DefaultDirectedGraph<>(SymbolicEdge.class);

        // create json object mapper
        ObjectMapper mapper = new ObjectMapper();

        // initialize json file object
        File graphFile = new File(filePath);

        // initialize lists for processing
        Map<Integer, PrintConstraint> constraintMap = new HashMap<>();
        Map<PrintConstraint, List<Integer>> sourceConstraintMap =
                new HashMap<>();
        List<Map<String, Object>> edgeData = new LinkedList<>();

        try {

            // get constraint data from json file
            List<Map<String, Object>> vertexData =
                    mapper.readValue(graphFile, List.class);

            for (Map<String, Object> obj : vertexData) {

                // get constraint vertex data
                int id = (Integer) obj.get("id");
                String actualValue = (String) obj.get("actualValue");
                int num = (Integer) obj.get("num");
                long timeStamp = (Long) obj.get("timeStamp");
                int type = (Integer) obj.get("type");
                String value = (String) obj.get("value");

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

                // get incoming edges
                List<Map<String, Object>> incomingEdges =
                        (List<Map<String, Object>>) obj.get("incomingEdges");

                for (Map<String, Object> incomingEdge : incomingEdges) {

                    // add to edge list
                    incomingEdge.put("target", id);
                    edgeData.add(incomingEdge);
                }
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

            for (Map<String, Object> obj : edgeData) {

                // get symbolic edge data
                int sourceId = (Integer) obj.get("source");
                PrintConstraint source = constraintMap.get(sourceId);
                int targetId = (Integer) obj.get("target");
                PrintConstraint target = constraintMap.get(targetId);
                String type = (String) obj.get("type");

                // create symbolic edge in graph from data
                SymbolicEdge edge = graph.addEdge(source, target);
                edge.setType(type);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        return graph;
    }

    private static Parser getParser(ExtendedSolver solver, boolean debug) {
        return new Parser(solver, debug);
    }

    private static Reporter getReporter(Settings.Reporter reportType,
                                        DirectedGraph<PrintConstraint,
                                                SymbolicEdge> graph,
                                        Parser parser,
                                        ExtendedSolver solver,
                                        boolean debug) {

        // initialize reporter as null
        Reporter reporter = null;

        if (reportType == Settings.Reporter.MODEL_COUNT) {

            // ensure solver is model count solver
            if (solver instanceof ModelCountSolver) {

                // cast solver
                ModelCountSolver mcSolver = (ModelCountSolver) solver;

                // create reporter from parameters
                reporter = new MCReporter(graph,
                                          parser,
                                          solver,
                                          debug,
                                          mcSolver);
            }

        } else if (reportType == Settings.Reporter.SAT) {

            // create reporter from parameters
            reporter = new SATReporter(graph, parser, solver, debug);
        }

        // return reporter
        return reporter;
    }

    private static ExtendedSolver getSolver(Settings settings) {

        // get needed info from settings object
        Settings.Solver selectedSolver = settings.getSolver();
        Settings.Reporter reporter = settings.getReporter();
        int modelVersion = settings.getAutomatonModelVersion();
        int boundingLength = settings.getInitialBoundingLength();

        // initialize extend solver as null
        ExtendedSolver solver = null;

        // create specified solver for parser
        if (selectedSolver == Settings.Solver.BLANK) {

            solver = new BlankSolver();

        } else if (selectedSolver == Settings.Solver.CONCRETE) {

            solver = new ConcreteSolver(2);

        } else if (selectedSolver == Settings.Solver.STRANGER) {

            solver = new EStranger();

        } else if (selectedSolver == Settings.Solver.Z3) {

            solver = new EZ3Str(5000,
                                "/usr/local/bin/Z3-str/Z3-str.py",
                                "str",
                                "tempZ3Str");

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 1 &&
                   reporter == Settings.Reporter.SAT) {

            solver = new UnboundedEJSASolver(boundingLength);

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 1 &&
                   reporter == Settings.Reporter.MODEL_COUNT) {

            solver = new UnboundedMCJSASolver(boundingLength);

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 2 &&
                   reporter == Settings.Reporter.SAT) {

            solver = new BoundedEJSASolver(boundingLength);

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 2 &&
                   reporter == Settings.Reporter.MODEL_COUNT) {

            solver = new BoundedMCJSASolver(boundingLength);

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 3 &&
                   reporter == Settings.Reporter.SAT) {

            // get model factory
            AutomatonModelFactory factory =
                    AutomatonModelFactory.getInstance(modelVersion);

            solver = new AggregateEJSASolver(factory, boundingLength);

        } else if (selectedSolver == Settings.Solver.JSA &&
                   modelVersion == 3 &&
                   reporter == Settings.Reporter.MODEL_COUNT) {

            // get model factory
            AutomatonModelFactory factory =
                    AutomatonModelFactory.getInstance(modelVersion);

            solver = new AggregateMCJSASolver(factory, boundingLength);

//        } else if (selectedSolver == Settings.Solver.JSA &&
//                   modelVersion == 4 &&
//                   reporter == Settings.Reporter.SAT) {
//
//            solver = new FourthEJSASolver(boundingLength);
//
//        } else if (selectedSolver == Settings.Solver.JSA &&
//                   modelVersion == 4 &&
//                   reporter == Settings.Reporter.MODEL_COUNT) {
//
//            solver = new FourthMCJSASolver(boundingLength);
//
        }

        // return created parser
        return solver;
    }

    private static boolean isValidGraphFile(String filePath) {
        File graphFile = new File(filePath);
        return graphFile.exists() && filePath.endsWith(".json");
    }

    private static void printHelp(Options options) {

        // create formatter
        HelpFormatter formatter = new HelpFormatter();

        formatter.setSyntaxPrefix("USAGE:\n");

        String appClass = "java " + SolveMain.class.getName();

        String header = "\nRun string constraint solver on specified control" +
                        " flow graph. The default string constraint solver " +
                        "is " +
                        Settings.Solver.DEFAULT +
                        ". The default reporter is " +
                        Settings.Reporter.DEFAULT +
                        ".\n\nOPTIONS:\n";

        String footer = "\nSee the code repository at https://github.com/" +
                        "BoiseState/string-constraint-solvers for more " +
                        "details.\n";

        formatter.printHelp(appClass + " <Graph File>",
                            header,
                            options,
                            footer,
                            true);
    }

    private static Settings processArgs(String[] args) {

        // create command line option objects
        Option solver = Option.builder("s")
                              .longOpt("solver")
                              .desc("The solver that will be used to solve " +
                                    "string constraints:\n" +
                                    Settings.Solver.BLANK +
                                    " - The blank solver used for testing" +
                                    ".\n" +
                                    Settings.Solver.CONCRETE +
                                    " - The concrete solver which provides an" +
                                    " oracle for other solvers.\n" +
                                    Settings.Solver.JSA +
                                    " - The Java String Analyzer solver which" +
                                    " comes from the dk.brics automaton and " +
                                    "string libraries.\n" +
                                    Settings.Solver.STRANGER +
                                    " - The STRANGER string constraint solver" +
                                    ".\n" +
                                    Settings.Solver.Z3 +
                                    " - The Z3 rule based string constraint " +
                                    "solver.\n\nThe default solver is " +
                                    Settings.Solver.DEFAULT +
                                    "\n")
                              .hasArg()
                              .numberOfArgs(1)
                              .argName("solver")
                              .build();
        Option reporter = Option.builder("r")
                                .longOpt("reporter")
                                .desc("The reporter used to gather " +
                                      "information for each string constraint" +
                                      ":\n" +
                                      Settings.Reporter.SAT +
                                      " - Reports on the satisfiability of " +
                                      "each string constraint in the " +
                                      "specified graph\n" +
                                      Settings.Reporter.MODEL_COUNT +
                                      " - Reports on the number and percent " +
                                      "of" +
                                      " string instances for each branch " +
                                      "leaving the string constraint, " +
                                      "includes satisfiability.\n\nThe " +
                                      "default reporter is " +
                                      Settings.Reporter.DEFAULT +
                                      "\n")
                                .hasArg()
                                .numberOfArgs(1)
                                .argName("reporter")
                                .build();

        Option debug = Option.builder("d")
                             .longOpt("debug")
                             .desc("Runs the solver framework in debug mode." +
                                   " Default value is false.")
                             .build();
        Option help = Option.builder("h")
                            .longOpt("help")
                            .desc("Display this message.")
                            .build();
        Option length = Option.builder("l")
                              .longOpt("length")
                              .desc("Initial bounding length of the " +
                                    "underlying finite state automata if " +
                                    "used for representing symbolic strings." +
                                    " Default value is " +
                                    Settings.DEFAULT_BOUNDING_LENGTH + ".")
                              .hasArg()
                              .numberOfArgs(1)
                              .argName("length")
                              .build();
        Option modelVersion = Option.builder("v")
                                    .longOpt("model-version")
                                    .desc("The version of the automaton model" +
                                          " used by the JSA string constraint" +
                                          " solver:\n1 - Unbounded " +
                                          "automaton model\n2 - Bounded " +
                                          "automaton model\n3 - Aggregate " +
                                          "bounded automata model\n4 - " +
                                          "Proposed accurate automaton model")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .argName("version")
                                    .build();

        // create options object
        Options options = new Options();
        options.addOption(debug);
        options.addOption(help);
        options.addOption(length);
        options.addOption(modelVersion);
        options.addOption(solver);
        options.addOption(reporter);

        // create parser
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;

        // attempt to parse command line arguments
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.format(
                    "Error processing command line arguments. Reason: %s",
                    e.getMessage());
            return null;
        }

        // create settings object
        Settings settings = new Settings();

        // process help option
        if (commandLine.hasOption("h")) {
            printHelp(options);
            return null;
        }

        // process additional arguments
        List<String> argsList = commandLine.getArgList();
        if (argsList.size() != 1 ||
            argsList.get(0) == null ||
            !isValidGraphFile(argsList.get(0))) {
            System.err.println(
                    "Invalid arguments have been specified, please consult " +
                    "usage documentation for help with the -h or --help " +
                    "option");

            return null;
        }

        // get vertices and edges files from unprocessed arguments list
        settings.setGraphFilePath(argsList.get(0));

        // process debug option
        if (commandLine.hasOption("d")) {
            settings.setDebug(true);
        }

        if (commandLine.hasOption("l")) {

            // set initial bounding length from option value
            String optionValue = commandLine.getOptionValue("l");
            int boundingLength = Integer.parseInt(optionValue);
            settings.setInitialBoundingLength(boundingLength);
        }

        if (commandLine.hasOption("v")) {

            // set initial bounding length from option value
            String optionValue = commandLine.getOptionValue("v");
            int version = Integer.parseInt(optionValue);
            settings.setAutomatonModelVersion(version);
        }

        // process solver option
        if (commandLine.hasOption("s")) {

            // get solver choice from option value
            String optionValue = commandLine.getOptionValue("s");
            String choice = optionValue.toLowerCase();

            if (choice.equals("blank")) {
                settings.setSolver(Settings.Solver.BLANK);
            } else if (choice.equals("concrete")) {
                settings.setSolver(Settings.Solver.CONCRETE);
            } else if (choice.equals("jsa")) {
                settings.setSolver(Settings.Solver.JSA);
            } else if (choice.equals("stranger")) {
                settings.setSolver(Settings.Solver.STRANGER);
            } else if (choice.equals("z3")) {
                settings.setSolver(Settings.Solver.Z3);
            } else {
                String errorMessage = String.format(
                        "The specified solver \"%s\" is not a recognized " +
                        "string constraint solver, please use the -h or " +
                        "--help option to see the valid solvers",
                        choice);
                System.err.println(errorMessage);
                return null;
            }
        }

        // process reporter
        if (commandLine.hasOption("r")) {

            // get reporter choice from option value
            String optionValue = commandLine.getOptionValue("r");
            String choice = optionValue.toLowerCase();

            if (choice.equals("sat")) {
                settings.setReporter(Settings.Reporter.SAT);
            } else if (choice.equals("model-count")) {
                settings.setReporter(Settings.Reporter.MODEL_COUNT);
            } else {

                String errorMessage = String.format(
                        "The specified solver \"%s\" is not a recognized " +
                        "reporter, please use the -h or --help option to see " +
                        "the valid reporters",
                        choice);
                System.err.println(errorMessage);
                return null;
            }
        }

        // return updated settings object
        return settings;
    }

}
