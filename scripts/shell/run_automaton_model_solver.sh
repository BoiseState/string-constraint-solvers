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
    if [ "$1" = '-d' -o \
         "$1" = '--debug' -o \
         "$1" = '-h' -o \
         "$1" = '--help' -o \
         "$1" = '-o' -o \
         "$1" = '--old' ]
    then
        solver_args="$solver_args $1"
        shift
    else
        solver_args="$solver_args $1 $2"
        shift
        shift
    fi
done

# consume reporter arg
set_reporter $1
shift

# get classpath
set_classpath ${project_dir}

# set results path
results_path="$project_dir/results/$reporter/$solver"
if [ "$solver" = 'jsa' ]
then
    case $solver_args in
    *'--model-version 1'*)        
        results_path="$project_dir/results/$reporter/jsa/unbounded"
        ;;
    *'--model-version 2'*)        
        results_path="$project_dir/results/$reporter/jsa/bounded"
        ;;
    *'--model-version 3'*)        
        results_path="$project_dir/results/$reporter/jsa/aggregate"
        ;;
    *'--model-version 4'*)        
        results_path="$project_dir/results/$reporter/jsa/weighted"
        ;;
    *)        
        results_path="$project_dir/results/$reporter/jsa/unbounded"
        ;;
    esac
fi

# ensure result path exists
mkdir -p ${results_path}

echo "results_path: $results_path"

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
            tee ${results_path}/${f_name}.csv

done