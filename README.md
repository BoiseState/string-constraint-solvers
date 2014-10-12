string-constraint-solvers
=========================
Features extended string constraint solvers. Based on thesis at http://scholarworks.boisestate.edu/cgi/viewcontent.cgi?article=1868&context=td.

Installation instructions:
	1. After downloading this repository, add the following jars to the classpath.
		- jgrapht: jgrapht.org
		- jna:https://github.com/twall/jna
	2. Install the stranger library: https://github.com/vlab-cs-ucsb/LibStranger
	3. Compile SolverInterface/src/analysis.SolveMain into SolverInterface/bin
	4. The runSolver.sh script runs the inputted solver on all graphs. Example usage ./runSolver.sh estranger

The current "Parser.java" file, will use the given solver to check if each branching point is "SING" or a singleton branching point, if the true branch is satisfiable, if the false branch is satisfiable, and if the branching point is disjoint.

V1: Contains EStranger, graphs (including extras not used in the thesis), and the framework to read and parse the graphs.
