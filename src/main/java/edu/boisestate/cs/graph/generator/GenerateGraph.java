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
	static int nodeId = 1;
	
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
		//a map of nodes and its level
		Map<Integer, List<Node>> levelOperations = new HashMap<Integer, List<Node>>();

		//for each level
		for(int l = 0; l <= depth; l++){
			//for each predicate
			for(String pred : predicates){
				Node n;
				List<Node> targets;
				if(l == 0){
					targets = symbSource;
				} else {
					targets = levelOperations.get(l);
				}
				switch (pred){
				case "contains" : 
						
						n = createContains(nodeId, l, targets, concrSource);
						;
				}
			}
		}
	}

	private static Node createContains(int id, int level, List<Node> targets, List<Node> args) {
		//get the indexes for target and arg nodes
		int tIndx = 0;
		Node target = targets.get(tIndx);
		int aIndx = 0;
		Node arg = targets.get(aIndx);
		String actualVal = target.getActualValue().contains(arg.getActualValue())?"true":"false";
		Contains ret = new Contains(id, level, actualVal, target, arg);
		return ret;
	}

}
