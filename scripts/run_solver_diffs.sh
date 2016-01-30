#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# load useful functions
. $project_dir/scripts/funcs.sh

# get solver
set_solver $1

# get classpath
set_classpath $project_dir

# for each graph file
for file in $project_dir/results/$solver/diff/*.txt
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}

    echo
    echo $f_name_ex
    echo
    echo '--- Old Solver ---'

    # execute old solver
    java -cp "$class_path" \
         -Xmx2g \
         old.SolveMain \
         $project_dir/graphs/$f_name.ser \
         $solver \
         $project_dir/results/oldTemp.txt \
         $project_dir/properties.txt \
         2>&1 | \
            tee $project_dir/results/old/log_$f_name.txt

    echo
    echo '--- New Extended Solver ---'


    # execute new extended solver
    java -cp "$class_path" \
         -Xmx2g \
         analysis.SolveMain \
         $project_dir/graphs/$f_name.ser \
         $solver \
         2>&1 | \
            tee $project_dir/results/extended/log_$f_name.txt

done
