package analysis;

import extendedSolvers.*;
import org.jgrapht.DirectedGraph;
import stringSymbolic.SymbolicEdge;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;

public class CommandLineUtilities {

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

    static Parser getParser(String solverName) {

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
}
