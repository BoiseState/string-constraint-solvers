#! /usr/bin/env bash

# <script> <solver> [<solver args>] <reporter> [<reporter args>] [<graph file without extension>]

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

# load useful functions
. ${project_dir}/scripts/shell/funcs.sh

# consume solver arg
set_solver $1
shift

# initialize solver_args from additional arguments
solver_args=''
until [ "${1:0:1}" != '-' ]
do
    solver_args="$solver_args $1 $2"
    shift
    shift
done

# consume reporter arg
set_reporter $1
shift

# get classpath
set_classpath ${project_dir}

# ensure extended solver results directory is ready
mkdir -p ${project_dir}/results/${reporter}/${solver}

# get graph files to parse
if [ -z "$1" ] ; then

    files=`ls ${project_dir}/graphs/*.json`

else

    files=`ls ${project_dir}/graphs/*.json | grep $1`

fi

# for each graph file
for file in ${files}
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.json}

    echo
    echo ${f_name}
    echo

    # execute solver
    java -cp "$class_path" \
         -Xmx2g \
         edu.boisestate.cs.SolveMain \
         ${project_dir}/graphs/${f_name}.json \
         --solver ${solver} \
         ${solver_args} \
         --reporter ${reporter} \
         2>&1 | \
            tee ${project_dir}/results/${reporter}/${solver}/${f_name}.txt

done