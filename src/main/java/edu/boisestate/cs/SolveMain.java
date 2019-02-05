/**
 * The processor. Traverses the inputted flow graph using a temporal depth first
 * search to create PCs and pass them to the constraint solvers using the
 * argument.
 *
 * @author Scott Kausler, Andrew Harris
 */
package edu.boisestate.cs;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.boisestate.cs.automatonModel.AutomatonModelManager;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.reporting.MCReporter;
import edu.boisestate.cs.reporting.Reporter;
import edu.boisestate.cs.reporting.SATReporter;
import edu.boisestate.cs.solvers.*;
import edu.boisestate.cs.util.LambdaVoid1;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.plaf.synth.SynthSeparatorUI;

@SuppressWarnings({"Duplicates", "unchecked"})
public class SolveMain {

    public static void main(String[] args) {

        Settings settings = CommandLine.processArgs(args);

        // ensure arguments processed properly before continuing
        if (settings == null) {
            return;
        }

        // initialize components object
        Components components = new Components();

        // load constraint graph
        loadGraph(components, settings);

        // load alphabet
        loadAlphabet(components, settings);

        // load solver
        loadSolver(components, settings);

        // if graph or parser not loaded, abort program
        if (components.getGraph() == null || components.getSolver() == null) {
            return;
        }

        // load parser
        loadParser(components, settings);

        // load reporter
        loadReporter(components, settings);

        // if reporter not loaded, abort program
        if (components.getReporter() == null) {
            return;
        }

        // run reporter
        components.getReporter().run();
    }

    private static void loadAlphabet(Components components, Settings settings) {

        // declare alphabet variable
        Alphabet alphabet = null;

        // if alphabet declared
        if (settings.getAlphabetDeclaration() != null) {

            // create alphabet from declaration
            alphabet = new Alphabet(settings.getAlphabetDeclaration());

            // if alphabet is not superset of minimal alphabet
            if (!alphabet.isSuperset(settings.getMinAlphabet())) {

                // reset alphabet to null
                alphabet = null;
            }
        }

        // if alphabet not already set
        if (alphabet == null) {

            // create alphabet from minimum required alphabet
            alphabet = new Alphabet(settings.getMinAlphabet());
        }

        // store alphabet
        components.setAlphabet(alphabet);

    }

    private static void loadGraph(Components components, final Settings settings) {
        // store graph as component
        LambdaVoid1<String> setMinAlphabet = new LambdaVoid1<String>() {
            @Override
            public void execute(String s) {
                settings.setMinAlphabet(s);
            }
        };
        components.setGraph(loadGraph(settings.getGraphFilePath(), setMinAlphabet));
    }

    public static DirectedGraph<PrintConstraint, SymbolicEdge> loadGraph(String graphPath, LambdaVoid1<String> setMinAlphabet) {

        // initialize graph object as null
        DirectedGraph<PrintConstraint, SymbolicEdge> graph =
                new DefaultDirectedGraph<>(SymbolicEdge.class);

        // create json object mapper
        ObjectMapper mapper = new ObjectMapper();

        // initialize json file object
        File graphFile = new File(graphPath);

        // initialize lists for processing
        Map<Integer, PrintConstraint> constraintMap = new HashMap<>();
        Map<PrintConstraint, List<Integer>> sourceConstraintMap =
                new HashMap<>();
        List<Map<String, Object>> edgeData = new LinkedList<>();

        try {

            // get graph data from json file
            Map<String, Object> graphData =
                    mapper.readValue(graphFile, Map.class);

            // add alphabet data to settings
            Map<String, Object> alphabetData =
                    (Map<String, Object>) graphData.get("alphabet");
            String minAlphabet = (String) alphabetData.get("declaration");
            setMinAlphabet.execute(minAlphabet);

            // get constraint data from graph data
            List<Map<String, Object>> vertexData =
                    (List<Map<String, Object>>) graphData.get("vertices");
            //System.out.println("VD " + vertexData);
            for (Map<String, Object> obj : vertexData) {

                // get constraint vertex data
                int id = (Integer) obj.get("id");
                String actualValue = (String) obj.get("actualValue");
                int num = (Integer) obj.get("num");
                long timeStamp;
                try {
                    timeStamp = (Long) obj.get("timeStamp");
                } catch (ClassCastException e) {
                    timeStamp = (Integer) obj.get("timeStamp");
                }
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
                //System.out.println("src " + source);
                //System.out.println("trgt " + target);
                // create symbolic edge in graph from data
                SymbolicEdge edge = graph.addEdge(source, target);
              // System.out.println(edge);
                edge.setType(type);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        // return graph
        return graph;
    }

    private static void loadParser(Components components, Settings settings) {

        // create and store parser as component
        components.setParser(new Parser(components.getSolver(),
                                        settings.getDebug()));

    }

    private static void loadReporter(Components components,
                                     Settings settings) {

        // get values from settings
        Settings.ReportType reportType = settings.getReportType();
        boolean debug = settings.getDebug();
        DirectedGraph<PrintConstraint, SymbolicEdge> graph =
                components.getGraph();
        Parser parser = components.getParser();
        ExtendedSolver solver = components.getSolver();

        // initialize reporter as null
        Reporter reporter = null;

        if (reportType == Settings.ReportType.MODEL_COUNT) {

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

        } else if (reportType == Settings.ReportType.SAT) {

            // create reporter from parameters
            reporter = new SATReporter(graph, parser, solver, debug);
        }

        // store reporter
        components.setReporter(reporter);
    }

    private static void loadSolver(Components components, Settings settings) {

        // get needed info from settings object
        Settings.SolverType selectedSolver = settings.getSolverType();
        Settings.ReportType reportType = settings.getReportType();
        int modelVersion = settings.getAutomatonModelVersion();
        int boundingLength = settings.getInitialBoundingLength();
        Alphabet alphabet = components.getAlphabet();

        // initialize extend solver as null
        ExtendedSolver solver = null;

        // create specified solver for parser
        if (selectedSolver == Settings.SolverType.BLANK) {

            solver = new BlankSolver();

        } else if (selectedSolver == Settings.SolverType.CONCRETE) {

            solver = new ConcreteSolver(alphabet, boundingLength);

        } else if (selectedSolver == Settings.SolverType.JSA) {

            // get model manager instance
            AutomatonModelManager modelManager =
                    AutomatonModelManager.getInstance(alphabet,
                                                      modelVersion,
                                                      boundingLength);

            if (reportType == Settings.ReportType.SAT) {

                solver = new AutomatonModelSolver(modelManager, boundingLength);

            } else if (reportType == Settings.ReportType.MODEL_COUNT) {

                solver = new MCAutomatonModelSolver(modelManager,
                                                    boundingLength);
            }

        }

        // store created solver
        components.setSolver(solver);
    }

}
