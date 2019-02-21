package edu.boisestate.cs.util;

import edu.boisestate.cs.SolveMain;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import edu.boisestate.cs.graph.generator.Node;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * From real graphs remove nodes
 * that do not lead to a predicate
 * @author elenasherman
 *
 */
public class CleanGraph {

	public static void main(String[] args) {

		String filepath = args[0];
		LambdaVoid1<String> printMinAlphabet = new LambdaVoid1<String>() {
			@Override
			public void execute(String s) {
				System.out.printf("Minimum Alphabet: %s\n", s);
			}
		};
		DirectedGraph<PrintConstraint, SymbolicEdge> graph =
				SolveMain.loadGraph(filepath, printMinAlphabet);

		Set<PrintConstraint> reachableNodes = new HashSet<PrintConstraint>();
		System.out.println("Before " + graph.vertexSet().size());
		int count = 0;
		EdgeReversedGraph<PrintConstraint, SymbolicEdge> reversedGraph = new EdgeReversedGraph<>(graph);
		for(PrintConstraint v : graph.vertexSet()){
			//System.out.println("v " + v.getSplitValue());
			//        	System.out.println("e " + graph.outgoingEdgesOf(v));
			if(v.getSplitValue().startsWith("equals!") ||
					v.getSplitValue().startsWith("isEmpty") ||
					v.getSplitValue().startsWith("contains")){
				//System.out.println("v " + v.getSplitValue());
				Set<PrintConstraint> allChildren = getAllParents(reversedGraph, v);
				//System.out.println(allChildren.size());
				reachableNodes.addAll(allChildren);
				count++;

			}
		}
		System.out.println("After " + reachableNodes.size() + " predicates " + count);
		if(reachableNodes.isEmpty()){
			return; // do not write the file with no info there
		}
		//System.out.println("Nodes " + reachableNodes);
		Set<Character> abc = new HashSet<Character>();
		for(PrintConstraint v : reachableNodes){
			//System.out.println("type " + v.getType() + "\t" + v);
			//System.out.println("type " + v.getType() + "\t val " + v.getSplitValue());
			String val = v.getActualVal();
			
			//if(val != null && val.length() > 2 && val.startsWith("\"")){
			if(v.getType() !=9 && val != null){
				//System.out.println("Actual " + val + " " + val.equalsIgnoreCase("false") + " " + val.equalsIgnoreCase("true"));
				//means it is a concerete value that should be included in the alphabet
				//String chars = val.split("\"")[1];
				String chars = val;
				if(!chars.equalsIgnoreCase("true") && !chars.equalsIgnoreCase("false")){
					String low = chars.toLowerCase();
					String upper = chars.toUpperCase();
					//merge them
					chars = low+upper;
					//System.out.println("chars " + chars);
					//System.out.println("Sybms " + chars);
					for(int i=0; i < chars.length(); i++){
						abc.add(chars.charAt(i));
					}
				}
			}
			//        	String actualVal = v.getActualVal();
			//        	if(actualVal.length() > 2 && actualVal.startsWith("\"")){
			//        		//get the actual value of that string
			//        	}
		}
		//add all other symbols?
		//System.out.println("The min abc is " + abc);
		Set<Character> lowerCase = new HashSet<Character>();
		boolean hasLower = false;
		for(int i = 97; i <= 122; i++){
			char lC = (char)i;
			lowerCase.add(lC);
			if(abc.contains(lC)){
				hasLower = true;
			}
		}

		Set<Character> upperCase = new HashSet<Character>();
		//add upper case
		boolean hasUpper = false;
		for(int i = 65; i <= 90; i++){
			char uC = (char)i;
			upperCase.add((char)i);
			if(abc.contains(uC)){
				hasUpper = true;
			}
		}

		//see if our abc has low or upper case symbols
		if(hasLower || hasUpper){
			//add all of them
			abc.addAll(upperCase);
			abc.addAll(lowerCase);
		}

		System.out.println("abc " + abc.size() + " : " + abc);
		//now create JSON file from it
		//write my own
		StringBuilder jsonStr = new StringBuilder("{\"alphabet\": {\"size\": " + abc.size() +",\"declaration\": \"");
		Iterator<Character> abcIter = abc.iterator();
		while(abcIter.hasNext()){
			char symb = abcIter.next();
			jsonStr.append(escapeMetaCharacters(String.valueOf(symb)));
			if(abcIter.hasNext()){
				jsonStr.append(","); //should be no spaced between abc symbols :(
			}
		}

		jsonStr.append("\"}, \"vertices\":[");
		Iterator<PrintConstraint> nIter = reachableNodes.iterator();
		while(nIter.hasNext()){
			PrintConstraint v = nIter.next();
			//System.out.println("v " + v + " av " + v.getActualVal() + " " + v.getValue());
			String vStr = "{\"num\": 0, \"actualValue\": \""+
					(v.getActualVal()!= null ? escapeMetaCharacters(v.getActualVal()) : "AbCd")
					+"\", \"incomingEdges\": "+getIncomingToString(v, graph) +
					", \"sourceConstraints\": [" + v.getSource().getId()+ "], \"timeStamp\": " + v.getTimeStamp() + ", \"type\": 0, \"id\": "+
					v.getId()+",\"value\": \""+ escapeMetaCharacters(v.getValue()) +"\"}";
			jsonStr.append(vStr);
			if(nIter.hasNext()){
				jsonStr.append(", ");
			}
		}
		jsonStr.append("]}");

		//System.out.println(jsonStr);

		String cleanderDirPath = "./graphs/real/cleaned/";
		String jsonFileName = new File(filepath).getName();
		int extIndex = jsonFileName.lastIndexOf('.');
		jsonFileName = jsonFileName.substring(0, extIndex);

		String graphJSONFilepath = String.format("%s/%s.json",
				cleanderDirPath,
				jsonFileName);

		try{
			Writer benchWriter = new FileWriter(graphJSONFilepath);
			benchWriter.write(jsonStr.toString());
			benchWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}

	}

	public static String escapeMetaCharacters(String inputString){
		final String[] metaCharacters = {"\\","\"", "\n"};
		//System.out.println("input string " + inputString);
		for (int i = 0 ; i < metaCharacters.length ; i++){
			if(inputString.contains(metaCharacters[i])){
				inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
			} else if (inputString.contains("\\u000d")){
				inputString = inputString.replace("\\u000d","A");
			}
		}
		return inputString;
	}

	private static String getIncomingToString(PrintConstraint v, DirectedGraph<PrintConstraint, SymbolicEdge> graph) {
		//System.out.println(v);
		StringBuilder ret = new StringBuilder("[");

		Iterator<SymbolicEdge> sourceIter = graph.incomingEdgesOf(v).iterator();
		while(sourceIter.hasNext()){
			SymbolicEdge s = sourceIter.next();
			PrintConstraint sN = (PrintConstraint) s.getASource();
			ret.append("{\"source\": " + sN.getId()+", \"type\": \"" + s.getType() +"\"}");
			if(sourceIter.hasNext()){
				ret.append(", ");
			}
		}
		ret.append("]");
		return ret.toString();
	}

	protected static Set<PrintConstraint> getAllParents(DirectedGraph<PrintConstraint, SymbolicEdge> revGraph, PrintConstraint vertex) {
		Set<PrintConstraint> parents = new HashSet<PrintConstraint>();
		BreadthFirstIterator<PrintConstraint, SymbolicEdge> breadthFirstIterator = new BreadthFirstIterator<>(revGraph, vertex);
		while (breadthFirstIterator.hasNext()) {
			parents.add(breadthFirstIterator.next());
		}
		return parents;
	}


}
