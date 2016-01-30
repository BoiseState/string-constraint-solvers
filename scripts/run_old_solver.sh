#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# load useful functions
. $project_dir/scripts/funcs.sh

# get solver
set_solver $1

# get classpath
set_classpath $project_dir

# ensure old results directory is ready
mkdir -p $project_dir/results/$solver/old

# for each graph file
for file in ./graphs/*.ser
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.ser}

    echo
    echo $f_name_ex
    echo
    echo

    java -cp "$class_path" \
         -Xmx2g \
         old.SolveMain \
         $file \
         $1 \
         $project_dir/results/oldTemp.txt \
         $project_dir/properties.txt | \
            tee $project_dir/results/$solver/old/$f_name.txt

done