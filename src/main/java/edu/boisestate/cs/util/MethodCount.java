package edu.boisestate.cs.util;

import edu.boisestate.cs.SolveMain;
import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.graph.SymbolicEdge;
import org.jgrapht.DirectedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MethodCount {

	public static void main(String[] args) {

		String[] fileprefixes = args[0].split(",");
		Set<String> keySet = new TreeSet<String>();
		List<Map<String, Integer>> table = new ArrayList<Map<String,Integer>>();
		List<String> headers = new ArrayList<String>();
		for(String filepair : fileprefixes){
			System.out.println(filepair);
			String[] filepairArr = filepair.split(":");
			String fileprefix = filepairArr [0];
			int numFiles = Integer.parseInt(filepairArr[1]);
			for(int i = 1; i <= numFiles; i++){
				String filepath = fileprefix+(i < 10 ? "0"+i: i)+".json";
				headers.add(filepath);
				LambdaVoid1<String> printMinAlphabet = new LambdaVoid1<String>() {
					@Override
					public void execute(String s) {
						//System.out.printf("Minimum Alphabet: %s\n", s);
					}
				};
				try {
					DirectedGraph<PrintConstraint, SymbolicEdge> graph =
							SolveMain.loadGraph(filepath, printMinAlphabet);

					Map<String, Integer> countMap = new HashMap<String, Integer>();
					for(PrintConstraint pc : graph.vertexSet()){
						String key = pc.getSplitValue();
						if(!key.contains("!!") || key.startsWith("\"")){
							key = "other";
						}
						int count = 1;
						if(countMap.containsKey(key)){
							count += countMap.get(key);
						} 
						countMap.put(key, count);
						keySet.add(key);
					}  

					table.add(countMap);
				} catch (Exception e){
					System.out.println("Cannot find such file");
				}
			} //end for loop
		}
		System.out.print("methods \t");
		for(String key : keySet){
			System.out.print(key + "\t");
		}
		System.out.println();
		for(int i =0 ; i < headers.size(); i++){
			System.out.print(headers.get(i)+"\t");
			Map<String, Integer> entry = table.get(i);
			for(String key : keySet){
				int val = 0;
				if(entry.containsKey(key)){
					val = entry.get(key);
				}
				System.out.print(val + "\t");
			}
			System.out.println();
		}
	}

}
