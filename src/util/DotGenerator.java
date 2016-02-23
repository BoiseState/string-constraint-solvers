package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.URL;

import old.ConstraintIdProvider;
import old.ConstraintNameProvider;
import old.EdgeInfoProvider;
import old.SolveMain;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;

import stringSymbolic.SymbolicEdge;
import analysis.PrintConstraint;

public class DotGenerator {
	
	public static void main(String[] args)
            throws Exception {

        URL main = DotGenerator.class.getResource("DotGenerator.class");
        if (!"file".equalsIgnoreCase(main.getProtocol())) {
            String errMsg = "DotGenerator class is not stored in a file.";
            throw new IllegalStateException(errMsg);
        }
        File classFile = new File(main.getPath());
        File projRootDirFile = classFile.getParentFile()
                                        .getParentFile()
                                        .getParentFile();

        String filepath = args[0]; //String.format("%s/graphs/beasties01.ser", projRootDirFile.getAbsolutePath());
        DirectedGraph<PrintConstraint, SymbolicEdge> graph = null;
        try {
//            System.out.println(filepath);
            RandomAccessFile raf = new RandomAccessFile(filepath, "rw");
            FileInputStream fin = new FileInputStream(raf.getFD());
            ObjectInputStream in = new ObjectInputStream(fin);
            graph =
                    (DirectedGraph<PrintConstraint, SymbolicEdge>) in
                            .readObject();
            in.close();
            fin.close();
            raf.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Graph not found");
            c.printStackTrace();
            return;
        }

            String dotDirPath = String.format("%s/graphs/dot",projRootDirFile);
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

            String graphDotFilepath = String.format("%s/%s-graph.dot",
                                                    dotDirPath,
                                                    graphFileName);
            generateGraphFile(graph, graphDotFilepath);
        }

/**
 * Creates a text file depicting the graph.
 *
 * @param graph    The graph to be described.
 * @param filepath
 */
public static void generateGraphFile(DirectedGraph<PrintConstraint,
        SymbolicEdge> graph, String filepath) {

    try {
        Writer dotWriter = new FileWriter(filepath);

        DOTExporter<PrintConstraint, SymbolicEdge> dotExporter =
                new DOTExporter<>(new ConstraintIdProvider(),
                                  new ConstraintNameProvider(),
                                  new EdgeInfoProvider());

        dotExporter.export(dotWriter, graph);

    } catch (IOException ex) {
        ex.printStackTrace();
    }

}

}
