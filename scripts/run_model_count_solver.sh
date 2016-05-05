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
output_dir=$project_dir/results/$solver/model_count
mkdir -p $output_dir

# get graph files to parse
if [ -z "$2" ] ; then

    files=`ls $project_dir/graphs/*.ser`

else

    files="$project_dir/graphs/$2.ser"

fi

# for each graph file
for file in $files
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
         edu.boisestate.cs.analysis.SolveMain \
         $project_dir/graphs/$f_name.ser \
         $solver \
         mc \
         2>&1 | \
            tee $output_dir/$f_name.txt

done