cd SolverInterface/bin
mkdir results
for file in ../../graphs/*.ser
do
printf "\n$file\n\n"
java -Xmx2g old.SolveMain $file $1
done
