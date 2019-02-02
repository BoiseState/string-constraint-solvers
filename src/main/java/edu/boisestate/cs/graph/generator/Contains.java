package edu.boisestate.cs.graph.generator;

/**
 * Class for contains method
 * @author elenasherman
 *
 */
public class Contains extends Predicate {
	private Node arg;
	
	public Contains(int level, String actualValue, Node target, Node arg){
		super(level, actualValue, target);
		addToIncoming(arg);
		this.arg = arg;
		value = "contains!!Ljava/lang/CharSequence;!:!0";
	}
}
