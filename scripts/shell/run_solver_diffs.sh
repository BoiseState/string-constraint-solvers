#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

# load useful functions
. $project_dir/scripts/shell/funcs.sh

# get solver
set_solver $1

# get reporter
set_reporter $2

# get classpath
set_classpath $project_dir

# ensure extended solver results directory is ready
mkdir -p $project_dir/results/$reporter/$solver/extended

# ensure extended solver results directory is ready
mkdir -p $project_dir/results/$reporter/$solver/automaton_model

# get graph files to parse
if [ -z "$3" ] ; then

    files=`ls $project_dir/graphs/*.ser`

else

    files="$project_dir/graphs/$3.ser"

fi

# for each graph file
for file in $project_dir/results/$reporter/$solver/diff/*.txt
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}

    echo
    echo $f_name_ex
    echo
    echo '--- Extended Solver ---'

    # execute extended solver
    java -cp "$class_path" \
         -Xmx2g \
         edu.boisestate.cs.SolveMain \
         $project_dir/graphs/$f_name.json \
         $solver_args \
         $reporter_args \
         --old \
         2>&1 | \
            tee $project_dir/results/$reporter/$solver/extended/$f_name.txt

    echo
    echo '--- Automaton Model Solver ---'


    # execute automaton model solver
    java -cp "$class_path" \
         -Xmx2g \
         edu.boisestate.cs.SolveMain \
         $project_dir/graphs/$f_name.json \
         $solver_args \
         $reporter_args \
         2>&1 | \
            tee $project_dir/results/$reporter/$solver/extended/$f_name.txt

done
