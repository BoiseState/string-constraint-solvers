/**
 * An interface for all constraint solvers, i.e., the wrapper. Constraints are incrementally analyzed based on
 * the order encountered in the program execution.
 */
package edu.boisestate.cs.old;

import java.util.HashMap;

public interface Solver {
	/**
	 * Passes a source vertex, i.e., a concrete or initial symbolic value to the constraint solver.
	 * @param value The string value of the vertex (e.g. a variable name).
	 * @param actualValue The actual value gathered during concrete execution.
	 * @param id A unique id. of the value.
	 */
	public void addRoot(String value, String actualValue, int id);

	/**
	 * Passes an operation to the constraint solver.
	 * @param value The name of the operation. The solver must distinguish the operation
	 * @param actualValue The actual result of the operation
	 * @param id The unique id of the operation
	 * @param sourceMap The id's of values used in the operation. "t" fetches the target, "s1" fetches the first argument, and so on.
	 */
	public void addOperation(String value, String actualValue, int id,
							 HashMap<String, Integer> sourceMap);

	/**
	 * Passes either a hotspot or branching point to the constraint solver.
	 * @param value The name of the hotspot/branching point. The solver must distinguish the hotspot/branching point.
	 * @param actualValue The actual result of the predicate
	 * @param id The unique id of the branching point.
	 * @param sourceMap The id's of values used in the branching point or hotspot. "t" fetches the target, "s1" fetches the first argument, and so on.
	 */
	public void addEnd(String value, String actualValue, int id,
					   HashMap<String, Integer> sourceMap);

	/**
	 * Prints stats gathered by the solver.
	 */
	public void getStats();
	/**
	 * Informs the solver that a value will not be used again.
	 * @param id The id of the value that will not be used again.
	 */
	public void remove(int id);
	/**
	 * Informs the solver to perform any final tasks before exiting.
	 */
	public void finishUp();
	/**Writes results to a file when applicable
	 * 
	 */
	public void writeToFile();

	/**
	 * Used when the solver cares about height for tracking the height of a flow graph.
	 * @param height
	 */
	public void addHeight(int height);

	/**
	 * Only used in recording the height of the flow graph. Most solvers return false.
	 * @return false, unless the solver cares about the height.
	 */
	public boolean caresAboutHeight();
}
