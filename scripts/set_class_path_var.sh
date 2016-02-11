#! /usr/bin/env bash

# get directory of this script as current working directory
proj_root="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

class_path="$proj_root/bin"

# for each jar file in lib directory
for jar_file in $proj_root/lib/*.jar
do

    # get filename
    f_name=${jar_file##*/}

    # add jar file to class path
    class_path="$class_path:$proj_root/lib/$f_name"

done

# for each dylib file in lib directory
for dylib_file in $proj_root/lib/*.dylib
do

    # get filename
    f_name=${dylib_file##*/}

    # add dylib file to class path
    class_path="$class_path:$proj_root/lib/$f_name"

done

# echo only the class path to be read by other bash scripts
echo $class_path