#! /usr/bin/env bash

# get directory of this script as current working directory
results_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../../results" && pwd )"

# load useful functions
. $results_dir/../scripts/shell/funcs.sh

# get solver
set_solver $1

# get reporter
set_reporter $2

# ensure extended solver results directory is ready
mkdir -p $results_dir/$reporter/$solver/diff/

# clean diffs directory
if [ "$(ls -A $results_dir/$reporter/$solver/diff)" ]; then
    rm $results_dir/$reporter/$solver/diff/*
fi

for file in $results_dir/$reporter/$solver/extended/*.txt
do

    # extract original graph name
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}

    echo
    echo "diff $f_name"
    echo
    
    # execute diff
    diff -B \
         $results_dir/$reporter/$solver/automaton_model/$f_name_ex \
         $results_dir/$reporter/$solver/extended/$f_name_ex &>/dev/null

    # if difference found
    if [ $? -ne 0 ]
    then

        # output file name
        echo
        echo "--- $f_name ---"

        # show side by side diff
        diff -B \
             $results_dir/$reporter/$solver/automaton_model/$f_name_ex \
             $results_dir/$reporter/$solver/extended/$f_name_ex | \
                tee $results_dir/$reporter/$solver/diff/$f_name.txt
    fi

done