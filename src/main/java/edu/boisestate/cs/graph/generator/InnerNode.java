package edu.boisestate.cs.graph.generator;

public class InnerNode extends Node{

	public InnerNode(String actualValue, NTYPE type,  Node target) {
		super(actualValue, type);
		addToIncoming(target);
	}
	
	public InnerNode(String actualValue, NTYPE type, Node target, Node ... args){
		super(actualValue,type);
		addToIncoming(target);
		for(Node arg : args){
			addToIncoming(arg);
		}
	}

}
