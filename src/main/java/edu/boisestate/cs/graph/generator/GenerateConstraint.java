package edu.boisestate.cs.graph.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import edu.boisestate.cs.graph.generator.Node.NTYPE;

public class GenerateConstraint {
	
	public static void main(String[] args){
		test5();
	}
	
	public static List<Node> addNodes(Node ... nodes){
		List<Node> allNodes = new ArrayList<Node>();
		for(Node oneNode : nodes){
			allNodes.add(oneNode);
		}
		return allNodes;
	}
	
	public static void test5(){
		char[] abc = {'A', 'B', 'C','a','b','c'};
		Node r1 = new Node("aBc", NTYPE.SYMB);
		Node r2 = new Node("Cab", NTYPE.SYMB);
		Node s1 = new Node("1", NTYPE.CONCR);
		Node s2 = new Node("3", NTYPE.CONCR);
		Node sub = new InnerNode("Bc", NTYPE.SUBSTR12, r1, s1, s2);
		Node c = new Node("c", NTYPE.CONCR);
		Node con1 = new InnerNode("Cabc", NTYPE.CONCAT, r2,c);
		Node con2 = new InnerNode("CabcBc", NTYPE.CONCAT, con1, sub);
		Node nEq = new InnerNode("false", NTYPE.ISEMPTY, con2);
		printJSON(addNodes(r1,r2,s1,s2,sub,c,con1,con2,nEq), "testDebug5", abc);
	}
	
	public static void test4(){
		char[] abc = {'A', 'B', 'C','a','b','c'};
		Node r1 = new Node("aBc", NTYPE.SYMB);
		Node r2 = new Node("Cab", NTYPE.SYMB);
		Node n3 = new InnerNode("aBcCab", NTYPE.CONCAT, r1, r2);
		Node c1 = new Node("", NTYPE.CONCR);
		Node n4 = new InnerNode("false", NTYPE.CONTAINS, n3, c1);
		printJSON(addNodes(r1,r2,n3,c1,n4), "testDebug4", abc);
	}
	
	public static void test3(){
		List<Node> allNodes = new ArrayList<Node>();
		char[] abc = {'A', 'B', 'C','a','b','c'};
		Node r1 = new Node("aBc", NTYPE.SYMB);
		Node s21 = new Node("2", NTYPE.CONCR);
		Node s22 = new Node("2", NTYPE.CONCR);
		Node n67 = new InnerNode("c", NTYPE.SUBSTR12, r1, s21, s22);
		Node d21 = new Node("0", NTYPE.CONCR);
		Node d22 = new Node("0", NTYPE.CONCR);
		Node n22 = new InnerNode("ab", NTYPE.DELETE, n67, d21,d22);
		
		Node r2 = new Node("aBC", NTYPE.SYMB);
		
		Node n150 = new InnerNode("abaBC", NTYPE.CONCAT, n22, r2);
		
		Node n180 = new InnerNode("false", NTYPE.ISEMPTY, n150);
		
		allNodes.add(r1);allNodes.add(s21);allNodes.add(s22);
		allNodes.add(n67); allNodes.add(d21);allNodes.add(d22);
		allNodes.add(n22); allNodes.add(n180); allNodes.add(r2); allNodes.add(n150);
		printJSON(allNodes, "testDebug3", abc);
	}
	
	public static void test2(){
		List<Node> allNodes = new ArrayList<Node>();
		char[] abc = {'A', 'B', 'C','a','b','c'};
		Node r1 = new Node("aBc", NTYPE.SYMB);
		Node n36 = new InnerNode("abc", NTYPE.TOLOWER, r1);
		Node s21 = new Node("2", NTYPE.CONCR);
		Node s22 = new Node("2", NTYPE.CONCR);
		Node n67 = new InnerNode("c", NTYPE.SUBSTR12,n36, s21, s22);
		Node nEmpty = new Node("", NTYPE.CONCR);
		
		Node nEq = new InnerNode("false", NTYPE.EQUALS, n67, nEmpty);
		
		allNodes.add(r1); allNodes.add(n36); allNodes.add(s21);
		allNodes.add(nEmpty); allNodes.add(n67); allNodes.add(s22);
		allNodes.add(nEq);
		printJSON(allNodes, "testDebug2", abc);
	}
	
	public static void test1(){
		List<Node> allNodes = new ArrayList<Node>();
		char[] abc = {'a','b','c'};
		Node r8 = new Node("abc", NTYPE.SYMB);
		Node r7 = new Node("abc", NTYPE.SYMB);
		Node d21 = new Node("2", NTYPE.CONCR);
		Node d22 = new Node("2", NTYPE.CONCR);
		Node d61 = new Node("6", NTYPE.CONCR);
		Node d62 = new Node("6", NTYPE.CONCR);
		Node b = new Node("b", NTYPE.CONCR);
		
		Node n17 = new InnerNode("abcb", NTYPE.CONCAT, r8, b);
		Node n22 = new InnerNode("ab", NTYPE.DELETE, r7, d21,d22);
		
		//Node n22Is = new InnerNode("false", NTYPE.ISEMPTY, n22);
		
		Node n59 = new InnerNode("abcbab", NTYPE.CONCAT, n17, n22);
		
		//Node n59Is = new InnerNode("false", NTYPE.ISEMPTY, n59);
		
		Node n64 = new InnerNode("abcba", NTYPE.DELETE, n59, d61, d62);
		
		Node n180 = new InnerNode("false", NTYPE.ISEMPTY, n64);
		allNodes.add(r8); allNodes.add(r7); allNodes.add(b);
		allNodes.add(d21); allNodes.add(d22); allNodes.add(d61);
		allNodes.add(d62); allNodes.add(n17); allNodes.add(n22);
		//allNodes.add(n22Is); 
		allNodes.add(n59); allNodes.add(n64);
		allNodes.add(n180); 
		//allNodes.add(n59Is);
		printJSON(allNodes, "testDebug", abc);
	}
	
	public static void printJSON(List<Node> nodes, String fileName, char[] abc){
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("{\"alphabet\": {\"size\": "+ abc.length+  ", \"declaration\": \"");
		for(int i=0; i < abc.length; i++){
			jsonStr.append(abc[i]);
			if(i + 1 < abc.length){
				jsonStr.append(","); //should be no spaced between abc symbols :(
			}
		}
		jsonStr.append("\"}, \"vertices\":");
		jsonStr.append(nodes.toString() +"}");

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

		String benchFileName = String.format("%s/%s.json", benchDirPath, fileName);

		try{
			Writer benchWriter = new FileWriter(benchFileName);
			benchWriter.write(jsonStr.toString());
			benchWriter.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

}
