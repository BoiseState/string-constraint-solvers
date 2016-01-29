#! /usr/bin/env bash

# get directory of this script as current working directory
results_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../results" && pwd )"
pushd $results_dir

# output heading
echo "Log files do not match for the following graph files:"

# clean diffs directory
rm ./diff/*

for file in ./extended/log_*.txt
do

    # extract original graph name
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}
    graph_name=${f_name##log_}
    
    # execute diff
    diff -B \
         ./extended/$f_name_ex \
         ./old/$f_name_ex &>/dev/null

    # if difference found
    if [ $? -ne 0 ]
    then

        # output file name
        echo
        echo "--- $graph_name ---"

        # show side by side diff
        diff -B \
             ./extended/$f_name_ex \
             ./old/$f_name_ex | tee ./diff/$graph_name.txt
    fi

done

popd