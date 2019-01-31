package edu.boisestate.cs.graph.generator;

import java.util.List;

public class Predicate extends Node {
	protected Node target;
	
	public Predicate(int id, int level, String actualValue, Node target){
		super(id, level, actualValue);
		this.target = target;
		addToIncoming(target);
	}
}
