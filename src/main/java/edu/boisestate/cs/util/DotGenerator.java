package edu.boisestate.cs.util;

import edu.boisestate.cs.SolveMain;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DotGenerator {

    public static void main(String[] args) {

        String filepath = args[0];
        LambdaVoid1<String> printMinAlphabet = new LambdaVoid1<String>() {
            @Override
            public void execute(String s) {
                System.out.printf("Minimum Alphabet: %s\n", s);
            }
        };
        DirectedGraph<PrintConstraint, SymbolicEdge> graph =
                SolveMain.loadGraph(filepath, printMinAlphabet);

        String dotDirPath = String.format("temp");
        File dotDir = new File(dotDirPath);
        if (dotDir.exists() && dotDir.isFile()) {
            dotDir.delete();
            dotDir.mkdir();
        } else if (!dotDir.exists()) {
            dotDir.mkdir();
        }

        String graphFileName = new File(filepath).getName();
        int extIndex = graphFileName.lastIndexOf('.');
        graphFileName = graphFileName.substring(0, extIndex);

        String graphDotFilepath = String.format("%s/%s-graph",
                                                dotDirPath,
                                                graphFileName);
        generateGraphFile(graph, graphDotFilepath);
    }

    /**
     * Creates a text file depicting the graph.
     *
     * @param graph
     *         The graph to be described.
     * @param filePath
     */
    public static void generateGraphFile(DirectedGraph<PrintConstraint,
            SymbolicEdge> graph, String filePath) {

        try {
            Writer dotWriter = new FileWriter(filePath + ".dot");

            DOTExporter<PrintConstraint, SymbolicEdge> dotExporter =
                    new DOTExporter<>(new ConstraintIdProvider(),
                                      new ConstraintNameProvider(),
                                      new EdgeInfoProvider());

            dotExporter.export(dotWriter, graph);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

//        // use dot on file to produce png
//        Runtime rt = Runtime.getRuntime();
//
//        try {
//            rt.exec("dot -Tsvg " + filePath + ".dot -o " + filePath + ".svg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
