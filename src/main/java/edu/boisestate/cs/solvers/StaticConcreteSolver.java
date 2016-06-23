package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class StaticConcreteSolver
        extends ExtendedSolver<ConcreteValues>
        implements ModelCountSolver {

    //this set keeps track of id's that should not
    //be computed because the input was not
    //feasible for that path
    //Set<Integer> infeasible = new HashSet<Integer>();
    //this set never decreases in size
    //this set is only updated on the predicates methods
    //each method first need to check weather
    //id is in the infeasible set
    //if it is then don't do any calculations
    //and do not update the count for that id, i.e., make it 0

    //	public boolean isFeasible(int id){
    //		//return !infeasible.containsString(id);
    //		return symbolicStringMap.get(id).isFeasible();
    //	}

    //in debug mode it will check weather the resulting
    //value is the same as the actual value collected
    //only applicable in re-run mode
    public static boolean DEBUG = true;
    private final Alphabet alphabet;

    public StaticConcreteSolver(Alphabet alphabet, int setBound) {
        super(setBound);

        this.alphabet = alphabet;
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // perform substring on arg values
        ConcreteValues substr = argValues.substring(start, end);

        // perform concatenation, equivalent to append
        ConcreteValues results = baseValues.concat(substr);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void append(int id, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // perform concatenation, equivalent to append
        ConcreteValues results = baseValues.concat(argValues);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    /*
     * Now we have predicates
     */
    @Override
    public void contains(boolean result, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertContainsOther(argValues);

            // get satisfying arg values
            argValues = argValues.assertContainedInOther(baseValues);
        }
        // false branch
        else {
            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotContainsOther(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotContainedInOther(baseValues);

            // set base values from temp values
            baseValues = tempValues;
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void delete(int id, int base, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform delete
        ConcreteValues results = baseValues.delete(start, end);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform delete character at index
        ConcreteValues results = baseValues.deleteCharAt(loc);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void endsWith(boolean result, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertEndsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertContainedInEnding(baseValues);
        }
        // false branch
        else {
            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEndsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotContainedInEnding(baseValues);

            // set base values from temp values
            baseValues = tempValues;
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void equals(boolean result, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertEqual(argValues);

            // get satisfying arg values
            argValues = baseValues.copy();
        }
        // false branch
        else {
            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEqual(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotEqual(baseValues);

            // set base values from temp values
            baseValues = tempValues;
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertEqualIgnoreCase(argValues);

            // get satisfying arg values
            argValues = argValues.assertEqualIgnoreCase(baseValues);
        }
        // false branch
        else {
            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEqualIgnoreCase(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotEqualIgnoreCase(baseValues);

            // set base values from temp values
            baseValues = tempValues;
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public Set<String> getAllVales(int id) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(id);

        // set created from string values
        return new HashSet<>(values.getValues());
    }

    @Override
    public int getModelCount(int id) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(id);

        // return size of string values list
        return values.getValues().size();
    }

    @Override
    public String getSatisfiableResult(int id) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(id);

        //return first string value from the string value list
        return values.getValues().get(0);
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // perform insertion
        ConcreteValues results = baseValues.insert(offset, argValues);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void insert(int id,
                       int base,
                       int arg,
                       int offset,
                       int start,
                       int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // perform substring on arg values
        ConcreteValues substr = argValues.substring(start, end);

        // perform insertion
        ConcreteValues results = baseValues.insert(offset, substr);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void isEmpty(boolean result, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertIsEmpty();
        }
        // false branch
        else {
            // get satisfying base values
            baseValues = baseValues.assertNotIsEmpty();
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
    }

    @Override
    public boolean isSatisfiable(int id) {
        // get values
        ConcreteValues values = symbolicStringMap.get(id);

        // satisfiable if string values list is not null and not empty
        return values != null && values.getValues().size() != 0;
    }

    @Override
    public boolean isSingleton(int id) {
        // get values
        ConcreteValues values = symbolicStringMap.get(id);

        // satisfiable if string values list is not null and only one string
        return values != null && values.getValues().size() == 1;
    }

    @Override
    public boolean isSingleton(int id, String actualValue) {
        // get values
        ConcreteValues values = symbolicStringMap.get(id);

        // satisfiable if string values list is not null and only one string
        // and strings are equal
        return values != null &&
               values.getValues().size() == 1 &&
               values.getValues().get(0).equals(actualValue);
    }

    @Override
    public boolean isSound(int id, String actualValue) {
        // get values
        ConcreteValues values = symbolicStringMap.get(id);

        // sound if string values contain actual value
        return values.getValues().contains(actualValue);
    }

    @Override
    public void newConcreteString(int id, String string) {
        // create new concrete values from string
        ConcreteValues newValues =
                new ConcreteValues(this.alphabet, this.initialBound, string);

        // store new values in symbolic string map
        this.symbolicStringMap.put(id, newValues);

    }

    @Override
    public void newSymbolicString(int id) {
        // get list of all possible strings from alphabet
        List<String> strings = this.alphabet.allStrings(0, this.initialBound);

        // create new concrete values from strings
        ConcreteValues newValues = new ConcreteValues(this.alphabet,
                                                      this.initialBound,
                                                      strings);

        // store new values in symbolic string map
        this.symbolicStringMap.put(id, newValues);

    }

    @Override
    public void propagateSymbolicString(int id, int base) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(base);

        // store copy of values in map
        this.symbolicStringMap.put(id, values.copy());
    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform replace
        ConcreteValues results = baseValues.replaceFindKnown(find);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform replace
        ConcreteValues results = baseValues.replace(find, replace);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform replace
        ConcreteValues results = baseValues.replaceReplaceKnown(replace);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharUnknown(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform replace
        ConcreteValues results = baseValues.replaceChar();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public String replaceEscapes(String value) {
        // no need to replace anything, return value
        return value;
    }

    @Override
    public void replaceStrings(int id, int base, int arg1, int arg2) {
        // get concrete values
        ConcreteValues baseValues = symbolicStringMap.get(base);
        ConcreteValues findValues = symbolicStringMap.get(arg1);
        ConcreteValues replaceValues = symbolicStringMap.get(arg2);

        // perform replace
        ConcreteValues results = baseValues.replace(findValues, replaceValues);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void reverse(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform reverse
        ConcreteValues results = baseValues.reverse();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // perform insertion
        ConcreteValues results = baseValues.setCharAt(offset, argValues);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void setLength(int id, int base, int length) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform set length
        ConcreteValues results = baseValues.reverse();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void shutDown() {
        // no need to do anything on shut down
    }

    @Override
    public void startsWith(boolean result, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // true branch
        if (result) {
            // get satisfying base values
            baseValues = baseValues.assertStartsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertContainedInStart(baseValues);
        }
        // false branch
        else {
            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotStartsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotContainedInStart(baseValues);

            // set base values from temp values
            baseValues = tempValues;
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void substring(int id, int base, int start) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform substring
        ConcreteValues results = baseValues.substring(start);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void substring(int id, int base, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform substring
        ConcreteValues results = baseValues.substring(start, end);

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void toLowerCase(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform to lower case
        ConcreteValues results = baseValues.toLowerCase();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void toUpperCase(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform to upper case
        ConcreteValues results = baseValues.toUpperCase();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void trim(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // perform trim
        ConcreteValues results = baseValues.trim();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

}
