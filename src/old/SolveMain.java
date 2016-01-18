/**
 * The processor. Traverses the inputted flow graph using a temporal depth first search to create PCs and pass them to the constraint solvers
 * using the argument.
 * @author Scott Kausler
 */
package old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import stringSymbolic.SymbolicEdge;
import analysis.PrintConstraint;

public class SolveMain {

	/**
	 * @param args See README in source file or Usage: print statement using "-u".
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String fileName="../sootOutput/" +
				"graph.ser";
		String solverName="JSASolver";
		String properties="../properties.txt";
		String tempFile="temp.txt";
		boolean generateText=false;
		if(args.length>0){
			LinkedList<String> list = new LinkedList<String>(Arrays.asList(args));
			
			String options=list.removeLast();
			if(options.startsWith("-")){
				if(options.contains("u")){
					System.out.println("Usage: <graph file> <solver name> (temp file) (properties file) (-<generate text output>(t) <solvers>(s) <usage>u)");
					System.out.println("Example: sootOutput/graph.ser JSASolver -td");
				}
				if(options.contains("s")){
					System.out.println("Solvers: Z3StrSolver, StrangerSolver");
				}
				if(options.contains("t")){
					generateText=true;
				}
			}else{
				list.addLast(options);
			}
			if(list.size()>0)
				fileName=list.removeFirst();
			if(list.size()>0)
				solverName=list.removeFirst();
			if(list.size()>0)
				tempFile=list.removeFirst();
			if(list.size()>0)
				properties=list.removeFirst();
		}
	      DirectedGraph<PrintConstraint, SymbolicEdge> graph = null;
	      try
	      {
	    	  System.out.println(fileName);
	    	  RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
	    	  FileInputStream fin=new FileInputStream(raf.getFD());
	         ObjectInputStream in = new ObjectInputStream(fin);
	         graph = (DirectedGraph<PrintConstraint, SymbolicEdge>) in.readObject();
	         in.close();
	         fin.close();
	         raf.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Graph not found");
	         c.printStackTrace();
	         return;
	      }
	      Solver solve=null;
	      String lc =solverName.toLowerCase();
	      if(lc.equals("z3strsolver"))
	    	  solve=new Z3StrSolver(true, properties, tempFile);
	      else if(solverName.toLowerCase().equals("jsasolver")) {
	    	  solve = new JSASolver(true, properties, tempFile);
	      }
	      else if(lc.equals("eclipsesolver")) {
	    	  solve = new ECLIPSESolver(true, properties, tempFile);
	      }
	      else if(lc.equals("combinedsatanalysis")) {
	    	  solve = new CombinedSatAnalysis(properties, tempFile);
	      }
	      else {
	    	  solve=new StrangerSolver(true, properties, tempFile);
	      }
	      if(generateText)
	    	  generateGraphFile(graph);
	      runSolver3(graph, solve);
	}
	/**
	 * Traverses the flow graph and solves PCs
	 * @param graph The graph to traverse.
	 * @param solve The solver to use.
	 */
	 public static void runSolver3(DirectedGraph<PrintConstraint,SymbolicEdge> graph,
								   Solver solve){
		 Set <PrintConstraint> removeSet=new HashSet<PrintConstraint>();
		 HashSet<PrintConstraint> processedSet=new HashSet<PrintConstraint>();

		 Set<PrintConstraint> ends=new HashSet<PrintConstraint>();
		 Set<PrintConstraint> roots=new HashSet<PrintConstraint>();
		 Iterator<PrintConstraint>rootIt=graph.vertexSet().iterator();
		 while(rootIt.hasNext()){
			 PrintConstraint constraint=rootIt.next();
			 if(graph.inDegreeOf(constraint)==0){
				 roots.add(constraint);
			 }
			 if(graph.outDegreeOf(constraint)==0){
				 ends.add(constraint);
			 }		 
		 }
		 LinkedList<PrintConstraint> toBeAdded=new LinkedList<PrintConstraint>();
		 ArrayList <PrintConstraint> vertexSet=new ArrayList<PrintConstraint>(graph.vertexSet());
		 Collections.sort(vertexSet);
		 boolean end=false;
		 //Topological progression...
		 while(vertexSet.size()>0){
			 graph.removeAllVertices(removeSet);
			 vertexSet.removeAll(removeSet);
			 vertexSet.removeAll(toBeAdded);
			 processedSet.removeAll(removeSet);
			 removeSet=new HashSet<PrintConstraint>();
			 			 
			 toBeAdded=new LinkedList<PrintConstraint>();
			 for(int i=0; i<vertexSet.size(); i++){
				 PrintConstraint next=vertexSet.get(i);
				 HashMap<String, Integer>sourceMap=new HashMap<String, Integer>();
				 if(!processedSet.contains(next)){
					 boolean readyToProcess=true;
					 Iterator<SymbolicEdge>edgeIt=graph.incomingEdgesOf(next).iterator();
					 while(edgeIt.hasNext()){
						 SymbolicEdge edge=edgeIt.next();
						 PrintConstraint source=(PrintConstraint) edge.getASource();
						 if(!processedSet.contains(source)){
							 readyToProcess=false;
							 break;
						 }
						 else
							 sourceMap.put(edge.getType(), source.getId());
					 }
					 if(readyToProcess){
						 next.setSourceMap(sourceMap);
						 toBeAdded.add(next);
						 break;
					 }
					 else{
					 }
				 }
				 else{
					 Iterator<SymbolicEdge>edgeIt=graph.outgoingEdgesOf(next).iterator();
					 boolean deleteNode=true;
					 while(edgeIt.hasNext()){
						 SymbolicEdge edge=edgeIt.next();
						 PrintConstraint target=(PrintConstraint) edge.getATarget();
						 if(!processedSet.contains(target)){
							 deleteNode=false;
							 break;
						 }
					 }
					 if(deleteNode){
						 removeSet.add(next);
						 solve.remove(next.getId());
					 }
					 
				 }
			 }
			// Collections.sort(toBeAdded);
			 if(toBeAdded.size()>0){
			 PrintConstraint first=toBeAdded.getFirst();
				 if(ends.contains(first)){
					 solve.addEnd(first.getSplitValue(), first.getActualVal(), first.getId(), first.getSourceMap());
				 }
				 else if(roots.contains(first)){
					 solve.addRoot(first.getSplitValue(), first.getActualVal(), first.getId());
					 if(solve.caresAboutHeight()){
						 int height=getVertexHeight(graph, first);
						 solve.addHeight(height);
					 }
				 }
				 else{
					 solve.addOperation(first.getSplitValue(), first.getActualVal(), first.getId(), first.getSourceMap());
				 }
				 processedSet.add(first);
			 }
		 }
		 solve.finishUp();
		 solve.getStats();
		 solve.writeToFile();
		 }
		
	 /**
	  * Used to calculate the height of the graph at a source vertex.
	  * @param graph The graph
	  * @param first The vertex to check.
	  * @return An integer representing the height at that vertex.
	  */
	 private static int getVertexHeight(
			DirectedGraph<PrintConstraint, SymbolicEdge> graph, PrintConstraint first) {
		 Set<SymbolicEdge> edgeSet=graph.outgoingEdgesOf(first);
		 int height=0;

		 while(edgeSet.size()>0){
			 height++;
			 Set<SymbolicEdge> newSet=new HashSet<SymbolicEdge>();
			 for(SymbolicEdge e:edgeSet){
				 newSet.addAll(graph.outgoingEdgesOf((PrintConstraint) e.getATarget()));
			 }
			 edgeSet=newSet;
		 }
		 return height;
	}
	 
	 /**
	  * Creates a text file depicting the graph.
	  * @param graph The graph to be described.
	  */
	public static void generateGraphFile(DirectedGraph<PrintConstraint, SymbolicEdge> graph) {
		    Iterator<SymbolicEdge>it=graph.edgeSet().iterator();
		    StringBuilder graphString=new StringBuilder();
		    while(it.hasNext()){
		  	  SymbolicEdge edge=it.next();
		  	  PrintConstraint source=((PrintConstraint) edge.getASource());
		  	  PrintConstraint target=((PrintConstraint) edge.getATarget());
		  	  graphString.append(source.getSplitValue()+"!:"+source.getActualVal()+"!:-"+source.getId()+ " -> "+target.getSplitValue()+"!:"+target.getActualVal()+"!:-"+target.getId()+" ["+edge.getType()+"];\n");
		    }
		    
		    File textFile = new File("graph.txt");
		    FileWriter fileWriter;
			try {
				fileWriter = new FileWriter(textFile);
			    fileWriter.write(graphString.toString());
			    fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		/**
		 * Processes a string for GraphVis (dot) by removing special characters.
		 * @param target The string to be modified.
		 * @return The modified version of the string.
		 */
		private static String replace(String target){
			  target=target.replaceAll("=", "equals");
			  target=target.replaceAll("0", "zero");
			  target=target.replaceAll("1", "one");
			  target=target.replaceAll("2", "two");
			  target=target.replaceAll("3", "three");
			  target=target.replaceAll("4", "four");
			  target=target.replaceAll("5", "five");
			  target=target.replaceAll("6", "six");
			  target=target.replaceAll("7", "seven");
			  target=target.replaceAll("8", "eight");
			  target=target.replaceAll("9", "nine");

			  target=target.replaceAll("@", "at");
			  target=target.replaceAll("~", "tilda");
			  target=target.replaceAll("\\^", "carrot");
			  target=target.replaceAll("`", "tic");
			  target=target.replaceAll("'", "squote");
			  target=target.replaceAll("/", "bslash");
			  target=target.replaceAll(";", "semicolon");
			  target=target.replaceAll("#", "pound");
			  target=target.replaceAll("\\?", "question");
			  target=target.replaceAll("!", "exclamation");
		  target=target.replaceAll(",", "comma");
	  	  target=target.replaceAll(" ", "space");
	  	  target=target.replaceAll("\\+", "plus");
	  	  target=target.replaceAll("\\*", "star");
	  	  target=target.replaceAll("\\\\", "fslash");
	  	  target=target.replaceAll(">", "gt");
	  	  target=target.replaceAll("<", "lt");
	  	  target=target.replaceAll("\\[", "obracket");
	  	  target=target.replaceAll("\\]", "cbracket");
	  	  target=target.replaceAll("\\{", "ocurly");
	  	  target=target.replaceAll("\\}", "ccurly");
	  	  target=target.replaceAll("\"", "quote");
	  	  target=target.replaceAll(":", "colon");
	  	  target=target.replaceAll("\\.", "period");
	  	  target=target.replaceAll("\\|", "vbar");
	  	  target=target.replaceAll("\\(", "oparam");
	  	  target=target.replaceAll("\\)", "cparam");
	  	  target=target.replaceAll("-", "dash");
	  	  target=target.replaceAll("\\$", "dollar");
	  	  target=target.replaceAll("%", "percent");
	  	  return target;
		}
		
		/**
		 * Processes a string as a literal for GraphViz (dot) by replacing certain characters
		 * with others.
		 * @param target The string to be modified.
		 * @return The modified version of the string.
		 */
		private static String prepareString(String target){
		  	  target=target.replaceAll(">", "\\>");
		  	  target=target.replaceAll("<", "\\<");
		  	  target=target.replaceAll("\"", "''");
		  	  target=target.replaceAll("\"", "''");
		  	  return target;
			}
}
