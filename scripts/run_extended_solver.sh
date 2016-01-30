#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# load useful functions
. $project_dir/scripts/funcs.sh

# get solver
set_solver $1

# get classpath
set_classpath $project_dir

# ensure extended solver results directory is ready
mkdir -p $project_dir/results/$solver/extended

# for each graph file
for file in $project_dir/graphs/*.ser
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.ser}

    echo
    echo $f_name_ex
    echo
    echo

    # execute solver
    java -cp "$class_path" \
         -Xmx2g \
         analysis.SolveMain \
         $project_dir/graphs/$f_name.ser \
         $solver | \
            tee $project_dir/results/$solver/extended/$f_name.txt

done