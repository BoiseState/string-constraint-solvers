#! /usr/bin/env bash

# get directory of this script as current working directory
proj_root="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

class_path="."
class_path="$class_path:$proj_root/bin"

# echo only the class path to be read by other bash scripts
echo $class_path