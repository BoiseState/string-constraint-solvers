package edu.boisestate.cs.graph.generator;

public class SymbNode extends Node{

	public SymbNode(String actualValue) {
		super(actualValue);
		value = "r"+id +"!:!getStringValue!";
	}

}
