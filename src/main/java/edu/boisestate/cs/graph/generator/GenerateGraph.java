package edu.boisestate.cs.graph.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.boisestate.cs.graph.generator.Node.NTYPE;

/**
 * The main method for generating graphs
 * @author elenasherman
 *
 */
public class GenerateGraph {
	private static Random rand = new Random(5);

	public static void main(String[] args) {
		char[] abc = {'A','B', 'C', 'a','b', 'c'};
		int depth = 3; //min of zero and max of two operations on the target edge
		int size = 3; //the max size of a concrete string
		Set<String> operations = new HashSet<String>();
		operations.add("concat");
		operations.add("replace");
		operations.add("delete");
		operations.add("toLowerCase");
		operations.add("substring12");
		Set<String> predicates = new HashSet<String>();
		predicates.add("contains");
		predicates.add("isEmpty");
		predicates.add("equals");
		//a list of symbolic source nodes available
		List<Node> symbSource = new ArrayList<Node>();
		//a list of concrete nodes available
		List<Node> concrSource = new ArrayList<Node>();
		// a set of nodes that represent integer values (for delete args)
		List<Node> intSource = new ArrayList<Node>();
		//a set of predicate nodes
		Set<Node> predicateNodes = new HashSet<Node>();
		//a map of symbolically designed nodes and its level
		//level 0 is a special case ? or maybe not
		Map<Integer, List<Node>> levelOperations = new HashMap<Integer, List<Node>>();

		//need to popular symbSource and concrete source
		//concrete: create all strings up to size from the abc
		//right now just stop at size 1
		for(char symb : abc){
			Node concrN = new Node(String.valueOf(symb), NTYPE.CONCR);
			concrSource.add(concrN);
		}//late could also use generateString method

		//create several symbolic nodes up to the size
		//should use combinatorial algorithms
		//how many symbolic nodes we would need?
		//it should depend on the number of 
		//operations because each operation
		//would result in a chain, e.g, concat-> concat or concat->delete
		//actually can do it with one single symbolic value, but it is ok 
		//to have such more even distribution
		for(int i=0; i < 5; i++){
			//generate a random string from the given abc up to length size
			//smaller size might end up with UNSAT/UNSAT, e.g., deletion operations
			String concreteVal = generateString(size, abc);
			Node symbNode = new Node(concreteVal, NTYPE.SYMB);
			symbSource.add(symbNode);
		}
		levelOperations.put(0, symbSource);

		//for each level create a set of final
		//symbolic values that one can use to
		//query predicates
		for(int l = 0; l < depth; l++){
			Node n;
			List<Node> targets = levelOperations.get(l);
			//create new operation nodes at that level if needed

			List<Node> operSet = new ArrayList<Node>();
			for(int i = 0 ; i < 10; i++){
				for(String oper : operations){
					switch(oper){
					case "concat" : n = createConcat(targets, concrSource);
					break;
					case "replace" : n = createReplace(targets, concrSource);
					break;
					case "delete" : n = createDelete(targets, intSource);
					break;
					case "toLowerCase" : n = createToLowerCase(targets);
					break;
					case "substring12" : n = createSubstring12(targets, intSource);
					break;
					default: n = new Node(String.valueOf(abc[0]), NTYPE.CONCR);
					}
					operSet.add(n);
				}

				//add operations to that level
				levelOperations.put(Integer.valueOf(l+1), operSet);
			}
		}// end for depth

		//after generating operations add on predicates
		for(int l = 0; l <= depth; l++){
			//for each predicate
			Node n = null;
			List<Node> targets = new ArrayList<Node>();
			targets.addAll(levelOperations.get(l));
			while(!targets.isEmpty()){
				for(String pred : predicates){
					if(!targets.isEmpty()){
					switch (pred){
					case "contains" : n = createContains(targets, concrSource,abc);
					break;
					case "isEmpty" : n = createIsEmpty(targets);
					break;
					case "equals" : n = createEquals(targets, concrSource, abc);
					break;
					default: n = new Node(String.valueOf(abc[0]), NTYPE.CONCR);
					}
					}
					predicateNodes.add(n);
				}
			}
		}

		List<Node> allNodes = new ArrayList<Node>();
		allNodes.addAll(concrSource);
		allNodes.addAll(intSource);
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
		//need to write it to a file
		String benchDirPath = String.format("graphs/benchmarks");
		File benchDir = new File(benchDirPath);
		if(benchDir.exists() && benchDir.isFile()){
			benchDir.delete();
			benchDir.mkdir();
		} else if (! benchDir.exists()){
			benchDir.mkdir();
		}

		String used = "";
		for(String op : operations){
			used += op+"_";
		}

		for(String pred: predicates){
			used+= pred + "_";
		}
		String benchFileName = String.format("%s/%sl%d_d%d_bench.json", benchDirPath, used, size, depth);

		try{
			Writer benchWriter = new FileWriter(benchFileName);
			benchWriter.write(jsonStr.toString());
			benchWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private static Node createSubstring12(List<Node> targets, List<Node> intSource) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		String targetVal = target.getActualValue();
		//get its concrete value's size
		int maxIndx = targetVal.length();
		//generate two numbers for the substring args
		int aIndx1 = rand.nextInt(maxIndx+1); // argument index can be the same as the length of the stirng
		int aIndx2 = rand.nextInt(maxIndx + 1 - aIndx1) + aIndx1;
		String actualVal = targetVal.substring(aIndx1, aIndx2);
		String aStr1 = String.valueOf(aIndx1);
		//find the appropriate nodes or create if none exist
		Node arg1 = findOrAdd(aStr1, intSource, null);
		String aStr2 = String.valueOf(aIndx2);
		Node arg2 = findOrAdd(aStr2, intSource, arg1);//make sure a node is different for that one
		InnerNode ret = new InnerNode(actualVal, NTYPE.SUBSTR12, target, arg1, arg2);
		return ret;
	}

	private static Node createToLowerCase(List<Node> targets) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		String actualVal = target.getActualValue().toLowerCase();
		InnerNode ret = new InnerNode(actualVal, NTYPE.TOLOWER, target);
		return ret;
	}

	private static Node createEquals(List<Node> targets, List<Node> concrSource, char[] abc) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		targets.remove(tIndx);
		//create a new concrete source for predicates, otherwise they will
		//be overwritten, possible with the empty language
		Node concrN = new Node(generateStringUpTo(2, abc), NTYPE.CONCR);
		concrSource.add(concrN);
		String actualVal = target.getActualValue().equals(concrN.getActualValue()) ? "true":"false";
		InnerNode ret = new InnerNode(actualVal, NTYPE.EQUALS, target, concrN);
		return ret;
	}

	private static Node createIsEmpty(List<Node> targets) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		targets.remove(tIndx);
		String actualVal = target.getActualValue().isEmpty()?"true":"false";
		InnerNode ret = new InnerNode(actualVal, NTYPE.ISEMPTY, target);
		return ret;
	}

	private static Node createDelete(List<Node> targets, List<Node> intSource) {
		//get a target randomly
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		String targetVal = target.getActualValue();
		//get its concrete value's size
		int maxIndx = targetVal.length();
		//generate two numbers for the delete args
		int aIndx1 = rand.nextInt(maxIndx+1); // argument index can be the same as the length of the string
		//make sure the second index is a valid one
		int aIndx2 = rand.nextInt(maxIndx + 1 - aIndx1) + aIndx1;
		StringBuffer actualVal = new StringBuffer(targetVal);
		actualVal.delete(aIndx1, aIndx2);
		String aStr1 = String.valueOf(aIndx1);
		//find the appropriate nodes or create if none exist
		Node arg1 = findOrAdd(aStr1, intSource, null);
		String aStr2 = String.valueOf(aIndx2);
		Node arg2 = findOrAdd(aStr2, intSource, arg1);//make sure a node is different for that one
		InnerNode ret = new InnerNode(actualVal.toString(), NTYPE.DELETE, target, arg1, arg2);
		return ret;
	}

	private static Node findOrAdd(String val, List<Node> intSource, Node exclude){
		Node ret = null;
		for(Node n : intSource){
			if(n.getActualValue().equals(val) && !n.equals(exclude)){
				ret = n;
				break;
			}
		}
		if(ret == null){
			//then create a new node and add it to the list
			ret = new Node(val, NTYPE.CONCR);
			intSource.add(ret);
		}

		return ret;
	}

	private static String generateString(int size, char[] abc) {
		StringBuilder ret = new StringBuilder();
		for(int i=0; i < size; i++){
			//randomly pick a value from a the alphabet
			ret.append(abc[rand.nextInt(abc.length)]);
		}
		return ret.toString();
	}
	
	private static String generateStringUpTo(int maxSize, char[] abc){
		//find a radnom number
		int size = rand.nextInt(maxSize+1);
		return generateString(size, abc);
	}

	private static Node createReplace(List<Node> targets, List<Node> args) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		int aIndx1 = rand.nextInt(args.size()); // argument index
		Node arg1 = args.get(aIndx1);
		int aIndx2 = rand.nextInt(args.size()); // argument index
		//make sure the indicies of two arguments are not the same
		while(aIndx1 == aIndx2){
			aIndx2 = rand.nextInt(args.size());
		}
		Node arg2 = args.get(aIndx2);
		String actualVal = target.getActualValue().replace(arg1.getActualValue(), arg2.getActualValue());
		InnerNode ret = new InnerNode(actualVal, NTYPE.REPLACE, target, arg1, arg2);
		return ret;
	}



	private static Node createConcat(List<Node> targets, List<Node> args) {
		int tIndx = rand.nextInt(targets.size()); // target index
		Node target = targets.get(tIndx);
		//symbolic or concrete
		Node arg;
		if(rand.nextBoolean()){
			int aIndx = rand.nextInt(args.size()); // argument index
			arg = args.get(aIndx);
		} else {
			int aIndx = rand.nextInt(targets.size()); // argument index
			while(aIndx == tIndx){ // it is not a multi-graph: cannot have two edges between the same nodes.
				aIndx = rand.nextInt(targets.size());
			}
			arg = targets.get(aIndx);
		}
		
		String actualVal = target.getActualValue().concat(arg.getActualValue());
		InnerNode ret = new InnerNode(actualVal, NTYPE.CONCAT, target, arg);
		return ret;
	}

	private static Node createContains(List<Node> targets, List<Node> args, char[] abc) {
		//get the indexes for target and arg nodes
		int tIndx = rand.nextInt(targets.size()); //can pick randomly
		Node target = targets.get(tIndx);
		targets.remove(tIndx);
		Node concrN = new Node(generateStringUpTo(2, abc), NTYPE.CONCR);
		args.add(concrN);
		//System.out.println(String.format("%d, %d, %d, %d", targets.size(), tIndx, args.size(), aIndx));
		String actualVal = target.getActualValue().contains(concrN.getActualValue())?"true":"false";
		InnerNode ret = new InnerNode(actualVal, NTYPE.CONTAINS, target, concrN);
		return ret;
	}

}
