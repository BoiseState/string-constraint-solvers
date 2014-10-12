/**
 * The processor. Traverses the inputted flow graph using a temporal depth first search to create PCs and pass them to the constraint solvers
 * using the argument.
 * @author Scott Kausler
 */
package analysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
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
import extendedSolvers.BlankSolver;
import extendedSolvers.EStranger;

public class SolveMain {

	public static void main(String[] args) {
		String fileName="../graphs/compact1.ser";
		String solverName="estranger";

		if(args.length>0){
			LinkedList<String> list = new LinkedList<String>(Arrays.asList(args));
			
			String options=list.removeLast();
			if(options.startsWith("-")){
				if(options.contains("u")){
					System.out.println("Usage: <graph file> <solver name> (-<solvers>(s) <usage>u)");
					System.out.println("Example: sootOutput/graph.ser StrangerSolver -td");
				}
				if(options.contains("s")){
					System.out.println("Solvers: EStranger");
				}
			}else{
				list.addLast(options);
			}
			if(list.size()>0)
				fileName=list.removeFirst();
			if(list.size()>0)
				solverName=list.removeFirst();
		}
	      DirectedGraph<PrintConstraint, SymbolicEdge> graph = null;
	      try
	      {
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
	      Parser parser=null;
	      String lc =solverName.toLowerCase();
	      if(lc.equals("blanksolver"))
	    	  parser=new Parser(new BlankSolver());
	      else
	    	  parser=new Parser(new EStranger());
	      runSolver(graph, parser);
	}
	/**
	 * Traverses the flow graph and solves PCs
	 * @param graph The graph to traverse.
	 * @param parser The solver to use.
	 */
	 public static void runSolver(DirectedGraph<PrintConstraint, SymbolicEdge> graph, Parser parser){
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
						 parser.remove(next.getId());
					 }
					 
				 }
			 }
			// Collections.sort(toBeAdded);
			 if(toBeAdded.size()>0){
			 PrintConstraint first=toBeAdded.getFirst();
				 if(ends.contains(first)){
					 parser.addEnd(first.getSplitValue(), first.getActualVal(), first.getId(), first.getSourceMap());
				 }
				 else if(roots.contains(first)){
					 parser.addRoot(first.getSplitValue(), first.getActualVal(), first.getId());
				 }
				 else{
					 parser.addOperation(first.getSplitValue(), first.getActualVal(), first.getId(), first.getSourceMap());
				 }
				 processedSet.add(first);
			 }
		 }
		 parser.finishUp();
	 }
}
