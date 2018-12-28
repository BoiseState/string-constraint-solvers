package edu.boisestate.cs.solvers;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class used to extend string constraint solvers.
 *
 * @param <TSymbolicString>
 *         The representation used to store a Path Condition.
 *
 * @author Scott Kausler
 */
public abstract class ExtendedSolver<TSymbolicString> {

    protected Map<Integer, String> concreteStringMap = new HashMap<>();
    protected int initialBound = -1;
    protected TSymbolicString last = null;
    protected TSymbolicString lastArg = null;
    protected int lastArgId = -1;
    protected int lastId = -1;
    protected Map<Integer, TSymbolicString> symbolicStringMap = new HashMap<>();

    public int getTempId() {
        return -1;
    }

    public ExtendedSolver() {
    }

    public ExtendedSolver(int initialBound) {

        // initialize bound from parameter value
        this.initialBound = initialBound;
    }

    /**
     * Checks if the parameter containsString a predicate method.
     *
     * @param string
     *         The name of the method to be checked.
     *
     * @return true if the parameter is a predicate.
     */
    public static boolean containsBoolFunction(String string) {
        String fName = string.split("!!")[0];
        return fName.equals("equals") ||
               fName.equals("contains") ||
               fName.equals("contentEquals") ||
               fName.equals("endsWith") ||
               fName.equals("startsWith") ||
               fName.equals("equalsIgnoreCase") ||
               fName.equals("matches") ||
               fName.equals("isEmpty") ||
               fName.equals("regionMatches");
    }

    /**
     * Interface for the {@link java.lang.StringBuilder#append(CharSequence,
     * int, int)} method.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param arg
     *         represents string argument.
     */
    public abstract void append(int id, int base, int arg, int start, int end);

    /**
     * Interface for the following methods: <ul> <li>{@link
     * java.lang.StringBuffer#append(StringBuffer)}</li> <li>{@link
     * java.lang.StringBuffer#append(boolean)}</li> <li>{@link
     * java.lang.StringBuffer#append(char)}</li> <li>{@link
     * java.lang.StringBuffer#append(char[])}</li> <li>{@link
     * java.lang.StringBuffer#append(char[], int, int)}</li> <li>{@link
     * java.lang.StringBuffer#append(CharSequence)}</li> <li>{@link
     * java.lang.StringBuffer#append(CharSequence, int, int)}</li> <li>{@link
     * java.lang.StringBuffer#append(double)}</li> <li>{@link
     * java.lang.StringBuffer#append(float)}</li> <li>{@link
     * java.lang.StringBuffer#append(int)}</li> <li>{@link
     * java.lang.StringBuffer#append(long)}</li> <li>{@link
     * java.lang.StringBuffer#append(Object)}</li> <li>{@link
     * java.lang.StringBuffer#append(String)}</li> <li>{@link
     * java.lang.StringBuffer#append(StringBuffer)}</li> <li>{@link
     * java.lang.StringBuilder#append(StringBuilder)}</li> <li>{@link
     * java.lang.StringBuilder#append(boolean)}</li> <li>{@link
     * java.lang.StringBuilder#append(char)}</li> <li>{@link
     * java.lang.StringBuilder#append(char[])}</li> <li>{@link
     * java.lang.StringBuilder#append(char[], int, int)}</li> <li>{@link
     * java.lang.StringBuilder#append(CharSequence)}</li> <li>{@link
     * java.lang.StringBuilder#append(CharSequence, int, int)}</li> <li>{@link
     * java.lang.StringBuilder#append(double)}</li> <li>{@link
     * java.lang.StringBuilder#append(float)}</li> <li>{@link
     * java.lang.StringBuilder#append(int)}</li> <li>{@link
     * java.lang.StringBuilder#append(long)}</li> <li>{@link
     * java.lang.StringBuilder#append(Object)}</li> <li>{@link
     * java.lang.StringBuilder#append(String)}</li> <li>{@link
     * java.lang.StringBuilder#append(StringBuffer)}</li> <li>{@link
     * java.lang.String#concat(String)}</li> </ul>
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param arg
     *         represents string argument.
     */
    public abstract void append(int id, int base, int arg);

    /**
     * Interface for the {@link java.lang.String#contains(CharSequence)}
     * method.
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     * @param arg
     *         represents string argument
     */
    public abstract void contains(boolean result, int base, int arg);

    /**
     * Interface for the {@link java.lang.StringBuffer#delete(int, int)} and
     * {@link java.lang.StringBuilder#delete(int, int)} methods
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void delete(int id, int base, int start, int end);

    /**
     * Interface for the {@link java.lang.StringBuffer#deleteCharAt(int)} and
     * {@link java.lang.StringBuilder#deleteCharAt(int)} methods.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void deleteCharAt(int id, int base, int loc);

    /**
     * Interface for the {@link java.lang.String#endsWith(String)} method.
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     * @param arg
     *         represents string argument
     */
    public abstract void endsWith(boolean result, int base, int arg);

    /**
     * Interface for the following methods: <ul> <li>{@link
     * java.lang.String#equals(Object)}</li>
     * <li>{@link java.lang.String#contentEquals(CharSequence)}</li>
     * <li>{@link java.lang.String#contentEquals(StringBuffer)}</li> </ul>
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     * @param arg
     *         represents string argument
     */
    public abstract void equals(boolean result, int base, int arg);

    /**
     * Interface for the {@link java.lang.String#equalsIgnoreCase(String)}
     * method.
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     * @param arg
     *         represents string argument
     */
    public abstract void equalsIgnoreCase(boolean result, int base, int arg);

    /**
     * Gets a satisfiable result for the symbolic string
     *
     * @param id
     *         represents string to test
     *
     * @return A satisfiable example
     */
    public abstract String getSatisfiableResult(int id);

    /**
     * Interface for the following methods: <ul> <li>{@link
     * java.lang.StringBuffer#insert(int, CharSequence)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, boolean)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, double)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, float)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, int)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, long)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, Object)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, String)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, CharSequence)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, boolean)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, double)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, float)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, int)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, long)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, Object)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, String)}</li> </ul>
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param arg
     *         represents string argument.
     */
    public abstract void insert(int id, int base, int arg, int offset);

    /**
     * Interface for the {@link java.lang.StringBuffer#insert(int, CharSequence,
     * int, int)} and {@link java.lang.StringBuilder#insert(int, CharSequence,
     * int, int)} methods.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param arg
     *         represents string argument.
     */
    public abstract void insert(int id,
                                int base,
                                int arg,
                                int offset,
                                int start,
                                int end);

    /**
     * Interface for the {@link java.lang.String#isEmpty()} method.
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     */
    public abstract void isEmpty(boolean result, int base);

    /**
     * Check if a symbolic string constraint is satisfiable
     *
     * @param id
     *         represents string to test
     *
     * @return true if the constraint is satisfiable, false otherwise.
     */
    public abstract boolean isSatisfiable(int id);

    /**
     * Check if a symbolic string value is a singleton
     *
     * @param id
     *         represents string to test
     *
     * @return true if the string is a singleton, false otherwise.
     */
    public abstract boolean isSingleton(int id);

    /**
     * Check if a symbolic string value is a singleton
     *
     * @param id
     *         represents string to test
     * @param actualValue
     *         Used to make evaluation of singleton quicker if the actual value
     *         is known.
     *
     * @return true if the string is a singleton, false otherwise.
     */
    public abstract boolean isSingleton(int id, String actualValue);

    /**
     * Check if the solver is sound by testing the result against an actual
     * value.
     *
     * @param id
     *         represents string to test
     * @param actualValue
     *         Value used to test soundness
     *
     * @return true if the value is sound, false otherwise.
     */
    public abstract boolean isSound(int id, String actualValue);

    /**
     * Creates a new concrete string using the representation of the solver,
     * e.g., "foo".
     *
     * @param id
     *         used to track result
     * @param string
     *         the concrete string to use
     */
    public abstract void newConcreteString(int id, String string);

    /**
     * Used to make a new symbolic string value
     *
     * @param id
     *         used to track the current symbolic string.
     */
    public abstract void newSymbolicString(int id);

    /**
     * Used to propagate string values in cases where a symbolic string is
     * unmodified in the following methods: <ul> <li>{@link
     * java.lang.String#copyValueOf(char[])}</li> <li>{@link
     * java.lang.String#copyValueOf(char[], int, int)}</li> <li>{@link
     * java.lang.String#intern()}</li>
     * <li>{@link java.lang.String#toString}</li>
     * <li>{@link java.lang.String#valueOf(boolean)}</li> <li>{@link
     * java.lang.String#valueOf(char)}</li>
     * <li>{@link java.lang.String#valueOf(char[])}</li>
     * <li>{@link java.lang.String#valueOf(char[], int, int)}</li> <li>{@link
     * java.lang.String#valueOf(double)}</li> <li>{@link
     * java.lang.String#valueOf(float)}</li>
     * <li>{@link java.lang.String#valueOf(int)}</li>
     * <li>{@link java.lang.String#valueOf(long)}</li> <li>{@link
     * java.lang.String#valueOf(Object)}</li> <li>{@link
     * java.lang.StringBuffer#toString()}</li> <li>{@link
     * java.lang.StringBuffer#trimToSize()}</li> <li>{@link
     * java.lang.StringBuilder#toString()}</li> <li>{@link
     * java.lang.StringBuilder#trimToSize()}</li> </ul>
     *
     * @param id
     *         represents result.
     * @param base
     *         represents value to propagate.
     */
    public abstract void propagateSymbolicString(int id, int base);

    /**
     * Interface for replace(char argOne, char argTwo) where both argument
     * values are known.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param find
     *         The character in the base string to replace.
     * @param replace
     *         The character used to replace the found character in the string.
     */
    public abstract void replaceCharKnown(int id,
                                          int base,
                                          char find,
                                          char replace);

    /**
     * Interface for replace(char argOne, char argTwo) where only the first
     * argument value, the character to replace in the string, is known.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param find
     *         The character in the base string to replace.
     */
    public abstract void replaceCharFindKnown(int id, int base, char find);

    /**
     * Interface for replace(char argOne, char argTwo) where only the second
     * argument value, the replacement character, is known.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param replace
     *         The character used to replace the found character in the string.
     */
    public abstract void replaceCharReplaceKnown(int id,
                                                 int base,
                                                 char replace);

    /**
     * Interface for replace(char argOne, char argTwo) where both argument
     * values are not known.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void replaceCharUnknown(int id, int base);

    /**
     * Interface for replace(String argOne, String argTwo).
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param argOne
     *         represents string to replace.
     * @param argTwo
     *         represents replacement string.
     */
    public abstract void replaceStrings(int id,
                                        int base,
                                        int argOne,
                                        int argTwo);

    /**
     * Replaces possible invalid characters in the input value. Introduces
     * over-approximation by mapping some characters to others.
     *
     * @param value
     *         string with potentially invalid characters
     *
     * @return argument sanitized from invalid characters
     */
    public abstract String replaceEscapes(String value);

    /**
     * Interface for the {@link java.lang.StringBuffer#reverse()} and {@link
     * java.lang.StringBuilder#reverse()} methods.
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void reverse(int id, int base);

    /**
     * Interface for setCharAt(int offset, char ch)
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     * @param arg
     *         represents character argument.
     */
    public abstract void setCharAt(int id, int base, int arg, int offset);

    /**
     * Interface for setLength(int length)
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void setLength(int id, int base, int length);

    /**
     * Used when a solver requires some sort of shut down task. For example
     * EZ3-str needs to shut down its executor
     */
    public abstract void shutDown();

    /**
     * Interface for startsWith(String arg)
     *
     * @param result
     *         used to assert the true or false branch
     * @param base
     *         represents calling string
     * @param arg
     *         represents string argument
     */
    public abstract void startsWith(boolean result, int base, int arg);

    /**
     * Interface for substring(int start)
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void substring(int id, int base, int start);

    /**
     * Interface for substring(int start, int end)
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void substring(int id, int base, int start, int end);

    /**
     * Interface for toLowerCase()
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void toLowerCase(int id, int base);

    /**
     * Interface for toUpperCase()
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void toUpperCase(int id, int base);

    /**
     * Interface for trim()
     *
     * @param id
     *         represents result.
     * @param base
     *         represents calling string.
     */
    public abstract void trim(int id, int base);

    /**
     * Used to get the value currently stored in the symbolic string map for the
     * given id.
     *
     * @param id
     *         id used to get the value to return.
     *
     * @return the symbolic string value represented by the id.
     */
    public TSymbolicString getValue(int id) {
        return symbolicStringMap.get(id);
    }

    /**
     * Used to check if the values involved are capable of being used in the
     * coming operations. For example, in EStranger, the time required to solve
     * constraints grows as more transitions appear in the automata.
     *
     * @param base
     *         representation of calling string
     * @param arg
     *         representation of argument string
     *
     * @return boolean value indicating if the symbolic values can be used in
     * the coming operations.
     */
    public boolean isValidState(int base, int arg) {
        return true;
    }

    /**
     * Remove a symbolic string that won't be used anymore.
     *
     * @param id
     *         represents string that won't be used anymore.
     */
    public void remove(int id) {
        if (symbolicStringMap != null && symbolicStringMap.containsKey(id)) {
            symbolicStringMap.remove(id);
        }
    }

    /**
     * Used to undo the last predicate applied. Useful for checking if the
     * branch is satisfiable without actually applying the predicate.
     */
    public void revertLastPredicate() {
        if (last == null) {
            throw new IllegalStateException();
        }
        symbolicStringMap.put(lastId, last);
        last = null;
        lastId = -1;

        if (lastArg != null) {
            symbolicStringMap.put(lastArgId, lastArg);
            lastArg = null;
            lastArgId = -1;
        }
        
    }

    /**
     * Sets the last base and argument for reverting the last predicate.
     *
     * @param base
     *         id of the current base.
     * @param arg
     *         id of the current arg
     */
    public void setLast(int base, int arg) {
        last = symbolicStringMap.get(base);
        lastId = base;
        if (arg > 0) {
            lastArg = symbolicStringMap.get(arg);
            lastArgId = arg;
        }
    }
}
