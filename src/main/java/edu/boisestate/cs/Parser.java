package edu.boisestate.cs;

import edu.boisestate.cs.graph.PrintConstraint;
import edu.boisestate.cs.solvers.ExtendedSolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses constraints and calls the appropriate solver method.
 *
 * @author Scott Kausler
 */
@SuppressWarnings("Duplicates")
public class Parser {

    public static Map<Integer, String> actualVals;
    ExtendedSolver solver;
    private boolean debug;
    private int maxGraphId;

    public void setMaxGraphId(int maxGraphId) {
        this.maxGraphId = maxGraphId;
    }

    static {
        actualVals = new HashMap<>();
    }

    public Parser(ExtendedSolver solver,
                  boolean debug) {

        // set field from parameter
        this.solver = solver;
        this.debug = debug;

        // initialize fields
        this.maxGraphId = 0;
    }

    public boolean addEnd(PrintConstraint constraint) {

        // get constraint info as variables
        String string = constraint.getSplitValue();
        String actualVal = constraint.getActualVal();
        int id = constraint.getId();
        Map<String, Integer> sourceMap = constraint.getSourceMap();

        // if debug mode set
        if (debug) {

            // output end information
            System.out.println("End: " + string + " | " + actualVal);
        }

        // ensure valid actual value
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
//eas 11-13-18: the actual value only recorded on the target, so
            //the checks below do not make sense
//            if (sourceMap.containsKey("s1")) {
//                int arg = sourceMap.get("s1");
//                if (!solver.isSound(arg, actualVal)) {
//                    System.err.println("Arg not sound:");
//                    System.err.println(solver.getValue(arg));
////                    throw new IllegalArgumentException("Invalid arg in
//// solver");
//                }
//            }
        }


        // if solver containsString boolean function name
        String fName = string.split("!!")[0];

        return ExtendedSolver.containsBoolFunction(fName);
    }

    /**
     * Processes an operation node propagating the resulting symbolic string
     * values.
     *
     * @param constraint
     *         The operation node constraint.
     *
     * @return Returns a string representation of the operation represented by
     * the operation node constraint.
     */
    public String addOperation(PrintConstraint constraint) {

        // get constraint info as variables
        String string = constraint.getSplitValue();
        String actualVal = constraint.getActualVal();
        int id = constraint.getId();
        Map<String, Integer> sourceMap = constraint.getSourceMap();

        // initialize operation string
        String operationString = String.format("[Unknown string operation %d: %s]", id, string);

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
            // return after setting new symbolic string
            solver.newSymbolicString(id);
            return null;
        }

        // process operation based on function name
        if ((fName.equals("concatenate")) ||
            fName.equals("concat") ||
            fName.equals("append")) {

            operationString = processConcat(constraint);

        } else if (fName.equals("<init>")) {

            operationString = processInit(constraint);

        } else if (string.startsWith("substring")) {

            operationString = processSubstring(constraint, string);

        } else if (fName.equals("setLength")) {

            operationString = processSetLength(constraint);

        } else if (fName.equals("insert")) {

            operationString = processInsert(constraint);

        } else if (fName.equals("setCharAt")) {

            operationString = processSetCharAt(constraint);

        }
        //TODO: Check for 2 cases: Restricted any string and more then 2
        // leading white space chars

        //For some reason it fails when it is any string of any length. This
        // hack fixes it (woo). Check should be done in strangerlib.
        else if (fName.equals("trim")) {

            // perform trim operation
            solver.trim(id, base);
            operationString = String.format("<S:%d>.trim()", base);

        } else if (fName.equals("delete")) {

            operationString = processDelete(constraint);

        } else if (fName.equals("deleteCharAt")) {

            operationString = processDeleteCharAt(constraint);

        } else if (fName.equals("reverse")) {

            // perform reverse operation
            solver.reverse(id, base);
            operationString = String.format("<S:%d>.reverse()", base);

        } else if (fName.startsWith("replace")) {

            operationString = processReplace(constraint);

        } else if (fName.equals("toUpperCase") && sourceMap.size() == 1) {

            // perform uppercase operation
            solver.toUpperCase(id, base);
            operationString = String.format("<S:%d>.toUpperCase()", base);

        } else if (fName.equals("toLowerCase") && sourceMap.size() == 1) {

            // perform lowercase operation
            solver.toLowerCase(id, base);
            operationString = String.format("<S:%d>.toLowerCase()", base);

        } else if (fName.equals("toString") ||
                   fName.equals("intern") ||
                   fName.equals("trimToSize") ||
                   fName.equals("length") ||
                   fName.equals("charAt")) {

            // perform string propagation
            processPropagation(constraint);
            operationString = String.format("<S:%d>.%s()", base, fName);

        } else if (fName.equals("valueOf") ||
                   (fName.equals("copyValueOf") && sourceMap.size() == 2)) {

            // perform string propagation
            processPropagation(constraint);
            operationString = String.format("String.%s(<S:%d>)", fName, base);

        } else {

            // create symbolic string
            solver.newSymbolicString(id);
        }

        // return op string
        return operationString;
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#append(boolean)}</li>
     * <li>{@link java.lang.StringBuffer#append(char)}</li> <li>{@link
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
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processConcat(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        int id = constraint.getId();
        int base = sourceMap.get("t");
        String fName = string.split("!!")[0];

        // get arg id from source map, -1 if none
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // initialize operation string
        String operation = String.format("<S:%d>.%s(<CS:%d>)", base, fName, arg);
        if (this.solver.isSingleton(arg)) {
            operation = String.format("<S:%d>.%s(\\\"%s\\\")", base, fName, actualVals.get(arg));
        }

        // if argument not set
        if (sourceMap.get("s1") == null) {
            arg = this.generateNextId();
            solver.newSymbolicString(arg);
        }

        // get operation parameters
        String params = string.split("!!")[1];

        // if function has more than two arguments
        if (sourceMap.size() > 3) {

            // get start and end indices
            int s2Id = sourceMap.get("s2");
            int s3Id = sourceMap.get("s3");
            String s2String = actualVals.get(s2Id);
            String s3String = actualVals.get(s3Id);
            int start = Integer.parseInt(s2String);
            int end = Integer.parseInt(s3String);

            // set param symbols for use in operation string
            String paramSymbols = String.format("<CS:%d>, %d, %d",
                                                arg,
                                                start,
                                                end);
            boolean argSingleton = this.solver.isSingleton(arg);
            if (argSingleton){
                paramSymbols = String.format("\\\"%s\\\", %d, %d",
                                          actualVals.get(arg),
                                          start,
                                          end);
            }

            // stringBuilder.append(char[] str, int offset, int len)
            // if first two parameters are char array and int
            if (params.startsWith("[CI")) {

                // update arg symbol
                if (!argSingleton) {
                    paramSymbols = String.format("<C[]:%d>, %d, %d",
                                                 arg,
                                                 start,
                                                 end);
                }

                // adjust end to be actual end value instead of length
                end = start + end;
            }

            operation = String.format("<S:%d>.append(%s)", base, paramSymbols);

            // perform operation
            solver.append(id, base, arg, start, end);

            // return string representation of operation
            return operation;

        }
        // stringBuilder.concatenate(char c)
        // if only parameter is a char
        else if (params.equals("C")) {

            // create new char
            int charId = sourceMap.get("s1");
            String charString = actualVals.get(charId);
            solver.newConcreteString(charId, charString);

            // update operation string
            operation = String.format("<S:%d>.%s('%s')", base, fName, charString);

        }
        // stringBuilder.concatenate(boolean b)
        // if only param is a boolean
        else if (params.equals("Z")) {

            String argString = "<boolean>";

            try {

                // get num
                int s1Id = sourceMap.get("s1");
                String s1String = actualVals.get(s1Id);
                int num = Integer.parseInt(s1String);

                // convert byte code values to boolean strings
                if (num == 1) {
                    solver.newConcreteString(arg, "true");
                    argString = "true";
                } else {
                    solver.newConcreteString(arg, "false");
                    argString = "false";
                }

            } catch (NumberFormatException e) {

                // could not parse int, create string symbolic string
                // from actual value
                int s1Id = sourceMap.get("s1");
                String s1String = actualVals.get(s1Id);
                solver.newConcreteString(arg, s1String);
            }

            // update operation string
            operation = String.format("<S:%d>.%s(%s)", base, fName, argString);
        }
        // stringBuilder.concatenate(String str)
        // if only param is a string
        else if (params.equals("Ljava/lang/String;") &&
                 arg != -1 && solver.getValue(arg) == null) {

            // if actual value exists
            String argValue = actualVals.get(arg);
            if (argValue != null) {

                // set arg symbolic string from actual value
                solver.newConcreteString(arg, argValue);

                // update operation string
                operation = String.format("<S:%d>.%s(\\\"%s\\\")", base, fName, argValue);

            } else {

                // set arg symbolic string to any string
                solver.newSymbolicString(arg);

                // update operation string
                operation = String.format("<S:%d>.%s(<S:%d>)", base, fName, arg);
            }
        }

        // perform concatenate operation
        solver.append(id, base, arg);

        // return operation string
        return operation;
    }

    private int generateNextId() {

        // increment max graph id for next valid id
        this.maxGraphId++;

        // return valid new id
        return this.maxGraphId;
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#delete(int, int)}</li>
     * <li>{@link java.lang.StringBuilder#delete(int, int)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processDelete(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get start and end indices
        int s1Id = sourceMap.get("s1");
        int s2Id = sourceMap.get("s2");
        String s1String = actualVals.get(s1Id);
        String s2String = actualVals.get(s2Id);
        int start = Integer.parseInt(s1String);
        int end = Integer.parseInt(s2String);

        // perform delete operation
        solver.delete(id, base, start, end);

        // return operation string
        return String.format("<S:%d>.delete(%d, %d)", base, start, end);
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#deleteCharAt(int)}</li>
     * <li>{@link java.lang.StringBuilder#deleteCharAt(int)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processDeleteCharAt(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get location index
        int s1Id = sourceMap.get("s1");
        String s1String = actualVals.get(s1Id);
        int loc = Integer.parseInt(s1String);

        // perform delete char at operation
        solver.deleteCharAt(id, base, loc);

        // return operation string
        return String.format("<S:%d>.deleteCharAt(%d)", base, loc);
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.String#String()}</li> <li>{@link
     * java.lang.StringBuffer#StringBuffer()}</li> <li>{@link
     * java.lang.StringBuilder#StringBuilder()}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processInit(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // declare operation string
        String operation;

        System.out.println("processInit " + id + " val " + actualVals.get
         (id));

        // if target and source ids exist and actual target value
        // is the empty string
        if (sourceMap.get("t") != null &&
            sourceMap.get("s1") != null &&
            actualVals.get(sourceMap.get("t")).equals("")) {

            // copy symbolic string
            solver.propagateSymbolicString(id, base);

            // set operation string
            operation = String.format("<this> = <S:%d>", base);

        } else {

            // create new symbolic string
            solver.newSymbolicString(id);

            // set operation string
            operation = String.format("<this> = <S:%d>", base);
        }

        // return operation string
        return operation;
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#insert(int,
     * CharSequence)}</li> <li>{@link java.lang.StringBuffer#insert(int,
     * CharSequence, int, int)}</li> <li>{@link java.lang.StringBuffer#insert
     * (int, char[], int, int)}</li> <li>{@link java.lang.StringBuffer#insert
     * (int, boolean)}</li> <li>{@link java.lang.StringBuffer#insert(int,
     * char)}</li> <li>{@link java.lang.StringBuffer#insert(int, char[])}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, double)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, float)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, int)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, long)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, Object)}</li> <li>{@link
     * java.lang.StringBuffer#insert(int, String)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, CharSequence)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, CharSequence, int, int)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, char[], int, int)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, boolean)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, char)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, char[])}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, double)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, float)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, int)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, long)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, Object)}</li> <li>{@link
     * java.lang.StringBuilder#insert(int, String)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processInsert(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get offset id
        // int offset = sourceMap.get("s1"); eas: it is a bug
        int s1Id = sourceMap.get("s1");
        String s1String = actualVals.get(s1Id);
        int offset = Integer.parseInt(s1String);

        // get arg id
        int arg = sourceMap.get("s2");

        // initialize operation string
        String operation = String.format("<S:%d>.insert(%d, <S:%d>)",
                                         base,
                                         offset,
                                         arg);

        //TODO implement other inserts

        // get operation parameters
        String params = string.split("!!")[1];

        // stringBuilder.insert(int offset, char c)
        // stringBuilder.insert(int offset, char[] str)
        // stringBuilder.insert(int offset, CharSequence str)
        if (params.equals("IC") ||
            ((params.equals("I[C") ||
              params.equals("ILjava/lang/CharSequence;")) &&
             sourceMap.size() <= 3)) {

            // create arg symbolic string
            String argString = actualVals.get(arg);
            solver.newConcreteString(arg, argString);

            // perform insert
            solver.insert(id, base, arg, offset);

            // set operation
            operation = String.format("<S:%d>.insert(%d, \\\"%s\\\")",
                                      base,
                                      offset,
                                      argString);

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

            // set operation
            operation = String.format("<S:%d>.insert(%d, <S:%d>, %d, %d)",
                                      base,
                                      offset,
                                      arg,
                                      start,
                                      end);

        } else {

            // perform insert operation
            solver.insert(id, base, arg, offset);

        }

        // return operation string
        return operation;
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.String#copyValueOf(char[])}</li>
     * <li>{@link java.lang.String#copyValueOf(char[], int, int)}</li>
     * <li>{@link java.lang.String#intern()}</li> <li>{@link
     * java.lang.String#toString}</li>
     * <li>{@link java.lang.String#valueOf(boolean)}</li>
     * <li>{@link java.lang.String#valueOf(char)}</li> <li>{@link
     * java.lang.String#valueOf(char[])}</li> <li>{@link
     * java.lang.String#valueOf(char[], int, int)}</li> <li>{@link
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
     * @param constraint
     *         The constraint corresponding to the operation.
     */
    private void processPropagation(PrintConstraint constraint) {
    	//System.out.println("Propagating " + constraint);

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");
        //System.out.println("Propagating " + constraint + " " + base +" to " + id);

        // get arg id from source map, -1 if none
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // if no target
        if (!sourceMap.containsKey("t")) {

            // copy symbolic string of arg
            solver.propagateSymbolicString(id, arg);

        } else {

            // copy symbolic string of base
            solver.propagateSymbolicString(id, base);

        }
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.String#replace(char, char)}</li>
     * <li>{@link java.lang.String#replace(CharSequence, CharSequence)}</li>
     * <li>{@link java.lang.String#replaceAll(String, String)}</li> <li>{@link
     * java.lang.String#replaceFirst(String, String)}</li> <li>{@link
     * java.lang.StringBuffer#replace(int, int, String)}</li> <li>{@link
     * java.lang.StringBuilder#replace(int, int, String)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processReplace(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        String fName = string.split("!!")[0];
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get argument ids
        int arg1 = sourceMap.get("s1");
        int arg2 = sourceMap.get("s2");

        // get operation parameters
        String params = string.split("!!")[1];

        // declare operation string
        String operation = null;

        // string.replaceAll(String regex, String replace)
        // string.replaceFirst(String regex, String replace)
        // stringBuilder.replace(int start, int end, String str)
        if (fName.equals("replaceAll") || fName.equals("replaceFirst")) {

            // set resulting symbolic string as any string
            solver.newSymbolicString(id);
            return String.format("<S:%d>.%s(<S:%d>, <S:%d>)",
                                 base,
                                 fName,
                                 arg1,
                                 arg2);
        }
        // first two params are int
        else if (params.startsWith("II") || sourceMap.size() != 3) {

            String arg1String = actualVals.get(arg1);
            String arg2String = actualVals.get(arg2);
            int start = Integer.parseInt(arg1String);
            int end = Integer.parseInt(arg2String);

            // set resulting symbolic string as any string
            solver.newSymbolicString(id);
            return String.format("<S:%d>.replace(%d, %d, <S:%d>)",
                                 base,
                                 start,
                                 end,
                                 arg2);

        }

        // string.replace(char oldChar, char newChar)
        // first two params are char
        if (params.equals("CC")) {

            // create symbolic strings as characters
            String arg1String = actualVals.get(arg1);
            String arg2String = actualVals.get(arg2);
            solver.newConcreteString(arg1, arg1String);
            solver.newConcreteString(arg2, arg2String);

            // check args constants
            if (arg1String != null &&
                arg1String.length() == 1 &&
                arg2String != null &&
                arg2String.length() == 1) {

                char findChar = 0;
                char replaceChar = 0;
                boolean findKnown = false;
                boolean replaceKnown = false;

                // determine if old char is known
                if (arg1String.charAt(0) != 0) {
                    findKnown = true;
                    findChar = arg1String.charAt(0);
                }

                // determine if new char is known
                if (arg2String.charAt(0) != 0) {
                    replaceKnown = true;
                    replaceChar = arg2String.charAt(0);
                }

                // perform appropriate replace operation
                if (findKnown && replaceKnown) {
                    this.solver.replaceCharKnown(id,
                                                 base,
                                                 findChar,
                                                 replaceChar);
                    operation = String.format("<S:%d>.replace('%s', '%s')",
                                              base,
                                              findChar,
                                              replaceChar);
                } else if (findKnown) {
                    this.solver.replaceCharFindKnown(id, base, findChar);
                    operation = String.format("<S:%d>.replace('%s', <char>)",
                                              base,
                                              findChar);
                } else if (replaceKnown) {
                    this.solver.replaceCharReplaceKnown(id, base, replaceChar);
                    operation = String.format("<S:%d>.replace(<char>, '%s')",
                                              base,
                                              replaceChar);
                } else {
                    this.solver.replaceCharUnknown(id, base);
                    operation = String.format("<S:%d>.replace(<char>, <char>)",
                                              base);
                }
            }
        }
        // string.replace(CharSequence target, CharSequence replacement)
        else if (params.equals("Ljava/lang/CharSequence;" +
                               "Ljava/lang/CharSequence;")) {

            // get string representations
            String str1 = actualVals.get(arg1);
            String str2 = actualVals.get(arg2);

            // set string representations
            solver.newConcreteString(arg1, str1);
            solver.newConcreteString(arg2, str2);

            // perform solver specific operation
            solver.replaceStrings(id, base, arg1, arg2);

            // set operation string
            operation = String.format("<S:%d>.replace(\\\"%s\\\", \\\"%s\\\")",
                                      base,
                                      str1,
                                      str2);

        } else {

            solver.newSymbolicString(id);

            operation = String.format("<S:%d>.[Unknown Replace Operation: %s]",
                                      base, fName);
        }

        // return operation string
        return operation;
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#setCharAt(int,
     * char)}</li> <li>{@link java.lang.StringBuilder#setCharAt(int, char)}</li>
     * </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processSetCharAt(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        int arg;

        // get arg id
        arg = sourceMap.get("s2");

        // create arg symbolic string as char
        String argString = actualVals.get(arg);
        solver.newConcreteString(arg, argString);

        // get offset
        int s1Id = sourceMap.get("s1");
        String s1String = actualVals.get(s1Id);
        int offset = Integer.parseInt(s1String);

        // perform set char at operation
        solver.setCharAt(id, base, arg, offset);

        return String.format("<S:%d>.setCharAt(%d, '%s')",
                             base,
                             offset,
                             argString);
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.StringBuffer#setLength(int)}</li>
     * <li>{@link java.lang.StringBuilder#setLength(int)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processSetLength(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get length
        int s1Id = sourceMap.get("s1");
        String s1String = actualVals.get(s1Id);
        int length = Integer.parseInt(s1String);
        //System.out.println("Lenght " + length + " " + base + " " +
        // actualVals.get(base).isEmpty());

        // perform set length operation
        solver.setLength(id, base, length);

        return String.format("<S:%d>.setLength(%d)", base, length);
    }

    /**
     * Determine and symbolically execute the correct concatenate or concatenate
     * operation: <ul> <li>{@link java.lang.String#subSequence(int, int)}</li>
     * <li>{@link java.lang.String#substring(int)}</li> <li>{@link
     * java.lang.String#substring(int, int)}</li> <li>{@link
     * java.lang.StringBuffer#subSequence(int, int)}</li> <li>{@link
     * java.lang.StringBuffer#substring(int)}</li> <li>{@link
     * java.lang.StringBuffer#substring(int, int)}</li> </ul>
     *
     * @param constraint
     *         The constraint corresponding to the operation.
     *
     * @return Returns a string representation of the operation.
     */
    private String processSubstring(PrintConstraint constraint, String fName) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // declare operation string
        String operation;

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

                operation = String.format("<S:%d>.substring(%d)", base, start);

            } else {

                // propagate
                solver.propagateSymbolicString(id, base);

                operation =
                        String.format("<S:%d>.[Unknown substring operation: %s]",
                                      base, fName);
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

            operation = String.format("<S:%d>.substring(%d, %d)",
                                      base,
                                      start,
                                      end);
        }

        // return operation string
        return operation;
    }

    /**
     * Parse a graph root node.
     *
     * @param constraint
     *         The root node constraint.
     *
     * @return Returns string representation of the the initialization
     * operation.
     */
    public String addRoot(PrintConstraint constraint) {

        // get constraint info as variables
        String value = constraint.getSplitValue();
        String actualValue = constraint.getActualVal();
        int id = constraint.getId();

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

        // if labeled as root value
        if (value.startsWith("r") || value.startsWith("$r")) {

            // create new symbolic string for id
            solver.newSymbolicString(id);
            return String.format("<S:%d> = <init>", id);
        }

        // create new concrete string for id from actual value
        solver.newConcreteString(id, actualValue);
        return String.format("<S:%d> = \\\"%s\\\"", id, actualValue);
    }

    /**
     * Assert a predicate on a symbolic value from the following boolean
     * function: <ul> <li>{@link java.lang.String#contains(CharSequence)}</li>
     * <li>{@link java.lang.String#contentEquals(CharSequence)}</li> <li>{@link
     * java.lang.String#contentEquals(StringBuffer)}</li> <li>{@link
     * java.lang.String#endsWith(String)}</li> <li>{@link
     * java.lang.String#equals(Object)}</li>
     * <li>{@link java.lang.String#equalsIgnoreCase(String)}</li>
     * <li>{@link java.lang.String#isEmpty()}</li> <li>{@link
     * java.lang.String#matches(String)}</li> <li>{@link
     * java.lang.String#regionMatches(boolean, int, String, int, int)}</li>
     * <li>{@link java.lang.String#regionMatches(int, String, int, int)}</li>
     * <li>{@link java.lang.String#startsWith(String)}</li> <li>{@link
     * java.lang.String#startsWith(String, int)}</li> </ul>
     *
     * @param result
     *         Is it a true or false predicate.
     * @param constraint
     *         The the boolean constraint which is being asserted.
     */
    public void assertBooleanConstraint(boolean result,
                                        PrintConstraint constraint) {

        // get constraint info as variables
        String string = constraint.getSplitValue();
        String fName = string.split("!!")[0];
        Map<String, Integer> sourceMap = constraint.getSourceMap();

        // get id of base symbolic string
        int base = (sourceMap.get("t"));

        // get id of second symbolic string if it exists
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // TODO: add starts with for sourceMap size 3 (two args)
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

    /**
     * Traverses the flow graph and solves PCs
     */
    public void runSolver() {
    }
}
