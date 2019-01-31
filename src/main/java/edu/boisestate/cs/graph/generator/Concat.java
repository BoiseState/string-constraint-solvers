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
	
	public Concat(int id, int level, String actualValue, Node target, Node arg){
		super(id, level, actualValue, target);
		addToIncoming(arg);
		value = "concat!!Ljava/lang/String;!:!0";
	}

	@Override
	public List<Node> getIncoming() {
		// TODO Auto-generated method stub
		List<Node> ret = new ArrayList<Node>();
		ret.add(target);
		ret.add(arg);
		return ret;
	}
}
