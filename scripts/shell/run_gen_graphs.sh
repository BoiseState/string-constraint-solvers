#!/usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

# load useful functions
. $project_dir/scripts/shell/funcs.sh

# get classpath
set_classpath $project_dir

# ensure extended solver results directory is ready
mkdir -p $project_dir/results

# set execution arguments
solver_args[0]='concrete'
solver_args[1]='jsa --model-version 1'
solver_args[2]='jsa --model-version 2'
solver_args[3]='jsa --model-version 3'

for ((i = 0; i < ${#solver_args[@]}; i++))
do

    solver_arg="${solver_args[$i]}"

    solver_name=$solver_arg

    if [[ $solver_name == 'jsa --model-version 1' ]]
    then
        solver_name='unbounded'
    elif [[ $solver_name == 'jsa --model-version 2' ]]
    then
        solver_name='bounded'
    elif [[ $solver_name == 'jsa --model-version 3' ]]
    then
        solver_name='aggregate'
    fi

    echo
    echo "--- Solver - $solver_name ---"
    echo

    for file in $project_dir/graphs/gen*.json
    do

        # get filename
        f_name_ex=${file##*/}
        f_name=${f_name_ex%%.json}
        result_name=$f_name'_'$solver_name

        echo
        echo "*** $f_name -> $result_name ***"
        echo

        # execute solver
        java -cp "$class_path" \
             -Xmx2g \
             edu.boisestate.cs.SolveMain \
             $file \
             --solver $solver_arg \
             --length 4 \
             --reporter model-count \
             2>&1 | \
                tee $project_dir/results/$result_name.csv

    done
done