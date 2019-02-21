#! /bin/bash
#file containing the names of the to the correctness benchmark
filename="$1"
#the max length of the symbolic string
len="$2"

while read -r line
do 
   name=( $line )
   echo ${name[0]}
   bench=${name[0]}

   pathfile=./graphs/real/cleaned/${bench}
   #echo ${pathfile}
   if [ -f $pathfile ]; then

   #imports $CLASSPATH variable
   #run concrete
   fileName=(${bench//./ })
    outfileOrig=./data/evaluation/type/${fileName[0]}_l${len}.txt
	
	#outfile=${outfileOrig//type/weightedAcyclic}
	#echo $outfile
    #echo "weighted"
    #java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 5 > ${outfile}
    
    outfile=${outfileOrig//type/bounded}
    echo "bounded"
    java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 1 > ${outfile}
    
    #outfile=${outfileOrig//type/acyclic}
    #echo "acyclic"
    #java -Xmx4g -cp ./target/classes/:$CLASSPATH edu.boisestate.cs.SolveMain -l ${len} ${pathfile} -r model-count -s jsa  -v 2 > ${outfile}
   fi
done < $filename
