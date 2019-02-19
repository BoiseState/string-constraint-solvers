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
//        	System.out.println("v " + v);
//        	System.out.println("e " + graph.outgoingEdgesOf(v));
        	if(v.getSplitValue().startsWith("equals") ||
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
        Set<Character> abc = new HashSet<Character>();
        for(PrintConstraint v : reachableNodes){
        	//System.out.println("type " + v.getType() + "\t val " + v.getSplitValue());
        	String val = v.getSplitValue();
        	if(val.length() > 2 && val.startsWith("\"")){
        		//means it is a concerete value that should be included in the alphabet
        		String chars = val.split("\"")[1];
        		String low = chars.toLowerCase();
        		String upper = chars.toUpperCase();
        		//merge them
        		chars = low+upper;
        		//System.out.println("Sybms " + chars);
        		for(int i=0; i < chars.length(); i++){
        			abc.add(chars.charAt(i));
        		}
        	}
        }
        //add all other symbols?
        //System.out.println("The min abc is " + abc);
        for(int i = 97; i <= 122; i++){
        	abc.add((char)i);
        }
        
        //add upper case
        for(int i = 65; i <= 90; i++){
        	abc.add((char)i);
        }
        
        System.out.println("abc " + abc.size() + " : " + abc);
        //now create JSON file from it
        //write my own
        StringBuilder jsonStr = new StringBuilder("{\"alphabet\": {\"size\": " + abc.size() +",\"declaration\": \"");
        Iterator<Character> abcIter = abc.iterator();
        while(abcIter.hasNext()){
        	char symb = abcIter.next();
        	jsonStr.append(symb);
			if(abcIter.hasNext()){
				jsonStr.append(","); //should be no spaced between abc symbols :(
			}
        }
   
		jsonStr.append("\"}, \"vertices\":[");
		Iterator<PrintConstraint> nIter = reachableNodes.iterator();
		while(nIter.hasNext()){
			PrintConstraint v = nIter.next();
			String vStr = "{\"num\": 0, \"actualValue\": \""+
					v.getActualVal() +"\", \"incomingEdges\": "+getIncomingToString(v, graph) +
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
        final String[] metaCharacters = {"\\","\""};

        for (int i = 0 ; i < metaCharacters.length ; i++){
            if(inputString.contains(metaCharacters[i])){
                inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
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
