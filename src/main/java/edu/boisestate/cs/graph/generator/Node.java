package edu.boisestate.cs.graph.generator;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	/* a unique id of the node */
	protected int id;
	/* the list on nodes with incoming edges
	 * the first is the target and the rest are arguments
	 */
	private List<Node> incoming;
	/*
	 * the level in the constraint graph
	 */
	protected int level;
	/*
	 * The concrete value
	 */
	protected String actualValue;
	
	//the print value of the constraint
	protected String value;
	
	public Node(int id,  int level, String actualValue){
		this.id = id;
		this.level = level;
		this.actualValue = actualValue;
		incoming = new ArrayList<Node>();
	}
	
	/**
	 * 
	 * @return a list on incoming nodes if any
	 */
	public List<Node> getIncoming(){
		return incoming;
	}
	
	/*
	 * Printed 
	 */
	public  String getValue(){
		return value;
	}
	
	public String getActualValue(){
		return actualValue;
	}
	
	protected void addToIncoming(Node n){
		incoming.add(n);
	}

}
