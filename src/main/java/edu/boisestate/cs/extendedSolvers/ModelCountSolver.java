package edu.boisestate.cs.extendedSolvers;

import java.util.Set;

public abstract class ModelCountSolver<TSymbolicString> extends ExtendedSolver<TSymbolicString>{
	
	int bound;
	
	public ModelCountSolver(int setBound){
		bound = setBound;
		
	}
	
	//return the number of solutions for given
	//node in the graph
	public abstract int getModelCount(int id);
	
	//return a single value if exists
	//from the given number of the solutions
//	public String getSingleValue(int id);
	
	//enumerates all possible values for 
	//a given node
	public abstract Set<String> getAllVales(int id);


}
