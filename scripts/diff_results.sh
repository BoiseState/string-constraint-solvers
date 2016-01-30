#! /usr/bin/env bash

# get directory of this script as current working directory
results_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../results" && pwd )"

# load useful functions
. $results_dir/../scripts/funcs.sh

# get solver
set_solver $1

# ensure extended solver results directory is ready
mkdir -p $results_dir/$solver/diff/

# clean diffs directory
rm $results_dir/$solver/diff/*

for file in $results_dir/$solver/extended/*.txt
do

    # extract original graph name
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}
    
    # execute diff
    diff -B \
         $results_dir/$solver/extended/$f_name_ex \
         $results_dir/$solver/old/$f_name_ex &>/dev/null

    # if difference found
    if [ $? -ne 0 ]
    then

        # output file name
        echo
        echo "--- $f_name ---"

        # show side by side diff
        diff -B \
             $results_dir/$solver/extended/$f_name_ex \
             $results_dir/$solver/old/$f_name_ex | \
                tee $results_dir/$solver/diff/$f_name.txt
    fi

done