#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
pushd $project_dir

# load class path into class_path var
class_path=$(bash ../scripts/set_class_path_var.sh)

# ensure old results directory is ready
mkdir -p ./results/old

# for each graph file
for file in ./graphs/*.ser
do

    # get filename
    f_name_ex=${file##*/}

    echo
    echo $f_name_ex
    echo
    echo

    java -cp "$class_path" \
         -Xmx2g old.SolveMain \
         $file \
         $1

done

popd