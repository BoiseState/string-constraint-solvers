package edu.boisestate.cs.graph.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that repesents an append node
 * @author elenasherman
 *
 */
public class Concat extends Operation {

	//append has one argument;
	private Node arg;
	
	public Concat(int level, String actualValue, Node target, Node arg){
		super(level, actualValue, target);
		addToIncoming(arg);
		this.arg = arg;
		value = "concat!!Ljava/lang/String;!:!0";
	}
}
