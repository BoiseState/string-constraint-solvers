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
    private String minAlphabet;
    private boolean old;
    private ReportType reportType;
    private SolverType solverType;

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

    public String getMinAlphabet() {
        return minAlphabet;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public SolverType getSolverType() {
        return solverType;
    }

    public boolean getOld() {
        return this.old;
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

    public void setMinAlphabet(String minAlphabet) {
        this.minAlphabet = minAlphabet;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public void setSolverType(SolverType solverType) {
        this.solverType = solverType;
    }

    public Settings() {

        // initialize fields with default values
        this.alphabetDeclaration = null;
        this.automatonModelVersion = 1;
        this.debug = false;
        this.initialBoundingLength = DEFAULT_BOUNDING_LENGTH;
        this.minAlphabet = " -~"; // ascii visual characters
        this.reportType = ReportType.DEFAULT;
        this.solverType = SolverType.DEFAULT;
        this.graphFilePath = "./graphs/beasties01.json";
        this.old = false;
    }

    public void setOld() {
        this.old = true;
    }

    public enum ReportType {
        SAT("SAT"),
        MODEL_COUNT("Model Count");

        public static ReportType DEFAULT = ReportType.SAT;
        private final String name;

        ReportType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.toLowerCase().replace(' ', '-');
        }
    }

    public enum SolverType {
        BLANK("Blank"),
        CONCRETE("Concrete"),
        JSA("JSA"),
        STRANGER("Stranger"),
        Z3("Z3");

        public static SolverType DEFAULT = SolverType.JSA;
        private final String name;

        SolverType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.toLowerCase().replace(' ', '-');
        }
    }
}
