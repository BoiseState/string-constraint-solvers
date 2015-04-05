cd SolverInterface/bin
mkdir results
for file in ../../graphs/*.ser
do
printf "\n$file\n\n"
java -Xmx2g -cp .:../../solverJars/* analysis.SolveMain $file $1 | tee results/log_$file.txt
done
