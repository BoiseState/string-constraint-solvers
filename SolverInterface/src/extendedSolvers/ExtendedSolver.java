package extendedSolvers;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class used to extend string constraint solvers.
 * @author Scott Kausler
 *
 * @param <T> The representation used to store a Path Condition.
 */
public abstract class ExtendedSolver<T> {
	
	protected Map<Integer, T> symbolicStringMap = new HashMap<Integer, T>();
	protected T last = null;
	protected T lastArg = null;
	
	protected int lastId = -1;
	protected int lastArgId = -1;
	
	/**
	 * Used to check if the values involved are capable of being used in the comming operations.
	 * For example, in EStranger, the time required to solve constraints grows as more transitions appear in the automata.
	 * @param base representation of calling string
	 * @param arg representation of argument string
	 * @return
	 */
	public boolean isValidState(int base, int arg){
		return true;
	}
	
	/**
	 * Used to make a new symbolic string value
	 * @param id used to track the current symbolic string.
	 */
	public abstract void newSymbolicString(int id);
	
	/**
	 * Creates a new concrete string using the representation of the solver, e.g., "foo".
	 * @param id used to track result
	 * @param string the concrete string to use
	 */
	public abstract void newConcreteString(int id, String string);
	
	/**
	 * Replaces possible invalid characters in the input value. 
	 * Introduces over-approximation by mapping some characters to others.
	 * @param value string with potentially invalid characters
	 * @return argument sanitized from invalid characters
	 */
	public abstract String replaceExcapes(String value);

	/**
	 * Used to propagate string values in cases where a symbolic
	 * string is unmodified, i.e., toString().
	 * @param id represents result.
	 * @param base represents value to propagate.
	 */
	public abstract void propagateSymbolicString(int id, int base);
	
	/**
	 * Interface for StringBuilder.append(CharSequence s, int start, int end).
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param arg represents string argument.
	 */
	public abstract void append(int id, int base, int arg, int start, int end);
	
	/**
	 * Interface for StringBuilder.append(CharSequence s). and String.concat(String)
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param arg represents string argument.
	 */
	public abstract void append(int id, int base, int arg);
	
	/**
	 * Interface for substring(int start)
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void substring(int id, int base, int start);
	
	/**
	 * Interface for substring(int start, int end)
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void substring(int id, int base, int start, int end);
	
	/**
	 * Interface for setLength(int length)
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void setLength(int id, int base, int length);
	
	/**
	 * Interface for insert(int offset, String str)
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param arg represents string argument.
	 */
	public abstract void insert(int id, int base, int arg, int offset);
	
	/**
	 * Interface for insert(int offset, String str, int start, int end)
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param arg represents string argument.
	 */
	public abstract void insert(int id, int base, int arg, int offset, int start, int end);
	
	/**
	 * Interface for setCharAt(int offset, char ch)
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param arg represents charactoer argument.
	 */
	public abstract void setCharAt(int id, int base, int arg, int offset);
	
	/**
	 * Interface for trim()
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void trim(int id, int base);
	
	/**
	 * Interface for delete(int start, int end)
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void delete(int id, int base, int start, int end);
	
	/**
	 * Interface for deleteCharAt(int loc)
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void deleteCharAt(int id, int base, int loc);
	
	/**
	 * Interface for reverse()
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void reverse(int id, int base);
	
	/**
	 * Interface for toUpperCase()
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void toUpperCase(int id, int base);
	
	/**
	 * Interface for toLowerCase()
	 * @param id represents result.
	 * @param base represents calling string.
	 */
	public abstract void toLowerCase(int id, int base);

	/**
	 * Interface for replace(String argOne, String argTwo)
	 * @param id represents result.
	 * @param base represents calling string.
	 * @param argOne represents string to replace.
	 * @param argTwo represents replacement string.
	 */
	public abstract void replace(int id, int base, int argOne, int argTwo);
	
	/**
	 * Interface for contains(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 * @param arg represents string argument
	 */
	public abstract void contains(boolean result, int base, int arg);

	/**
	 * Interface for endsWith(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 * @param arg represents string argument
	 */
	public abstract void endsWith(boolean result, int base, int arg);
	
	/**
	 * Interface for startsWith(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 * @param arg represents string argument
	 */
	public abstract void startsWith(boolean result, int base, int arg);
	
	/**
	 * Interface for equals(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 * @param arg represents string argument
	 */
	public abstract void equals(boolean result, int base, int arg);
	
	/**
	 * Interface for equalsIgnoreCase(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 * @param arg represents string argument
	 */
	public abstract void equalsIgnoreCase(boolean result, int base, int arg);
	
	/**
	 * Interface for isEmpty(String arg)
	 * @param result used to assert the true or false branch
	 * @param base represents calling string
	 */
	public abstract void isEmpty(boolean result, int base);

	/**
	 * Gets a statisfiable result for the symbolic string
	 * @param id represents string to test
	 * @return A satisfiable example
	 */
	public abstract String getSatisfiableResult(int id);
	
	/**
	 * Check if a symbolic string constraint is satisfiable
	 * @param id represents string to test
	 * @return true if the constraint is satisfiable, false otherwise.
	 */
	public abstract boolean isSatisfiable(int id);
	
	/**
	 * Check if a symbolic string value is a singleton
	 * @param id represents string to test
	 * @return true if the string is a singleton, false otherwise.
	 */
	public abstract boolean isSingleton(int id);
	
	/**
	 * Check if a symbolic string value is a singleton
	 * @param id represents string to test
	 * @param actualValue Used to make evaluation of singleton quicker if the actual value is known.
	 * @return true if the string is a singleton, false otherwise.
	 */
	public abstract boolean isSingleton(int id, String actualValue);
	
	/**
	 * Check if the solver is sound by testing the result against an actual value.
	 * @param id represents string to test
	 * @param actualValue Value used to test soundness
	 * @return true if the value is sound, false otherwise.
	 */
	public abstract boolean isSound(int id, String actualValue);

	public int getTempId(){
		return -1;
	}
	
	/**
	 * Remove a symbolic string that won't be used anymore.
	 * @param id represents string that won't be used anymore.
	 */
	public void remove(int id){
		if(symbolicStringMap != null && symbolicStringMap.containsKey(id)){
			symbolicStringMap.remove(id);
		}
	}
	
	/**
	 * Checks if the parameter contains a predicate method.
	 * @param string The name of the method to be checked.
	 * @return true if the parameter is a predicate.
	 */
	public static boolean containsBoolFunction(String string){
		String fName=string.split("!!")[0];
		if(fName.equals("equals")||fName.equals("contains")||fName.equals("contentEquals")||fName.equals("endsWith")||fName.equals("startsWith")
				||fName.equals("equalsIgnoreCase")||fName.equals("matches")||fName.equals("isEmpty")||fName.equals("regionMathes"))
			return true;
		return false;
	}
	
	/**
	 * Sets the last base and argument for reverting the last predicate.
	 * @param base id of the current base.
	 * @param arg id of the current arg
	 */
	protected void setLast(int base, int arg) {
		last = symbolicStringMap.get(base);
		lastId = base;
		if(arg > 0) {
			lastArg = symbolicStringMap.get(arg);
			lastArgId = arg;
		}
	}
	
	/**
	 * Used to undo the last predicate applied. Useful for checking 
	 * if the branch is satisfiable without actually applying the predicate.
	 */
	public void revertLastPredicate(){
		if(last == null){
			throw new IllegalStateException();
		}
		symbolicStringMap.put(lastId, last);
		last = null;
		lastId = -1;
		
		if(lastArg !=null){
			symbolicStringMap.put(lastArgId, lastArg);
			lastArg = null;
			lastArgId = -1;
		}
	}

	/**
	 * Used to get the value currently stored in the symbolic string map for the given id.
	 * @param id id used to get the value to return.
	 * @return
	 */
	public T getValue(int id) {
		return symbolicStringMap.get(id);
	}

	/**
	 * Used when a solver requires some sort of shut down task. For example EZ3-str needs to shut down its executor
	 */
	public abstract void shutDown();
}
