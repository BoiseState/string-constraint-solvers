package analysis;

import extendedSolvers.ExtendedSolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses constraints and calls the appropriate solver method.
 *
 * @author Scott Kausler
 */
public class Parser {

    ExtendedSolver solver;
    public static Map<Integer, String> actualVals;

    private boolean debug = true;

    static {
        actualVals = new HashMap<Integer, String>();
    }

    public Parser(ExtendedSolver solver) {

        // set field from parameter
        this.solver = solver;

        // output header
        System.out.println("SING\tTSAT\tFSAT\tDISJOINT");
    }

    /**
     * Allows the setting of debug mode.
     *
     * @param debug boolean indicating if debug mode should be set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Parse a graph root node.
     * @param value Value of a {@link analysis.PrintConstraint}.
     * @param actualValue Actual value of a {@link analysis.PrintConstraint}.
     * @param id Id of a {@link analysis.PrintConstraint}.
     */
    public void addRoot(String value, String actualValue, int id) {

        // if debug mode set
        if (debug) {

            // output root information
            System.out.println("Root: " + value);
        }

        // if actual value not null
        if (actualValue != null) {

            // ensure string is in a valid format
            actualValue = solver.replaceEscapes(actualValue);
        }

        // add actual value to map
        actualVals.put(id, actualValue);
        value = solver.replaceEscapes(value);

        //
        if (value.startsWith("r") || value.startsWith("$r")) {

            // create new symbolic string for id
            solver.newSymbolicString(id);

        } else {

            // create new concrete string for id from actual value
            solver.newConcreteString(id, actualValue);

        }
    }


    public void addOperation(String string,
                             String actualVal,
                             int id,
                             Map<String, Integer> sourceMap) {

        // if debug mode set
        if (debug) {

            // output operation information
            String opInfo = String.format("Operation: %s | %s | %d | %s",
                                          string,
                                          actualVal,
                                          id,
                                          sourceMap);
            System.out.println(opInfo);
        }

        // ensure valid actual value
        actualVal = solver.replaceEscapes(actualVal);
        actualVals.put(id, actualVal);

        // get base id from source map
        int base = sourceMap.get("t");

        // get function name from string value
        String fName = string.split("!!")[0];

        // get arg id from source map, -1 if none
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // ensure the operation can be completed
        if (!solver.isValidState(base, arg)) {
            solver.newSymbolicString(id);
            return;
        }

        // process operation based on function name
        if ((fName.equals("append")) || fName.equals("concat")) {

            // TODO: figure this out
            if (sourceMap.get("s1") == null) {
                solver.newSymbolicString(sourceMap.get("s1"));
            }

            // if function has more than two arguments
            if (sourceMap.size() > 3) {

                // stringBuilder.append(char[] str, int offset, int len)
                // if first two parameters are char array and int
                if (string.split("!!")[1].startsWith("[CI")) {

                    // set arg as new symbolic string
                    arg = solver.getTempId();
                    solver.newSymbolicString(arg);

                }
                // stringBuilder.append(CharSequence s, int start, int end)
                else {

                    // get start and end indices
                    int s2Id = sourceMap.get("s2");
                    int s3Id = sourceMap.get("s3");
                    String s2String = actualVals.get(s2Id);
                    String s3String = actualVals.get(s3Id);
                    int start = Integer.parseInt(s2String);
                    int end = Integer.parseInt(s3String);

                    // perform operation and return
                    solver.append(id, base, arg, start, end);
                    return;
                }

            }
            // stringBuilder.append(char c)
            // if only parameter is a char
            else if (string.split("!!")[1].equals("C")) {

                // create new char
                int charId = sourceMap.get("s1");
                createChar(charId);

            }
            // stringBuilder.append(boolean b)
            // if only param is a boolean
            else if (string.split("!!")[1].equals("Z")) {

                try {

                    // get num
                    int s1Id = sourceMap.get("s1");
                    String s1String = actualVals.get(s1Id);
                    int num = Integer.parseInt(s1String);

                    // convert byte code values to boolean strings
                    if (num == 1) {
                        solver.newConcreteString(arg, "true");
                    } else {
                        solver.newConcreteString(arg, "false");
                    }

                } catch (NumberFormatException e) {

                    // could not parse int, create string symbolic string
                    // from actual value
                    int s1Id = sourceMap.get("s1");
                    String s1String = actualVals.get(s1Id);
                    solver.newConcreteString(arg, s1String);
                }
            }

            // perform append operation
            solver.append(id, base, arg);

        } else if (fName.equals("<init>")) {

            // if target and source ids exist and actual target value
            // is the empty string
            if (sourceMap.get("t") != null &&
                sourceMap.get("s1") != null &&
                actualVals.get(sourceMap.get("t")).equals("")) {

                // copy symbolic string
                solver.propagateSymbolicString(id, base);
            } else {

                // create new symbolic string
                solver.newSymbolicString(id);
            }

        } else if (fName.equals("toString") ||
                   fName.equals("valueOf") ||
                   fName.equals("intern") ||
                   fName.equals("trimToSize") ||
                   (fName.equals("copyValueOf") && sourceMap.size() == 2)) {

            // if no target
            if (!sourceMap.containsKey("t")) {

                // copy symbolic string of arg
                solver.propagateSymbolicString(id, arg);

            } else {

                // copy symbolic string of base
                solver.propagateSymbolicString(id, base);

            }

        } else if (string.startsWith("substring")) {

            // string.substring(int beginIndex)
            // stringBuilder.substring(int start)
            if (sourceMap.size() == 2) {

                // get start index
                int s1Id = sourceMap.get("s1");
                String s1String = actualVals.get(s1Id);
                int start = Integer.parseInt(s1String);

                // if a substring requested
                if (start != 0) {

                    // perform substring operation
                    solver.substring(id, base, start);
                }
            }
            // string.substring(int beginIndex, int endIndex)
            // stringBuilder.substring(int start, int end)
            else {

                // get start and end indices
                int s1Id = sourceMap.get("s1");
                int s2Id = sourceMap.get("s2");
                String s1String = actualVals.get(s1Id);
                String s2String = actualVals.get(s2Id);
                int start = Integer.parseInt(s1String);
                int end = Integer.parseInt(s2String);

                // perform substring operation
                solver.substring(id, base, start, end);
            }

        } else if (fName.equals("setLength")) {

            // get length
            int s1Id = sourceMap.get("s1");
            String s1String = actualVals.get(s1Id);
            int length = Integer.parseInt(s1String);

            // perform set length operation
            solver.setLength(id, base, length);
        }
        //TODO implement other insert
        else if (fName.equals("insert")) {

            // get arg id
            arg = sourceMap.get("s2");

            // get offset id
            int offset = sourceMap.get("s1");

            // stringBuilder.insert(int offset, char c)
            if (string.split("!!")[1].equals("IC")) {

                // create arg symbolic string as char
                createChar(arg);

            }
            // stringBuilder.insert(int offset, char[] str)
            else if (string.split("!!")[1].startsWith("I[C")) {

                // create arg symbolic string
                solver.newSymbolicString(arg);

            }
            // stringBuilder.insert(int index, char[] str, int offset, int len)
            // stringBuilder.insert(int dstOffset,
            //                      CharSequence s,
            //                      int start,
            //                      int end)
            else if (sourceMap.size() > 3) {

                // get start and end indices
                int s3Id = sourceMap.get("s3");
                int s4Id = sourceMap.get("s4");
                String s3String = actualVals.get(s3Id);
                String s4String = actualVals.get(s4Id);
                int start = Integer.parseInt(s3String);
                int end = Integer.parseInt(s4String);

                // perform insert operation
                solver.insert(id, base, arg, offset, start, end);

            } else {

                // perform insert operation
                solver.insert(id, base, arg, offset);

            }
        } else if (fName.equals("setCharAt")) {

            // get arg id
            arg = sourceMap.get("s2");

            // create arg symbolic string as char
            createChar(arg);

            // get offset
            int s1Id = sourceMap.get("s1");
            String s1String = actualVals.get(s1Id);
            int offset = Integer.parseInt(s1String);

            // perform set char at operation
            solver.setCharAt(id, base, arg, offset);

        }
        //TODO: Check for 2 cases: Restricted any string and more then 2
        // leading white space chars

        //For some reason it fails when it is any string of any length. This
        // hack fixes it (woo). Check should be done in strangerlib.
        else if (fName.equals("trim")) {

            // perform trim operation
            solver.trim(id, base);

        } else if (fName.equals("delete")) {

            // get start and end indices
            int s1Id = sourceMap.get("s1");
            int s2Id = sourceMap.get("s2");
            String s1String = actualVals.get(s1Id);
            String s2String = actualVals.get(s2Id);
            int start = Integer.parseInt(s1String);
            int end = Integer.parseInt(s2String);

            // perform delete operation
            solver.delete(id, base, start, end);

        } else if (fName.equals("deleteCharAt")) {

            // get location index
            int s1Id = sourceMap.get("s1");
            String s1String = actualVals.get(s1Id);
            int loc = Integer.parseInt(s1String);

            // perform delete char at operation
            solver.deleteCharAt(id, base, loc);

        } else if (fName.equals("reverse")) {

            // perform reverse operation
            solver.reverse(id, base);

        } else if (fName.equals("toUpperCase") && sourceMap.size() == 1) {

            // perform uppercase operation
            solver.toUpperCase(id, base);

        } else if (fName.equals("toLowerCase") && sourceMap.size() == 1) {

            // perform lowercase operation
            solver.toLowerCase(id, base);

        } else if (fName.startsWith("replace")) {

            // string.replaceAll(String regex, String replace)
            // string.replaceFirst(String regex, String replace)
            // stringBuilder.replace(int start, int end, String str)
            if (fName.equals("replaceAll") ||
                fName.equals("replaceFirst") ||
                // first two params are int
                string.split("!!")[1].startsWith("II") ||
                sourceMap.size() != 3) {

                // set resulting symbolic string as any string
                solver.newSymbolicString(id);
                return;
            }

            // get argument ids
            int argOne = sourceMap.get("s1");
            int argTwo = sourceMap.get("s2");

            // string.replace(char oldChar, char newChar)
            // first two params are char
            if (string.split("!!")[1].equals("CC")) {

                // create symbolic strings as characters
                createChar(argOne);
                createChar(argTwo);

            }
            // string.replace(CharSequence target, CharSequence replacement)
//            else {
//
//                // get string representations
//                String str1 = actualVals.get(argOne);
//                String str2 = actualVals.get(argTwo);
//
//                // set string representations
//                solver.newConcreteString(argOne, str1);
//                solver.newConcreteString(argTwo, str2);
//            }

            // perform solver specific operation
            solver.replace(id, base, argOne, argTwo);

        } else {

            // create symbolic string
            solver.newSymbolicString(id);
        }
    }

    public void addEnd(String string,
                       String actualVal,
                       int id,
                       Map<String, Integer> sourceMap) {

        // if debug mode set
        if (debug) {

            // output end information
            System.out.println("End: " + string + " | " + actualVal);
        }

        actualVal = solver.replaceEscapes(actualVal);
        actualVals.put(id, actualVal);

        if (debug) {
            if (sourceMap.containsKey("t")) {
                int base = sourceMap.get("t");
                if (!solver.isSound(base, actualVal)) {
                    System.err.println("Base not sound:");
                    System.err.println("base: " + solver.getValue(base));
                    System.err.println("actual value: " + actualVal);
//                    throw new IllegalArgumentException("Invalid base in " +
//                                                       "solver");
                }
            }

            if (sourceMap.containsKey("s1")) {
                int arg = sourceMap.get("s1");
                if (!solver.isSound(arg, actualVal)) {
                    System.err.println("Arg not sound:");
                    System.err.println(solver.getValue(arg));
//                    throw new IllegalArgumentException("Invalid arg in
// solver");
                }
            }
        }

        String fName = string.split("!!")[0];

        if (ExtendedSolver.containsBoolFunction(fName)) {
            calculateStats(fName, actualVal, sourceMap);

        }
    }

    private void calculateStats(String fName,
                                String actualVal,
                                Map<String, Integer> sourceMap) {
        int base = sourceMap.get("t");
        String stats = "";
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null ||
             solver.isSingleton(sourceMap.get("s1"), actualVal))) {
            stats += "true\t";
        } else {
            stats += "false\t";
        }
        assertBooleanConstraint(fName, true, sourceMap);
        stats += solver.isSatisfiable(base) + "\t";
        solver.revertLastPredicate();
        assertBooleanConstraint(fName, false, sourceMap);
        stats += solver.isSatisfiable(base) + "\t";
        solver.revertLastPredicate();

        if (!actualVal.equals("true") && !actualVal.equals("false")) {

            System.err.println( "warning constraint detected without " +
                                "true/false value");
            return;
        }

        boolean result = true;
        if (actualVal.equals("false")) {
            result = false;
        }

        // branches disjoint?
        assertBooleanConstraint(fName, result, sourceMap);
        assertBooleanConstraint(fName, !result, sourceMap);

        String disjoint = "yes";
        if (solver.isSatisfiable(base)) {
            disjoint = "no";
        }

        stats += disjoint;

        solver.revertLastPredicate();

        // output stats
        System.out.println(stats);
    }

    /**
     * Deprecated. Ensures the result is a character.
     *
     * @param id The id of the constraint.
     */
    private void createChar(int id) {

        String val = actualVals.get(id);
        solver.newConcreteString(id, val);
    }

    /**
     * Assert a predicate on a symbolic value.
     *
     * @param result    Is it a true or false predicate.
     * @param method    The predicate method.
     * @param sourceMap The values involved.
     */
    private void assertBooleanConstraint(String method,
                                         boolean result,
                                         Map<String, Integer> sourceMap) {

        // get function name
        String fName = method.split("!!")[0];

        // get id of base symbolic string
        int base = (sourceMap.get("t"));

        // get id of second symbolic string if it exists
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // assert the boolean constraint
        if (fName.equals("contains")) {

            solver.contains(result, base, arg);

        } else if (fName.equals("endsWith")) {

            solver.endsWith(result, base, arg);

        } else if (fName.equals("startsWith") && sourceMap.size() == 2) {

            solver.startsWith(result, base, arg);

        } else if (fName.equals("equals") || fName.equals("contentEquals")) {

            solver.equals(result, base, arg);

        } else if (fName.equals("equalsIgnoreCase")) {

            solver.equalsIgnoreCase(result, base, arg);

        } else if (fName.equals("isEmpty")) {

            solver.isEmpty(result, base);

        }
    }

    public void remove(int id) {
        solver.remove(id);
    }

    public void shutDown() {
        solver.shutDown();
    }
}
