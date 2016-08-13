#!/usr/bin/env bash

function set_solver {

    # set default solver
    default_solver='jsa'

    # get solver string from argument
    solver=$1

    # convert solver string to lowercase
    solver=`tr A-Z a-z <<< $solver`

    # initialize solver_args as empty string
    solver_args=''

    local length='4'

    # extract solver
    if [ "$solver" == '' ] ; then

          solver="$default_solver"

    fi

    if [ "$solver" == 'blank' -o \
         "$solver" == 'blanksolver' ] ; then

          solver='blank'

    elif [ "$solver" == 'concrete' -o \
         "$solver" == 'concretesolver' ] ; then

          solver='concrete'
          solver_args="--length $length"

    elif [ "$solver" == 'jsa' -o \
           "$solver" == 'jsasolver' -o \
           "$solver" == 'ejsa' -o \
           "$solver" == 'ejsasolver' ] ; then
        
          solver='jsa'
          solver_args="--model-version 1 --length $length"

    elif [ "$solver" == 'stranger' -o \
           "$solver" == 'strangersolver' -o \
           "$solver" == 'estranger' -o \
           "$solver" == 'estrangersolver' ] ; then

          solver='stranger'

    elif [ "$solver" == 'z3str' -o \
           "$solver" == 'z3strsolver' -o \
           "$solver" == 'ez3str' -o \
           "$solver" == 'ez3strsolver' ] ; then

          solver='z3'

    fi

    if [ "$solver_args" != '' ] ; then

      solver_args="--solver $solver $solver_args"

    else

      solver_args="--solver $solver"

    fi

}

function set_reporter {

    # set default reporter
    default_reporter='sat'

    # get reporter string from argument
    reporter=$1

    # convert reporter string to lowercase
    reporter=`tr A-Z a-z <<< $reporter`

    # initialize reporter_args as empty string
    reporter_args=''

    # extract reporter
    if [ "$reporter" == '' ] ; then

          reporter="$default_reporter"

    fi

    if [ "$reporter" == 'sat' -o \
         "$reporter" == 'satreporter' ] ; then

          reporter='sat'

    elif [ "$reporter" == 'mc' -o \
         "$reporter" == 'mcreporter' -o \
         "$reporter" == 'model-count' -o \
         "$reporter" == 'model-countreporter' -o \
         "$reporter" == 'modelcount' -o \
         "$reporter" == 'modelcountreporter' ] ; then

          reporter='model-count'

    fi

    if [ "$reporter_args" != '' ] ; then

      reporter_args="--reporter $reporter"

    else

      reporter_args="--reporter $reporter $reporter_args"

    fi

}

function set_classpath {

  # get project root directory from argument
  proj_root_dir=$1

  if ! hash mvn 2>/dev/null; then

    # begin classpath with bin directory
    class_path="$proj_root_dir/bin"

    # for each jar file in lib directory
    for jar_file in $proj_root_dir/lib/*.jar
    do

        # get filename
        f_name=${jar_file##*/}

        # add jar file to class path
        class_path="$class_path:$proj_root_dir/lib/$f_name"

    done

  else

    # add target directories for maven build configuration
    class_path="$proj_root_dir/target/classes"

    # ensure dependencies are accounted for
    mvn dependency:build-classpath 2>&1 1>/dev/null

    mvn_class_path=`mvn dependency:build-classpath | grep -v '^\[INFO\]'`

    class_path="$class_path:$mvn_class_path"

  fi
}