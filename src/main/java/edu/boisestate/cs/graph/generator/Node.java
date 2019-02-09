package edu.boisestate.cs.graph.generator;

import java.util.ArrayList;
import java.util.List;

public class Node {
	static int nodeId = 1;
	
	enum NTYPE {CONCR, SYMB, CONCAT, CONTAINS, REPLACE, DELETE, ISEMPTY, EQUALS, TOLOWER, SUBSTR12};
	
	/* a unique id of the node */
	protected int id;
	/* the list on nodes with incoming edges
	 * the first is the target and the rest are arguments
	 */
	private List<Node> incoming;

	/*
	 * The concrete value
	 */
	protected String actualValue;
	
	/*
	 * The type of the node
	 */
	protected NTYPE type;
	
	//the print value of the constraint
	protected String value;
	
	public Node(String actualValue, NTYPE type){
		this.id = nodeId;
		nodeId++;
		this.actualValue = actualValue;
		incoming = new ArrayList<Node>();
		this.type = type;
		
		switch(type){
		case CONCR :	value = "\\\"" + actualValue +"\\\"!:!<init>";
		break;
		case SYMB:  value = "r"+id +"!:!getStringValue!!";
		break;
		case CONCAT: value = "concat!!Ljava/lang/String;!:!0";
		break;
		case CONTAINS: value = "contains!!Ljava/lang/CharSequence;!:!0";
		break;
		case REPLACE:  value = "replace!!CC!:!0";
		break;
		case DELETE: value = "delete!!II!:!0";
		break;
		case ISEMPTY: value =  "isEmpty!!!:!0";
		break;
		case EQUALS: value = "equals!!Ljava/lang/Object;!:!0";
		break;
		case TOLOWER: value = "toLowerCase!!!:!0";
		break;
		case SUBSTR12: value = "substring!!II!:!0";
		break;
		}
	}
	
	public int getId(){
		return id;
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
	
	
	@Override
	public String toString(){
		//make in json format
		String ret = "{\"num\": 0, \"actualValue\": \""+
				actualValue +"\", \"incomingEdges\": "+getIncomingToString() +
				", \"sourceConstraints\": [], \"timeStamp\": " + 1482521433 + ", \"type\": 0, \"id\": "+
				id+",\"value\": \""+value +"\"}";
		
		return ret;
	}

	private String getIncomingToString() {
		StringBuilder ret = new StringBuilder("[");
		for(int i=0; i < incoming.size(); i++){
			Node n = incoming.get(i);
			ret.append("{\"source\": " + n.getId()+", \"type\": \"" + (i==0?"t":"s"+i) +"\"}");
			if(i + 1 < incoming.size()){
				ret.append(", ");
			}
		}
		ret.append("]");
		return ret.toString();
	}

}
