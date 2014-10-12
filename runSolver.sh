#!/bin/bash
#Usage: Make sure analysis.SolveMain is compile in SolverInterface/bin.
#	The argument is the solver name, e.g., blanksovler or estranger
cd SolverInterface/bin
for file in ../../graphs/*.ser
do
printf "\n$file\n\n"
java -Xmx2g -cp .:../../solverJars/* analysis.SolveMain $file $1 | tee log.txt
done
