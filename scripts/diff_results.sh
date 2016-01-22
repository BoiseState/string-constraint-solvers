#! /usr/bin/env bash

# get directory of this script as current working directory
results_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../results" && pwd )"
pushd $results_dir

for file in ./extended/log_*.txt
do

    # extract original graph name
    f_name_ex=${file##*/}
    f_name=${f_name_ex%%.txt}
    graph_name=${f_name##log_}

    echo
    echo "diff for $graph_name"
    echo
    
    # execute diff
    diff ./extended/$f_name_ex ./old/$f_name_ex

done

popd