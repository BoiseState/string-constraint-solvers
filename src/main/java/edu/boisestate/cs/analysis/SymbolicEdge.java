/**
 * Represents an edge in a SymbolicGraph
 * @author Scott Kausler
 */
package edu.boisestate.cs.analysis;

import edu.boisestate.cs.analysis.PrintConstraint;
import org.jgrapht.graph.DefaultEdge;

import java.lang.reflect.Field;

public class SymbolicEdge extends DefaultEdge{

	private static final long serialVersionUID = 1L;
	/**
	 * An additional type to be used as a label.
	 */
	private String type;

    public void setType(String type)
	{
		this.type=type;
	}
	public String getType(){
		return type;
	}
	
	public String toString(){
		return super.toString()+"("+type+")";
	}
	
	/**
	 * Wrapper used because getSource is protected.
	 * @return The result from getSource.
	 */
	public Object getASource(){
		return this.getSource();
	}
	/**
	 * Wrapper used because getTarget is protected.
	 * @return The result from getTarget.
	 */
	public Object getATarget(){
		return this.getTarget();
	}
}
