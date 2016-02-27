package analysis;

import extendedSolvers.ConcreteSolver;
import extendedSolvers.ExtendedSolver;
import org.jgrapht.DirectedGraph;
import stringSymbolic.SymbolicEdge;

import java.util.*;

/**
 * Parses constraints and calls the appropriate solver method.
 *
 * @author Scott Kausler
 */
public class Parser {

    public static Map<Integer, String> actualVals;
    DirectedGraph<PrintConstraint, SymbolicEdge> graph;
    ExtendedSolver solver;
    private boolean debug;
    private int maxGraphId;

    public Parser(ExtendedSolver solver,
                  DirectedGraph<PrintConstraint, SymbolicEdge> graph,
                  boolean debug) {

        // set field from parameter
        this.solver = solver;
        this.graph = graph;
        this.debug = debug;

        // initialize fields
        this.maxGraphId = 0;

        // output header
        System.out.println("ID    \t" +
                           "SING \t" +
                           "TSAT \t" +
                           "FSAT \t" +
                           "DISJOINT");
    }

    /**
     * Traverses the flow graph and solves PCs
     */
    public void runSolver() {

        // initialize sets
        Set<PrintConstraint> removeSet = new HashSet<>();
        Set<PrintConstraint> processedSet = new HashSet<>();
        Set<PrintConstraint> ends = new HashSet<>();
        Set<PrintConstraint> roots = new HashSet<>();

        // populate root and end sets
        for (PrintConstraint constraint : graph.vertexSet()) {

            // if no in paths, node is a root node
            if (graph.inDegreeOf(constraint) == 0) {
                roots.add(constraint);
            }

            // if no out paths, node is an end node
            if (graph.outDegreeOf(constraint) == 0) {
                ends.add(constraint);
            }
        }

        // initialize list of constraints
        List<PrintConstraint> toBeAdded = new LinkedList<>();

        // initialize sorted list of vertices
        List<PrintConstraint> vertices = new ArrayList<>(graph.vertexSet());
        Collections.sort(vertices);

        //Topological progression...
        while (vertices.size() > 0) {

            // remove constraints from collections
            graph.removeAllVertices(removeSet);
            vertices.removeAll(removeSet);
            vertices.removeAll(toBeAdded);
            processedSet.removeAll(removeSet);

            // clear collections
            removeSet.clear();
            toBeAdded.clear();

            // for each vertex constraint
            for (PrintConstraint vertex : vertices) {

                // initialize source map
                Map<String, Integer> sourceMap = new HashMap<>();

                // if vertex not in processed set
                if (!processedSet.contains(vertex)) {

                    // set flag
                    boolean readyToProcess = true;

                    // for each incoming edge of the current vertex
                    for (SymbolicEdge edge : graph.incomingEdgesOf(vertex)) {

                        // get source of incoming edge
                        PrintConstraint source =
                                (PrintConstraint) edge.getASource();

                        // if source vertex not in processed set
                        if (!processedSet.contains(source)) {

                            // unset flag
                            readyToProcess = false;

                            // break out of incoming edges loop
                            break;

                        } else {

                            // update source map with edge information
                            String edgeType = edge.getType();
                            int sourceId = source.getId();
                            sourceMap.put(edgeType, sourceId);
                        }
                    }

                    // if flag is still set
                    if (readyToProcess) {

                        // set the source map for the vertex
                        vertex.setSourceMap(sourceMap);

                        // current vertex queued for addition
                        toBeAdded.add(vertex);

                        // break out of the vertices loop
                        break;
                    }

                } else {

                    // set flag
                    boolean deleteNode = true;

                    // for each outgoing edge from the current vertex
                    for (SymbolicEdge edge : graph.outgoingEdgesOf(vertex)) {

                        // get the target vertex of the outgoing edge
                        PrintConstraint target =
                                (PrintConstraint) edge.getATarget();

                        // if target vertex not in processed set
                        if (!processedSet.contains(target)) {

                            // unset flag
                            deleteNode = false;

                            // break out of outgoing edges loop
                            break;
                        }
                    }

                    // if flag set
                    if (deleteNode) {

                        // current vertex queued for removal
                        removeSet.add(vertex);

                        // remove vertex from parser
                        int vertexId = vertex.getId();
                        solver.remove(vertexId);
                    }
                }
            }

            // sort addition list before processing
            // Collections.sort(toBeAdded);

            // if addition list contains constraints
            if (toBeAdded.size() > 0) {

                // get first constraint in list
                PrintConstraint first = toBeAdded.get(0);

                // if constraint is an end node
                if (ends.contains(first)) {

                    // add end
                    this.addEnd(first);

                } else if (roots.contains(first)) {

                    // add root
                    this.addRoot(first);

                } else {

                    // add operation
                    this.addOperation(first);
                }

                // add constraint to processed list
                processedSet.add(first);
            }
        }

        // shut down solver
        solver.shutDown();
    }

    /**
     * Parse a graph root node.
     *
     * @param constraint The root node constraint.
     */
    public void addRoot(PrintConstraint constraint) {

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

        //
        if (value.startsWith("r") || value.startsWith("$r")) {

            // create new symbolic string for id
            solver.newSymbolicString(id);

        } else {

            // create new concrete string for id from actual value
            solver.newConcreteString(id, actualValue);

        }
    }

    /**
     * Processes an operation node propagating the resulting symbolic string
     * values.
     *
     * @param constraint The operation node constraint.
     */
    public void addOperation(PrintConstraint constraint) {

        // get constraint info as variables
        String string = constraint.getSplitValue();
        String actualVal = constraint.getActualVal();
        int id = constraint.getId();
        Map<String, Integer> sourceMap = constraint.getSourceMap();

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
            return;
        }

        // process operation based on function name
        if ((fName.equals("append")) || fName.equals("concat")) {

            processAppend(constraint);

        } else if (fName.equals("<init>")) {

            processInit(constraint);

        } else if (string.startsWith("substring")) {

            processSubstring(constraint);

        } else if (fName.equals("setLength")) {

            processSetLength(constraint);

        } else if (fName.equals("insert")) {

            processInsert(constraint);

        } else if (fName.equals("setCharAt")) {

            processSetCharAt(constraint);

        }
        //TODO: Check for 2 cases: Restricted any string and more then 2
        // leading white space chars

        //For some reason it fails when it is any string of any length. This
        // hack fixes it (woo). Check should be done in strangerlib.
        else if (fName.equals("trim")) {

            // perform trim operation
            solver.trim(id, base);

        } else if (fName.equals("delete")) {

            processDelete(constraint);

        } else if (fName.equals("deleteCharAt")) {

            processDeleteCharAt(constraint);

        } else if (fName.equals("reverse")) {

            // perform reverse operation
            solver.reverse(id, base);

        } else if (fName.startsWith("replace")) {

            processReplace(constraint);

        } else if (fName.equals("toUpperCase") && sourceMap.size() == 1) {

            // perform uppercase operation
            solver.toUpperCase(id, base);

        } else if (fName.equals("toLowerCase") && sourceMap.size() == 1) {

            // perform lowercase operation
            solver.toLowerCase(id, base);

        } else if (fName.equals("toString") ||
                   fName.equals("valueOf") ||
                   fName.equals("intern") ||
                   fName.equals("trimToSize") ||
                   (fName.equals("copyValueOf") && sourceMap.size() == 2) ||
                   fName.equals("length") ||
                   fName.equals("charAt")){
            processPropagation(constraint);

        } else {

            // create symbolic string
            solver.newSymbolicString(id);
        }
        
//        if(id == 106325 || id == 106323 || id == 106321 || id == 106318 || id == 106319 || id == 106316 
//        		|| id == 106314){
//    		System.out.println(id + " " + base + " " + actualVals.get(base) + " " + string);
//    	}
        
        if(solver instanceof ConcreteSolver && ConcreteSolver.DEBUG){
        	//check the actual and concrete values
        	ConcreteSolver cs = (ConcreteSolver) solver;
        	String calString = cs.getValue(id).getValue();
        	if(fName.equals("length")){
        		calString = String.valueOf(calString.length());
        	}
        	if(fName.equals("charAt")){
        		int index = Integer.parseInt(actualVals.get(arg));
        		if(calString != null && index < calString.length()){
        			calString = String.valueOf(calString.charAt(index));
        		}
        	}
        	
        	String actString = actualVals.get(id);
        	if(!actString.equals(calString)){
        		System.err.println(id  + " Concrete and Actual do not match \t" + calString + "\t" + actString 
        				+ "\t" + constraint + " base " + base + " " +
        				actualVals.get(base) + " args " + arg + " " + actualVals.get(arg));
        		//System.exit(2);
        		//fix it for toString
        		if(fName.endsWith("toString")){
        			System.err.println("Fixing toString()");
        			solver.newConcreteString(id, actString);
        			calString = cs.getValue(id).getValue();
        			if(!actString.equals(calString)){
        				System.err.println("Did not fix toString!!!");
        			}
        		}
        		
        	}
        	
        }
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#append(AbstractStringBuilder)}</li>
     * <li>{@link java.lang.StringBuffer#append(boolean)}</li>
     * <li>{@link java.lang.StringBuffer#append(char)}</li>
     * <li>{@link java.lang.StringBuffer#append(char[])}</li>
     * <li>{@link java.lang.StringBuffer#append(char[], int, int)}</li>
     * <li>{@link java.lang.StringBuffer#append(CharSequence)}</li>
     * <li>{@link java.lang.StringBuffer#append(CharSequence, int, int)}</li>
     * <li>{@link java.lang.StringBuffer#append(double)}</li>
     * <li>{@link java.lang.StringBuffer#append(float)}</li>
     * <li>{@link java.lang.StringBuffer#append(int)}</li>
     * <li>{@link java.lang.StringBuffer#append(long)}</li>
     * <li>{@link java.lang.StringBuffer#append(Object)}</li>
     * <li>{@link java.lang.StringBuffer#append(String)}</li>
     * <li>{@link java.lang.StringBuffer#append(StringBuffer)}</li>
     * <li>{@link java.lang.StringBuilder#append(AbstractStringBuilder)}</li>
     * <li>{@link java.lang.StringBuilder#append(boolean)}</li>
     * <li>{@link java.lang.StringBuilder#append(char)}</li>
     * <li>{@link java.lang.StringBuilder#append(char[])}</li>
     * <li>{@link java.lang.StringBuilder#append(char[], int, int)}</li>
     * <li>{@link java.lang.StringBuilder#append(CharSequence)}</li>
     * <li>{@link java.lang.StringBuilder#append(CharSequence, int, int)}</li>
     * <li>{@link java.lang.StringBuilder#append(double)}</li>
     * <li>{@link java.lang.StringBuilder#append(float)}</li>
     * <li>{@link java.lang.StringBuilder#append(int)}</li>
     * <li>{@link java.lang.StringBuilder#append(long)}</li>
     * <li>{@link java.lang.StringBuilder#append(Object)}</li>
     * <li>{@link java.lang.StringBuilder#append(String)}</li>
     * <li>{@link java.lang.StringBuilder#append(StringBuffer)}</li>
     * <li>{@link java.lang.String#concat(String)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processAppend(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get arg id from source map, -1 if none
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // if argument not set
        if (sourceMap.get("s1") == null) {
            arg = this.generateNextId();
            solver.newSymbolicString(arg);
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
        // stringBuilder.append(String str)
        // if only param is a string
        else if (string.split("!!")[1].equals("Ljava/lang/String;") &&
                 arg != -1 && solver.getValue(arg) == null) {

            // if actual value exists
            String argValue = actualVals.get(arg);
            if (argValue != null) {

                // set arg symbolic string from actual value
                solver.newConcreteString(arg, argValue);

            } else {

                // set arg symbolic string to any string
                solver.newSymbolicString(arg);
            }
        }

        // perform append operation
        solver.append(id, base, arg);
    }

    /**
     * Deprecated. Ensures the result is a character.
     *
     * @param id The id of the constraint.
     */
    private void createChar(int id) {

        // get string representing char from actual values
        String val = actualVals.get(id);

        // create new symbolic string from char string
        solver.newConcreteString(id, val);
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#delete(int, int)}</li>
     * <li>{@link java.lang.StringBuilder#delete(int, int)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processDelete(PrintConstraint constraint) {

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
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#deleteCharAt(int)}</li>
     * <li>{@link java.lang.StringBuilder#deleteCharAt(int)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processDeleteCharAt(PrintConstraint constraint) {

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
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.String#String()}</li>
     * <li>{@link java.lang.StringBuffer#StringBuffer()}</li>
     * <li>{@link java.lang.StringBuilder#StringBuilder()}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processInit(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");
        
        //System.out.println("processInit " + id + " val " + actualVals.get(id));

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
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#insert(int, CharSequence)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, CharSequence, int, int)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, char[], int, int)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, boolean)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, char)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, char[])}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, double)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, float)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, int)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, long)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, Object)}</li>
     * <li>{@link java.lang.StringBuffer#insert(int, String)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, CharSequence)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, CharSequence, int, int)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, char[], int, int)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, boolean)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, char)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, char[])}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, double)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, float)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, int)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, long)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, Object)}</li>
     * <li>{@link java.lang.StringBuilder#insert(int, String)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processInsert(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get offset id
       // int offset = sourceMap.get("s1"); eas: it is a bug
        int offset = Integer.parseInt(actualVals.get(sourceMap.get("s1")));

        // get arg id
        int arg = sourceMap.get("s2");


        //TODO implement other insert

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
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.String#copyValueOf(char[])}</li>
     * <li>{@link java.lang.String#copyValueOf(char[], int, int)}</li>
     * <li>{@link java.lang.String#intern()}</li>
     * <li>{@link java.lang.String#toString}</li>
     * <li>{@link java.lang.String#valueOf(boolean)}</li>
     * <li>{@link java.lang.String#valueOf(char)}</li>
     * <li>{@link java.lang.String#valueOf(char[])}</li>
     * <li>{@link java.lang.String#valueOf(char[], int, int)}</li>
     * <li>{@link java.lang.String#valueOf(double)}</li>
     * <li>{@link java.lang.String#valueOf(float)}</li>
     * <li>{@link java.lang.String#valueOf(int)}</li>
     * <li>{@link java.lang.String#valueOf(long)}</li>
     * <li>{@link java.lang.String#valueOf(Object)}</li>
     * <li>{@link java.lang.StringBuffer#toString()}</li>
     * <li>{@link java.lang.StringBuffer#trimToSize()}</li>
     * <li>{@link java.lang.StringBuilder#toString()}</li>
     * <li>{@link java.lang.StringBuilder#trimToSize()}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processPropagation(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

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
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.String#replace(char, char)}</li>
     * <li>{@link java.lang.String#replace(CharSequence, CharSequence)}</li>
     * <li>{@link java.lang.String#replaceAll(String, String)}</li>
     * <li>{@link java.lang.String#replaceFirst(String, String)}</li>
     * <li>{@link java.lang.StringBuffer#replace(int, int, String)}</li>
     * <li>{@link java.lang.StringBuilder#replace(int, int, String)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processReplace(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        String string = constraint.getSplitValue();
        String fName = string.split("!!")[0];
        int id = constraint.getId();
        int base = sourceMap.get("t");

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

            // perform solver specific operation
            solver.replace(id, base, argOne, argTwo);

        }
        // string.replace(CharSequence target, CharSequence replacement)
        else if (string.split("!!")[1].equals("Ljava/lang/CharSequence;" +
                                              "Ljava/lang/CharSequence;")) {

            // get string representations
            String str1 = actualVals.get(argOne);
            String str2 = actualVals.get(argTwo);

            // set string representations
            solver.newConcreteString(argOne, str1);
            solver.newConcreteString(argTwo, str2);

            // perform solver specific operation
            solver.replace(id, base, argOne, argTwo);

        } else {

            solver.newSymbolicString(id);
        }
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#setCharAt(int, char)}</li>
     * <li>{@link java.lang.StringBuilder#setCharAt(int, char)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processSetCharAt(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        int arg;

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

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.StringBuffer#setLength(int)}</li>
     * <li>{@link java.lang.StringBuilder#setLength(int)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processSetLength(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

        // get length
        int s1Id = sourceMap.get("s1");
        String s1String = actualVals.get(s1Id);
        int length = Integer.parseInt(s1String);
        //System.out.println("Lenght " + length + " " + base + " " + actualVals.get(base).isEmpty());

        // perform set length operation
        solver.setLength(id, base, length);
    }

    /**
     * Determine and symbolically execute the correct append or concatenate
     * operation:
     * <ul>
     * <li>{@link java.lang.String#subSequence(int, int)}</li>
     * <li>{@link java.lang.String#substring(int)}</li>
     * <li>{@link java.lang.String#substring(int, int)}</li>
     * <li>{@link java.lang.StringBuffer#subSequence(int, int)}</li>
     * <li>{@link java.lang.StringBuffer#substring(int)}</li>
     * <li>{@link java.lang.StringBuffer#substring(int, int)}</li>
     * </ul>
     *
     * @param constraint The constraint corresponding to the operation.
     */
    private void processSubstring(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        int id = constraint.getId();
        int base = sourceMap.get("t");

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
            } else {

                // propagate
                solver.propagateSymbolicString(id, base);
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
    }

    public void addEnd(PrintConstraint constraint) {

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
        
      

        // if solver contains boolean function name
        String fName = string.split("!!")[0];
        if (ExtendedSolver.containsBoolFunction(fName)) {

            // calculate stats
            calculateStats(constraint);
        }
    }

    private void calculateStats(PrintConstraint constraint) {

        // get constraint info as variables
        Map<String, Integer> sourceMap = constraint.getSourceMap();
        StringBuilder stats = new StringBuilder();
        String actualVal = constraint.getActualVal();
        int base = sourceMap.get("t");

        // get id of second symbolic string if it exists
        int arg = -1;
        if (sourceMap.get("s1") != null) {
            arg = sourceMap.get("s1");
        }

        // determine if symbolic strings are singletons
        if (solver.isSingleton(base, actualVal) &&
            (sourceMap.get("s1") == null ||
             solver.isSingleton(sourceMap.get("s1"), actualVal))) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

        // store symbolic string values
        solver.setLast(base, arg);

        // test if true branch is SAT
        assertBooleanConstraint(true, constraint);
        if (solver.isSatisfiable(base)) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

        // revert symbolic string values
        solver.revertLastPredicate();

        // store symbolic string values
        solver.setLast(base, arg);

        // test if false branch is SAT
        assertBooleanConstraint(false, constraint);
        if (solver.isSatisfiable(base)) {
            stats.append("true \t");
        } else {
            stats.append("false\t");
        }

        // revert symbolic string values
        solver.revertLastPredicate();

        // if actual execution did not produce either true or false
        if (!actualVal.equals("true") && !actualVal.equals("false")) {

            System.err.println("warning constraint detected without " +
                               "true/false value");
            return;
        }

        // determine result of actual execution
        boolean result = true;
        if (actualVal.equals("false")) {
            result = false;
        }

        // branches disjoint?
        assertBooleanConstraint(result, constraint);

        // store symbolic string values
        solver.setLast(base, arg);

        assertBooleanConstraint(!result, constraint);

        // set yes or no for disjoint branches
        String disjoint = "yes";
        if (solver.isSatisfiable(base)) {
            disjoint = "no";
        }

        // add disjoint result to output string
        stats.append(disjoint);

        // revert symbolic string values
        solver.revertLastPredicate();

        // output count and increment
        System.out.format("%06d\t", constraint.getId());

        // output stats
        System.out.println(stats);
    }

    /**
     * Assert a predicate on a symbolic value from the following boolean
     * function:
     * <ul>
     * <li>{@link java.lang.String#contains(CharSequence)}</li>
     * <li>{@link java.lang.String#contentEquals(CharSequence)}</li>
     * <li>{@link java.lang.String#contentEquals(StringBuffer)}</li>
     * <li>{@link java.lang.String#endsWith(String)}</li>
     * <li>{@link java.lang.String#equals(Object)}</li>
     * <li>{@link java.lang.String#equalsIgnoreCase(String)}</li>
     * <li>{@link java.lang.String#isEmpty()}</li>
     * <li>{@link java.lang.String#matches(String)}</li>
     * <li>{@link java.lang.String#regionMatches(boolean, int, String, int, int)}</li>
     * <li>{@link java.lang.String#regionMatches(int, String, int, int)}</li>
     * <li>{@link java.lang.String#startsWith(String)}</li>
     * <li>{@link java.lang.String#startsWith(String, int)}</li>
     * </ul>
     *
     * @param result     Is it a true or false predicate.
     * @param constraint The the boolean constraint which is being asserted.
     */
    private void assertBooleanConstraint(boolean result,
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

    private int generateNextId() {

        // lazy load id set
        if (this.maxGraphId <= 0) {

            // get set of all print constraint ids
            for (PrintConstraint constraint : graph.vertexSet()) {
                if (constraint.getId() > this.maxGraphId) {
                    this.maxGraphId = constraint.getId();
                }
            }
        }

        // increment max graph id for next valid id
        this.maxGraphId++;

        // return valid new id
        return this.maxGraphId;
    }

    static {
        actualVals = new HashMap<>();
    }
}
