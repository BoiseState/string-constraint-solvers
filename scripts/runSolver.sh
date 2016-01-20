#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
pushd $project_dir

# load class path into class_path var
class_path=$(bash ../scripts/set_class_path_var.sh)

# ensure extended solver results directory is ready
mkdir results/extended
for file in ./graphs/*.ser
do

    echo
    echo $file
    echo
    echo

    java -cp "$class_path" \
         -Xmx2g analysis.SolveMain \
         $file \
         $1 | tee ./results/extended/log_$file.txt

done

popd
