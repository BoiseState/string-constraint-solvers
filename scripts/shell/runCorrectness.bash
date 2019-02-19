#! /bin/bash
#file containing the names of the to the correctness benchmark
filename="$1"

while read -r line
do 
   name=( $line )
   echo ${name[0]}
   bench=${name[0]}

   pathfile=./graphs/benchmarks/${bench}
   #echo ${pathfile}
   if [ -f $pathfile ]; then
   #need to parse bench in order to figure out l
   len=3;
   if [[ $bench == *"_l2_"* ]]; then
    len=2;
   elif [[ $bench == *"_l3_"* ]]; then
      len=3;
   fi
   #echo $len
   #imports $CLASSPATH variable
   #run concrete
   fileName=(${bench//./ })
    outfileOrig=./data/correctness/type/${fileName[0]}.txt
    #echo $outfile
    outfile=${outfileOrig//type/concrete}
    #echo $outfile
    echo "concrete"
	java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s concrete > ${outfile}
	outfile=${outfileOrig//type/weightedAcyclic}
	#echo $outfile
    echo "weighted"
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 5 > ${outfile}
    #compare them
    echo "comparing"
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.reporting.MCDifference ${fileName[0]}.txt
    outfile=${outfileOrig//type/bounded}
	#echo $outfile
    echo "bounded"
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 1 > ${outfile}
    outfile=${outfileOrig//type/acyclic}
	#echo $outfile
    echo "acyclic"
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 2 > ${outfile}
   fi
done < $filename
