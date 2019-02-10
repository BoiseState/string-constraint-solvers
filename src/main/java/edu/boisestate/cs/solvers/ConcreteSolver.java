package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.BasicTimer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class ConcreteSolver
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

    public ConcreteSolver(Alphabet alphabet, int setBound) {
        super(setBound);

        this.alphabet = alphabet;
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // start timer
        BasicTimer.start();

        // perform substring on arg values
        ConcreteValues substr = argValues.substring(start, end);

        // perform concatenation, equivalent to append
        ConcreteValues results = baseValues.concat(substr);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void append(int id, int base, int arg) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // start timer
        BasicTimer.start();

        // perform concatenation, equivalent to append
        ConcreteValues results = baseValues.concat(argValues);

        // start timer
        BasicTimer.stop();

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
//        if(arg == 2){
//        	System.out.println(result);
//        	System.out.println(baseValues + " " + argValues + " " + arg);
//        }
        // true branch
        if (result) {
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertContainsOther(argValues);

            // get satisfying arg values
            argValues = argValues.assertContainedInOther(baseValues);

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotContainsOther(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotContainedInOther(baseValues);

            // set base values from temp values
            baseValues = tempValues;

            // start timer
            BasicTimer.stop();
        }
//        if(arg == 2){
//        	System.out.println(baseValues + " " + base);
//        	System.out.println(argValues + " " + arg);
//        	//System.exit(2);
//        }
        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void delete(int id, int base, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform delete
        ConcreteValues results = baseValues.delete(start, end);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform delete character at index
        ConcreteValues results = baseValues.deleteCharAt(loc);

        // start timer
        BasicTimer.stop();

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
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertEndsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertEndsOther(baseValues);

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEndsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotEndsOther(baseValues);

            // set base values from temp values
            baseValues = tempValues;

            // start timer
            BasicTimer.stop();
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
        //System.out.println("argModel " + arg + "\n" + argValues.toString());
        //System.out.println("result " + result);
        // true branch
        if (result) {
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertEqual(argValues);

            // get satisfying arg values
            argValues = baseValues.copy();

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEqual(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotEqual(baseValues);

            // set base values from temp values
            baseValues = tempValues;

            // start timer
            BasicTimer.stop();
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
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertEqualIgnoreCase(argValues);

            // get satisfying arg values
            argValues = argValues.assertEqualIgnoreCase(baseValues);

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotEqualIgnoreCase(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotEqualIgnoreCase(baseValues);

            // set base values from temp values
            baseValues = tempValues;

            // start timer
            BasicTimer.stop();
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
        return values.getValues();
    }

    @Override
    public long getModelCount(int id) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(id);

        // return size of string values list
        return values.modelCount();
    }

    @Override
    public String getSatisfiableResult(int id) {
        // get values
        ConcreteValues values = this.symbolicStringMap.get(id);

        //return first string value from the string value list
        return values.getValues().iterator().next();
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // start timer
        BasicTimer.start();

        // perform insertion
        ConcreteValues results = baseValues.insert(offset, argValues);

        // start timer
        BasicTimer.stop();

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

        // start timer
        BasicTimer.start();

        // perform substring on arg values
        ConcreteValues substr = argValues.substring(start, end);

        // perform insertion
        ConcreteValues results = baseValues.insert(offset, substr);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void isEmpty(boolean result, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // true branch
        if (result) {
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertIsEmpty();

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertNotEmpty();

            // start timer
            BasicTimer.stop();
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
    }

    @Override
    public boolean isSatisfiable(int id) {
        // get values
        ConcreteValues values = symbolicStringMap.get(id);

        // satisfiable if string values list is not null and not empty
        return values != null && !values.getValues().isEmpty();
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
               values.getValues().iterator().next().equals(actualValue);
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
        // start timer
        BasicTimer.start();

        // create new concrete values from string
        ConcreteValues newValues =
                new ConcreteValues(this.alphabet, this.initialBound, string);

        // start timer
        BasicTimer.stop();

        // store new values in symbolic string map
        this.symbolicStringMap.put(id, newValues);

    }

    @Override
    public void newSymbolicString(int id) {
        // start timer
        BasicTimer.start();

        // get list of all possible strings from alphabet
        List<String> strings = this.alphabet.allStrings(0, this.initialBound);

        // create new concrete values from strings
        ConcreteValues newValues = new ConcreteValues(this.alphabet,
                                                      this.initialBound,
                                                      strings);

        // start timer
        BasicTimer.stop();

        // store new values in symbolic string map
        this.symbolicStringMap.put(id, newValues);

    }

    @Override
    public void propagateSymbolicString(int id, int base) {
        // start timer
        BasicTimer.start();

        // get values
        ConcreteValues values = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.stop();

        // store copy of values in map
        this.symbolicStringMap.put(id, values.copy());
    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform replace
        ConcreteValues results = baseValues.replaceFindKnown(find);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform replace
        ConcreteValues results = baseValues.replace(find, replace);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform replace
        ConcreteValues results = baseValues.replaceReplaceKnown(replace);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void replaceCharUnknown(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform replace
        ConcreteValues results = baseValues.replaceChar();

        // start timer
        BasicTimer.stop();

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
        String findString = concreteStringMap.get(arg1);
        String replaceString = concreteStringMap.get(arg2);

        // start timer
        BasicTimer.start();

        // perform replace
        ConcreteValues results = baseValues.replace(findString, replaceString);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void reverse(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform reverse
        ConcreteValues results = baseValues.reverse();

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);
        ConcreteValues argValues = this.symbolicStringMap.get(arg);

        // start timer
        BasicTimer.start();

        // perform insertion
        ConcreteValues results = baseValues.setCharAt(offset, argValues);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void setLength(int id, int base, int length) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform set length
        ConcreteValues results = baseValues.reverse();

        // start timer
        BasicTimer.stop();

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
            // start timer
            BasicTimer.start();

            // get satisfying base values
            baseValues = baseValues.assertStartsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertStartsOther(baseValues);

            // start timer
            BasicTimer.stop();
        }
        // false branch
        else {
            // start timer
            BasicTimer.start();

            // get satisfying base values as temp values
            ConcreteValues tempValues = baseValues.assertNotStartsWith(argValues);

            // get satisfying arg values
            argValues = argValues.assertNotStartsOther(baseValues);

            // set base values from temp values
            baseValues = tempValues;

            // start timer
            BasicTimer.stop();
        }

        // store resulting concrete values
        this.symbolicStringMap.put(base, baseValues);
        this.symbolicStringMap.put(arg, argValues);
    }

    @Override
    public void substring(int id, int base, int start) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform substring
        ConcreteValues results = baseValues.substring(start);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void substring(int id, int base, int start, int end) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform substring
        ConcreteValues results = baseValues.substring(start, end);

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void toLowerCase(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform to lower case
        ConcreteValues results = baseValues.toLowerCase();

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void toUpperCase(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform to upper case
        ConcreteValues results = baseValues.toUpperCase();

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

    @Override
    public void trim(int id, int base) {
        // get concrete values
        ConcreteValues baseValues = this.symbolicStringMap.get(base);

        // start timer
        BasicTimer.start();

        // perform trim
        ConcreteValues results = baseValues.trim();

        // start timer
        BasicTimer.stop();

        // store result in map
        this.symbolicStringMap.put(id, results);
    }

}
