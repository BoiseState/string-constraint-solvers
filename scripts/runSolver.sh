#! /usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
pushd $project_dir

# load class path into class_path var
class_path=$(bash ./scripts/set_class_path_var.sh)

# ensure extended solver results directory is ready
mkdir -p ./results/extended

# for each graph file
for file in ./graphs/htmlCleaner17.ser
do

    # get filename
    f_name_ex=${file##*/}
    f_name=${filename%%.ser}

    echo
    echo $f_name_ex
    echo
    echo

    # execute solver
    java -cp "$class_path" \
#         -Djna.library.path=
         -Xmx2g analysis.SolveMain \
         $file \
         $1 | tee ./results/extended/log_$f_name.txt

done

popd
