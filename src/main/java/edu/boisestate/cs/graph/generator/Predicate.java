package edu.boisestate.cs.graph.generator;

public class Predicate extends Node {
	protected Node target;
	
	public Predicate(int level, String actualValue, Node target){
		super(actualValue);
		this.level = level;
		this.target = target;
		addToIncoming(target);
	}
}
