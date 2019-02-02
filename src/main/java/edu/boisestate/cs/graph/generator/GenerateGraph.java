package edu.boisestate.cs.graph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main method for generating graphs
 * @author elenasherman
 *
 */
public class GenerateGraph {

	
	public static void main(String[] args) {
		char[] abc = {'a','b','c'};
		int depth = 2; //min of zero and max of two operations on the target edge
		int size = 2; //the max size of a concrete string
		Set<String> operations = new HashSet<String>();
		operations.add("concat");
		Set<String> predicates = new HashSet<String>();
		predicates.add("contains");
		//a list of symbolic source nodes available
		List<Node> symbSource = new ArrayList<Node>();
		//a list of concrete nodes available
		List<Node> concrSource = new ArrayList<Node>();
		//a set of predicate nodes
		Set<Node> predicateNodes = new HashSet<Node>();
		//a map of symbolicly designed nodes and its level
		//level 0 is a special case ? or maybe not
		Map<Integer, List<Node>> levelOperations = new HashMap<Integer, List<Node>>();
		
		//need to popular symbSource and concrete source
		//concrete: create all strings up to size from abc
		//right now just stop at size 1
		for(char symb : abc){
			Node concrN = new Node(String.valueOf(symb));
			concrSource.add(concrN);
		}
		
		//create several symbolic nodes up to the size
		//should use combinatorial algorithms
		Node sybNode = new SymbNode("abc");
		symbSource.add(sybNode);
		sybNode = new SymbNode("bab");
		symbSource.add(sybNode);
		sybNode = new SymbNode("aaa");
		symbSource.add(sybNode);
		
		//for each level
		for(int l = 0; l <= depth; l++){
			//for each predicate
			Node n;
			List<Node> targets; //targets for that level
			if(l == 0){
				targets = symbSource;
			} else {
				targets = levelOperations.get(l);
			}
			for(String pred : predicates){
				
				switch (pred){
				case "contains" : 
						
						n = createContains(l, targets, concrSource);
						;
						
				default: n = new Node(String.valueOf(abc[0]));
				}
				
				predicateNodes.add(n);
			}
			
			//create new operation nodes at that level;
			for(String oper : operations){
				switch(oper){
				case "concat" : n = createConcat(l, targets, concrSource);
					
				}
			}
		}
	}

	private static Node createConcat(int level, List<Node> targets, List<Node> concrSource) {
		int tIndx = 0; // target index
		Node target = targets.get(tIndx);
		int aIndx = 0; // argument index
		Node arg = targets.get(aIndx);
		String actualVal = target.getActualValue().concat(arg.getActualValue());
		Concat ret = new Concat(level, actualVal, target, arg);
		return ret;
	}

	private static Node createContains( int level, List<Node> targets, List<Node> args) {
		//get the indexes for target and arg nodes
		int tIndx = 0; //can pick randomly
		Node target = targets.get(tIndx);
		int aIndx = 0;
		Node arg = targets.get(aIndx);
		String actualVal = target.getActualValue().contains(arg.getActualValue())?"true":"false";
		Contains ret = new Contains(level, actualVal, target, arg);
		return ret;
	}

}
