
function set_solver {

    # get solver string from argument
    solver=$1

    # convert solver string to lowercase
    solver=`tr A-Z a-z <<< $solver`

    # extract solver
    if [ "$solver" == 'concrete' ] ; then

        solver='concretesolver'

    elif [ "$solver" == 'jsa' -o \
           "$solver" == 'ejsa' -o \
           "$solver" == 'ejsasolver' ] ; then
        
          solver='jsasolver'

    elif [ "$solver" == 'stranger' -o \
           "$solver" == 'estranger' -o \
           "$solver" == 'estrangersolver' ] ; then
        
          solver='strangersolver'

    elif [ "$solver" == 'z3str' -o \
           "$solver" == 'ez3str' -o \
           "$solver" == 'ez3strsolver' ] ; then

          solver='z3strsolver'

    elif [ "$solver" == "" ] ; then

          solver='jsasolver'

    fi    

}

function set_classpath {

    # get project root directory from argument
    proj_root_dir=$1

    # begin classpath
    class_path="$proj_root_dir/bin"

    # for each jar file in lib directory
    for jar_file in $proj_root_dir/lib/*.jar
    do

        # get filename
        f_name=${jar_file##*/}

        # add jar file to class path
        class_path="$class_path:$proj_root_dir/lib/$f_name"

    done
}