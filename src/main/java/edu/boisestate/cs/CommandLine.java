package edu.boisestate.cs;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.List;

/**
 *
 */
class CommandLine {

    static Settings processArgs(String[] args) {

        // create options
        Options options = createOptions();

        // create command line parser
        CommandLineParser parser = new DefaultParser();
        org.apache.commons.cli.CommandLine commandLine;

        try {

            // parse command line arguments
            commandLine = parser.parse(options, args);

        } catch (ParseException e) {

            // error parsing arguments, show error and return null
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

            // no other action needed, return null
            return null;
        }

        // ensure valid unprocessed arguments
        List<String> argsList = commandLine.getArgList();
        if (argsList.size() != 1 ||
            argsList.get(0) == null ||
            !isValidGraphFile(argsList.get(0))) {

            // error parsing arguments, show error and return null
            System.err.println(
                    "Invalid arguments have been specified, please consult " +
                    "usage documentation for help with the -h or --help " +
                    "option");

            return null;
        }

        // get graph file from unprocessed arguments list
        settings.setGraphFilePath(argsList.get(0));

        // process debug option
        if (commandLine.hasOption("d")) {
            settings.setDebug(true);
        }

        // process bounding length option
        if (commandLine.hasOption("l")) {

            // set initial bounding length from option value
            String optionValue = commandLine.getOptionValue("l");
            int boundingLength = Integer.parseInt(optionValue);
            settings.setInitialBoundingLength(boundingLength);
        }

        // process automaton model version option
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
                settings.setSolverType(Settings.SolverType.BLANK);
            } else if (choice.equals("concrete")) {
                settings.setSolverType(Settings.SolverType.CONCRETE);
            } else if (choice.equals("jsa")) {
                settings.setSolverType(Settings.SolverType.JSA);
            } else if (choice.equals("stranger")) {
                settings.setSolverType(Settings.SolverType.STRANGER);
            } else if (choice.equals("z3")) {
                settings.setSolverType(Settings.SolverType.Z3);
            } else {
                String errorMessage = String.format(
                        "The specified solver \"%s\" is not a recognized " +
                        "string constraint solver, please use the -h or " +
                        "--help option to see the valid solvers",
                        choice);
                System.err.println(errorMessage);
                return null;
            }

            if (commandLine.hasOption("o")) {
                settings.setOld();
            }
        }

        // process reporter option
        if (commandLine.hasOption("r")) {

            // get reporter choice from option value
            String optionValue = commandLine.getOptionValue("r");
            String choice = optionValue.toLowerCase();

            if (choice.equals("sat")) {
                settings.setReportType(Settings.ReportType.SAT);
            } else if (choice.equals("model-count")) {
                settings.setReportType(Settings.ReportType.MODEL_COUNT);
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

    private static boolean isValidGraphFile(String filePath) {
        File graphFile = new File(filePath);
        return graphFile.exists() && filePath.endsWith(".json");
    }

    private static void printHelp(Options options) {

        // create formatter
        HelpFormatter formatter = new HelpFormatter();

        formatter.setSyntaxPrefix("USAGE:\n\n" + padding(2));

        String appClass = "java " + SolveMain.class.getName();

        StringBuilder header = new StringBuilder();

        // description
        header.append("\nRun string constraint solver on specified control")
              .append(" flow graph. The default string constraint solver ")
              .append("is ")
              .append(Settings.SolverType.DEFAULT)
              .append(". The default reporter is ")
              .append(Settings.ReportType.DEFAULT);

        // section header for options
        header.append(".\n\nOPTIONS:\n\n");

        StringBuilder footer = new StringBuilder();

        // section header for example usage
        footer.append("\n\nUSAGE EXAMPLES:");

        // first example
        footer.append("\n\n")
              .append(padding(4))
              .append(appClass)
              .append(" <PROJECT_ROOT>/graphs/iText02.json")
              .append("\n")
              .append(padding(8))
              .append("-s jsa -r sat -v 1 -l 10");

        // first example explanation
        footer.append("\n\nRun sat reporter for the iText02.json constraint")
              .append(" graph file using the JSA solver with unbounded")
              .append(" automata and an initial bounding length of 10.");

        // second example
        footer.append("\n\n")
              .append(padding(4))
              .append(appClass)
              .append(" <PROJECT_ROOT>/graphs/iText02.json")
              .append("\n")
              .append(padding(8))
              .append("-s concrete -r model-count -l 10");

        // second example explanation
        footer.append("\n\nRun model count reporter for the iText02.json")
              .append(" constraint graph file using the Concrete solver with")
              .append(" an initial bounding length of 10.");

        // section header for additional information
        footer.append("\n\nADDITIONAL INFORMATION:");

        // additional information description
        footer.append("\n\nSee the code repository at https://github.com/")
              .append("BoiseState/string-constraint-solvers for more ")
              .append("details.\n");

        formatter.printHelp(appClass + " <Graph File>",
                            header.toString(),
                            options,
                            footer.toString(),
                            true);
    }

    private static String padding(int length) {

        // create char array of length with blank spaces
        char[] pad = new char[length];
        for (int i = 0; i < length; i++) {
            pad[i] = ' ';
        }

        // return string created from pad array
        return new String(pad);
    }

    private static Options createOptions() {

        // solver option
        Option solver = Option.builder("s")
                              .longOpt("solver")
                              .desc("The solver that will be used to solve " +
                                    "string constraints:\n" +
                                    Settings.SolverType.BLANK +
                                    " - The blank solver used for testing" +
                                    ".\n" +
                                    Settings.SolverType.CONCRETE +
                                    " - The concrete solver which provides an" +
                                    " oracle for other solvers.\n" +
                                    Settings.SolverType.JSA +
                                    " - The Java String Analyzer solver which" +
                                    " comes from the dk.brics automaton and " +
                                    "string libraries.\n" +
                                    Settings.SolverType.STRANGER +
                                    " - The STRANGER string constraint solver" +
                                    ".\n" +
                                    Settings.SolverType.Z3 +
                                    " - The Z3 rule based string constraint " +
                                    "solver.\n\nThe default solver is " +
                                    Settings.SolverType.DEFAULT +
                                    "\n")
                              .hasArg()
                              .numberOfArgs(1)
                              .argName("solver")
                              .build();

        // reporter option
        Option reporter = Option.builder("r")
                                .longOpt("reporter")
                                .desc("The reporter used to gather " +
                                      "information for each string constraint" +
                                      ":\n" +
                                      Settings.ReportType.SAT +
                                      " - Reports on the satisfiability of " +
                                      "each string constraint in the " +
                                      "specified graph\n" +
                                      Settings.ReportType.MODEL_COUNT +
                                      " - Reports on the number and percent " +
                                      "of" +
                                      " string instances for each branch " +
                                      "leaving the string constraint, " +
                                      "includes satisfiability.\n\nThe " +
                                      "default reporter is " +
                                      Settings.ReportType.DEFAULT +
                                      "\n")
                                .hasArg()
                                .numberOfArgs(1)
                                .argName("reporter")
                                .build();

        // debug mode flag
        Option debug = Option.builder("d")
                             .longOpt("debug")
                             .desc("Runs the solver framework in debug mode." +
                                   " Default value is false.")
                             .build();

        // help / usage option
        Option help = Option.builder("h")
                            .longOpt("help")
                            .desc("Display this message.")
                            .build();

        // automaton bounding length option
        Option length = Option.builder("l")
                              .longOpt("length")
                              .desc("Initial bounding length of the " +
                                    "underlying symbolic string, used with " +
                                    "JSA ans Concrete solvers. Default value " +
                                    "is " +
                                    Settings.DEFAULT_BOUNDING_LENGTH + ".")
                              .hasArg()
                              .numberOfArgs(1)
                              .argName("length")
                              .build();

        // automaton model version option
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

        Option old = Option.builder("o")
                           .longOpt("old")
                           .desc("Runs older version of jsa solver")
                           .build();

        // add each option to options collection
        Options options = new Options();
        options.addOption(debug);
        options.addOption(help);
        options.addOption(length);
        options.addOption(modelVersion);
        options.addOption(solver);
        options.addOption(reporter);
        options.addOption(old);

        // return options
        return options;
    }
}
