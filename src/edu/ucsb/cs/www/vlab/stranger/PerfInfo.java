package edu.ucsb.cs.www.vlab.stranger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;



public class PerfInfo {

	public static class GraphInfo {
		String sinkName;
		int numOfNodes = 0;
		int numOfEdges = 0;
		public GraphInfo(String sinkName, int numOfNodes, int numOfEdges){
			this.sinkName = sinkName;
			this.numOfNodes = numOfNodes;
			this.numOfEdges = numOfEdges;
		}
		public String toString(){
			String retMe = "Sink: " + sinkName + "\n";
			retMe += "Number of nodes = " + numOfNodes + ",   Number of edges = " + numOfEdges + "\n";
			return retMe;
		}
	}

	public List<GraphInfo> graphsInfo = new LinkedList<GraphInfo>();
	
	public void addGraphInfo(String sinkName, int numOfNodes, int numOfEdges){
		PerfInfo.GraphInfo graphInfo = new PerfInfo.GraphInfo(sinkName
				, numOfNodes, numOfEdges);
		graphsInfo.add(graphInfo);
	}
	
	public long sinkRunningTime = 0;
	public int stringLength = 0;
	
	public int numOfConcat = 0;
	public int numOfUnion = 0;
	public int numOfReplace = 0;
	public int numOfPrefix = 0;
	public int numOfSuffix=0;
	
	public int prefixTime = 0;
	public int suffixTime = 0;
	
	public long concatTime = 0;
	public long unionTime = 0;
	public long replaceTime = 0;
	
	public int numOfPreConcat = 0;
	public int numOfPreReplace = 0;
	
	public long preconcatTime = 0;
	public long prereplaceTime = 0;
	
	public int numOfIntersect = 0;
	public long intersectTime = 0;
	public int numOfPreciseWiden = 0;
	public long preciseWidenTime = 0;
	public int numOfCoarseWiden = 0;
	public long coarseWidenTime = 0;
	public int numOfClosure = 0;
	public long closureTime = 0;
	public int numOfComplement = 0;
	public long complementTime = 0;
	public int numOfConstPreConcat = 0;
	public long constpreconcatTime = 0;
	public long forwardTime = 0;
	public long backwardTime = 0;
	public long multiTime = 0;
	public long generateExampleTime = 0;
	public int numOfGenerateExample = 0;
	
	public String getInfo(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Time elapsed in seconds = " + (((double) sinkRunningTime) / ((double)1000))+"\n");
		
		sb.append("Forward analysis time in seconds = " + (((double) forwardTime) / ((double)1000))+"\n");
		
		sb.append("Backward analysis time in seconds = " + (((double) backwardTime) / ((double)1000))+"\n");
		
		sb.append("Forward multi analysis time in seconds = " + (((double) multiTime) / ((double)1000))+"\n");

		
		sb.append("String length = " + stringLength+"\n");
		
		sb.append("Number of concat = " + numOfConcat+"\n");
		sb.append("Time of concat in seconds = " + (((double) concatTime) / ((double)1000))+"\n");
		
		sb.append("Number of union = " + numOfUnion+"\n");
		sb.append("Time of union in seconds = " + (((double) unionTime) / ((double)1000))+"\n");
		
		sb.append("Number of replace = " + numOfReplace+"\n");
		sb.append("Time of replace in seconds = " + (((double) replaceTime) / ((double)1000))+"\n");
		
		sb.append("Number of preconcat = " + numOfPreConcat+"\n");
		sb.append("Time of preconcat in seconds = " + (((double) preconcatTime) / ((double)1000))+"\n");
		
		sb.append("Number of constPreconcat = " + numOfConstPreConcat+"\n");
		sb.append("Time of constPreconcat in seconds = " + (((double) constpreconcatTime) / ((double)1000))+"\n");
		
		sb.append("Total number of preConcat = " + (numOfPreConcat + numOfConstPreConcat)+"\n");
		sb.append("Total time of preconcat in seconds = " + ( ( (  (double) preconcatTime  ) + (  (double) constpreconcatTime  )  ) / ((double)1000))+"\n");
		
		sb.append("Number of prereplace = " + numOfPreReplace+"\n");
		sb.append("Time of prereplace in seconds = " + (((double) prereplaceTime) / ((double)1000))+"\n");
		
		sb.append("Number of prefix = " + numOfPrefix+"\n");
		sb.append("Time of prefix in seconds = " + (((double) prefixTime) / ((double)1000))+"\n");
		
		sb.append("Number of suffix = " + numOfSuffix+"\n");
		sb.append("Time of suffix in seconds = " + (((double) suffixTime) / ((double)1000))+"\n");
		
		
		sb.append("-------------     GRAPHS INFO      -----------"+"\n");
		for (GraphInfo graphInfo: graphsInfo){
			sb.append(graphInfo.toString());
			sb.append("------------------------"+"\n");
		}
		return sb.toString();
	}
	
	private static void printUsage() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
                Object value;
                try {
                    value = method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                    value = e;
                }
                System.out.println(method.getName() + " = " + value);
            }
        }
    }
	
	public static void printMemoryUsage() {
		 long mem = Runtime.getRuntime().totalMemory() -
	      Runtime.getRuntime().freeMemory();
		 System.out.println(
		      "Memory consumption = " + mem + " bytes");
		 System.out.println("From MONA: total allocated memory = " +StrangerAutomaton.getAllocatedMemory());
	}

	public void reset() {
		 sinkRunningTime = 0;
		 stringLength = 0;
		
		 numOfConcat = 0;
		 numOfUnion = 0;
		 numOfReplace = 0;
		
		 concatTime = 0;
		 unionTime = 0;
		 replaceTime = 0;
		
		 numOfPreConcat = 0;
		 numOfPreReplace = 0;
		
		 preconcatTime = 0;
		 prereplaceTime = 0;
		
		 numOfIntersect = 0;
		 intersectTime = 0;
		 numOfPreciseWiden = 0;
		 preciseWidenTime = 0;
		 numOfCoarseWiden = 0;
		 coarseWidenTime = 0;
		 numOfClosure = 0;
		 closureTime = 0;
		 numOfComplement = 0;
		 complementTime = 0;
		 numOfConstPreConcat = 0;
		 constpreconcatTime = 0;
		 forwardTime = 0;
		 backwardTime = 0;
		 multiTime = 0;
		 graphsInfo = new LinkedList<GraphInfo>();
	}


}
