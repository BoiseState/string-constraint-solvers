package edu.boisestate.cs.graph.generator;

import java.util.ArrayList;
import java.util.List;

public class Node {
	static int nodeId = 1;
	
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
	
	public Node(String actualValue){
		level = 0;
		this.id = nodeId;
		nodeId++;
		this.actualValue = actualValue;
		incoming = new ArrayList<Node>();
		//default value for the concrete init
		value = "\"" + actualValue +"\"!:!<init>";
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
