#! /bin/bash
#file containing the names of the real benchmarks to be cleaned
filename="$1"

while read -r line
do 
   name=( $line )
   echo ${name[0]}
   bench=${name[0]}

   pathfile=./graphs/real/${bench}
   #echo ${pathfile}
   if [ -f $pathfile ]; then
	echo ${bench}
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.util.CleanGraph ${pathfile}
   fi
done < $filename
