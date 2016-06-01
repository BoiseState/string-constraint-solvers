package edu.boisestate.cs;

import edu.boisestate.cs.reporting.Reporter;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.solvers.ExtendedSolver;
import org.jgrapht.DirectedGraph;

/**
 *
 */
class Components {

    private Alphabet alphabet;
    private DirectedGraph<PrintConstraint, SymbolicEdge> graph;
    private Parser parser;
    private Reporter reporter;
    private ExtendedSolver solver;

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public DirectedGraph<PrintConstraint, SymbolicEdge> getGraph() {
        return graph;
    }

    public Parser getParser() {
        return parser;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public ExtendedSolver getSolver() {
        return solver;
    }

    public void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public void setGraph(DirectedGraph<PrintConstraint, SymbolicEdge> graph) {
        this.graph = graph;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public void setSolver(ExtendedSolver solver) {
        this.solver = solver;
    }
}
