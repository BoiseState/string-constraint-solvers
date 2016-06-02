#! /usr/bin/env bash

# get dot file directory
png_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )/../../graphs/png" && pwd )

# get all dot files for neato graphs
png_files=`ls $png_dir/*.png`


# generate graph png
for file in $png_files; do

    # remove
    rm $file

done