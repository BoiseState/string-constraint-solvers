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
		char[] abc = {'A','B','C'};
		int depth = 1; //min of zero and max of two operations on the target edge
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
		Node sybNode = new SymbNode("A");
		symbSource.add(sybNode);
		sybNode = new SymbNode("BA");
		symbSource.add(sybNode);
		sybNode = new SymbNode("AA");
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
				case "contains" : n = createContains(l, targets, concrSource);
				break;
				default: n = new Node(String.valueOf(abc[0]));
				}

				predicateNodes.add(n);
			}

			//create new operation nodes at that level if needed
			if(l  < depth){
			List<Node> operSet = new ArrayList<Node>();
			for(String oper : operations){
				switch(oper){
				case "concat" : n = createConcat(l, targets, concrSource);
				break;
				default: n = new Node(String.valueOf(abc[0]));
				}
				operSet.add(n);
			}
			//add operations to that level
			levelOperations.put(Integer.valueOf(l+1), operSet);
			}
		}// end for depth

		//now let print them out in json format
		//		System.out.println(symbSource);
		//		System.out.println(concrSource);
		System.out.println(predicateNodes);
		//build one list of all nodes
		List<Node> allNodes = new ArrayList<Node>();
		allNodes.addAll(concrSource);
		allNodes.addAll(symbSource);
		for(List<Node> listLevel : levelOperations.values()){
			allNodes.addAll(listLevel);
		}
		allNodes.addAll(predicateNodes);

		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("{\"alphabet\": {\"size\": "+ abc.length+  ", \"declaration\": \"");
		for(int i=0; i < abc.length; i++){
			jsonStr.append(abc[i]);
			if(i + 1 < abc.length){
				jsonStr.append(","); //should be no spaced between abc symbols :(
			}
		}
		jsonStr.append("\"}, \"vertices\":");
		jsonStr.append(allNodes.toString() +"}");

		System.out.println(jsonStr);

	}

	private static Node createConcat(int level, List<Node> targets, List<Node> args) {
		int tIndx = 0; // target index
		Node target = targets.get(tIndx);
		int aIndx = 0; // argument index
		Node arg = args.get(aIndx);
		String actualVal = target.getActualValue().concat(arg.getActualValue());
		Concat ret = new Concat(level, actualVal, target, arg);
		return ret;
	}

	private static Node createContains( int level, List<Node> targets, List<Node> args) {
		//get the indexes for target and arg nodes
		int tIndx = 0; //can pick randomly
		Node target = targets.get(tIndx);
		int aIndx = 0;
		Node arg = args.get(aIndx);
		String actualVal = target.getActualValue().contains(arg.getActualValue())?"true":"false";
		Contains ret = new Contains(level, actualVal, target, arg);
		return ret;
	}

}
