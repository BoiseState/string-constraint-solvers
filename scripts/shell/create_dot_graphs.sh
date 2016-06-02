#! /usr/bin/env bash

# get dot file directory
dot_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )/../../graphs/dot" && pwd )

# get all dot files for neato graphs
dot_files=`ls $dot_dir/*.dot`


# generate graph png
for file in $dot_files; do

    # extract filename only
    filename=$(basename "$file")
    filebase="${filename%.*}"

    echo "creating graph for $filebase..."

    dot -Tpng $dot_dir/$filename -o $dot_dir/../png/$filebase.png

done