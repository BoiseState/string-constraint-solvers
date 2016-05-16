package edu.boisestate.cs;

public class Settings {

    public static final int DEFAULT_BOUNDING_LENGTH = 10;
    private String alphabetDeclaration;
    /**
     * Version of the automaton model used with JSA family of string constraint
     * solvers: <ol> <li>Unbounded automaton model</li> <li>Bounded automaton
     * model</li> <li>Aggregate bounded automata model</li> <li>Proposed
     * accurate automaton model</li> </ol>
     */
    private int automatonModelVersion;
    private boolean debug;
    private String graphFilePath;
    private int initialBoundingLength;
    private Reporter reporter;
    private Solver solver;

    public String getAlphabetDeclaration() {
        return alphabetDeclaration;
    }

    public int getAutomatonModelVersion() {
        return automatonModelVersion;
    }

    public boolean getDebug() {
        return debug;
    }

    public String getGraphFilePath() {
        return graphFilePath;
    }

    public int getInitialBoundingLength() {
        return initialBoundingLength;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setAlphabetDeclaration(String alphabetDeclaration) {
        this.alphabetDeclaration = alphabetDeclaration;
    }

    public void setAutomatonModelVersion(int automatonModelVersion) {
        this.automatonModelVersion = automatonModelVersion;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setGraphFilePath(String graphFilePath) {
        this.graphFilePath = graphFilePath;
    }

    public void setInitialBoundingLength(int initialBoundingLength) {
        this.initialBoundingLength = initialBoundingLength;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public Settings() {

        // initialize fields with default values
        this.alphabetDeclaration = " -~"; // ascii visual characters
        this.automatonModelVersion = 1;
        this.debug = false;
        this.initialBoundingLength = DEFAULT_BOUNDING_LENGTH;
        this.reporter = Reporter.DEFAULT;
        this.solver = Solver.DEFAULT;
        this.graphFilePath = "./graphs/beasties01.json";
    }

    public enum Reporter {
        SAT("SAT"),
        MODEL_COUNT("Model Count");

        public static Reporter DEFAULT = Reporter.SAT;
        private final String name;

        Reporter(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.toLowerCase().replace(' ', '-');
        }
    }

    public enum Solver {
        BLANK("Blank"),
        CONCRETE("Concrete"),
        JSA("JSA"),
        STRANGER("Stranger"),
        Z3("Z3");

        public static Solver DEFAULT = Solver.JSA;
        private final String name;

        Solver(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.toLowerCase().replace(' ', '-');
        }
    }
}
