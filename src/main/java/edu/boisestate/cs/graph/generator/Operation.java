package edu.boisestate.cs.graph.generator;

/**
 * The node of operation type
 * @author elenasherman
 *
 */
public class Operation extends Node{
	//any operation node must have a target;
	protected Node target;
	
	public Operation( int level, String actualValue, Node target){
		super(actualValue);
		this.level = level;
		addToIncoming(target);
	}
}
