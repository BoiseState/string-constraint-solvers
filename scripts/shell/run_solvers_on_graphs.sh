#!/usr/bin/env bash

# get directory of this script as current working directory
project_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

# detect if python available
python_cmd=''
if hash python 2>/dev/null;
then
    python_cmd='python'
elif hash python2 2>/dev/null;
then
    python_cmd='python2'
elif hash python3 2>/dev/null;
then
    python_cmd='python3'
fi

# if python command available
if [ "x$python_cmd" != 'x' ]
then
    # run script with parameters
    ${python_cmd} ${project_dir}/scripts/python/run_solvers_on_graphs.py \
                  --debug \
                  --graph-files gen*.json \
                  --mc-reporter \
                  --length 2 \
                  --concrete-solver \
                  --unbounded-solver \
                  --bounded-solver \
                  --aggregate-solver
else
    echo
    echo "!!! None of the python commands \"python\", \"python2\", or \"python3\" are available !!!"
    echo
fi