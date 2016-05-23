string-constraint-solvers
=========================
Features extended string constraint solvers. Based on thesis at http://scholarworks.boisestate.edu/cgi/viewcontent.cgi?article=1868&context=td.

Installation instructions:
	1. After downloading this repository, add the following jars to the classpath.
		- jgrapht: jgrapht.org
		- jna:https://github.com/twall/jna
	2. Install the required libraries for the solvers you want to use.
		-Stranger: https://github.com/vlab-cs-ucsb/LibStranger
		-JSA: http://www.brics.dk/JSA/
		-ECLiPSe: http://eclipseclp.org/ (Must obtain string library from authors of paper).
		-Z3-str: https://sites.google.com/site/z3strsolver/ (First version was used in thesis.)

	3. Compile SolverInterface/src/analysis.SolveMain into SolverInterface/bin
	4. The runSolver.sh script runs the inputted solverType on all graphs. Example usage ./runSolver.sh estranger

The current "Parser.java" file, will use the given solverType to check if each branching point is "SING" or a singleton branching point, if the true branch is satisfiable, if the false branch is satisfiable, and if the branching point is disjoint.

V1: Contains EStranger, graphs (including extras not used in the thesis), and the framework to read and parse the graphs.

4/5/15: Added a refactored EZ3-str as well as all of the code originally used in thesis.
