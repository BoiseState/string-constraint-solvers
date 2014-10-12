package analysis;

import java.util.HashMap;
import java.util.Map;

import extendedSolvers.ExtendedSolver;

/**
 * Parses constraints and calls the appropriate solver method.
 * @author Scott Kausler
 *
 */
public class Parser {

	ExtendedSolver solver;
	Map<Integer, String> actualVals;
	
	private static final boolean DEBUG = false;
	
	public Parser(ExtendedSolver solver) {
		this.solver = solver;
		actualVals = new HashMap<Integer, String>();
		System.out.println("SING\tTSAT\tFSAT\tDISJOINT");
	}
	
	public void addRoot(String value, String actualValue, int id) {
		if(DEBUG){
			System.out.println("Root: " + value);
		}
		if(actualValue !=null){
			actualValue=solver.replaceExcapes(actualValue);
		}
		actualVals.put(id, actualValue);
		value=solver.replaceExcapes(value);

		if (value.startsWith("r")||value.startsWith("$r")){
			solver.newSymbolicString(id);
		}
		else {
			solver.newConcreteString(id, actualValue);
		}
	}


	public void addOperation(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {

		if(DEBUG){
			System.out.println("Operation: " + string);
		}
		
		actualVal= solver.replaceExcapes(actualVal);
		actualVals.put(id, actualVal);
		
		int base = sourceMap.get("t");
		String fName = string.split("!!")[0];

		int arg = -1;
		if(sourceMap.get("s1")!=null){
			arg = sourceMap.get("s1");
		}
		
		if(!solver.isValidState(base, arg)){
			solver.newSymbolicString(id);
			return;
		}
		
		if((fName.equals("append"))||fName.equals("concat")){

			 if(sourceMap.get("s1")==null)
				 solver.newSymbolicString(sourceMap.get("s1"));

			if(sourceMap.size()>3){
				if(string.split("!!")[1].startsWith("[CI")){
					arg = solver.getTempId();
					solver.newSymbolicString(arg);
				}
				else{
					int start=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
					int end=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
					solver.append(id, base, arg, start, end);
					return;
				}
			}
			else if(string.split("!!")[1].equals("C")){
				createChar(sourceMap.get("s1"));
			}
			else if(string.split("!!")[1].equals("Z")){
				try{
					int num=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
					if(num==1){
						solver.newConcreteString(arg, "true");
					}
					else{
						solver.newConcreteString(arg, "false");
					}
				}
				catch(NumberFormatException e){
					solver.newConcreteString(arg, actualVals.get(sourceMap.get("s1")));
				}
			}	
			solver.append(id, base, arg);
		}
		else if(fName.equals("<init>")){
			if(sourceMap.get("t")!=null && sourceMap.get("s1")!=null &&actualVals.get(sourceMap.get("t")).equals("")){
				solver.propagateSymbolicString(id, base);
			}
			else
				solver.newSymbolicString(id);
		}
		else if(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||(fName.equals("copyValueOf")&&sourceMap.size()==2)){
			if(!sourceMap.containsKey("t")){
				solver.propagateSymbolicString(id, arg);
			}
			else{
				solver.propagateSymbolicString(id, base);
			}
		}
		else if(string.startsWith("substring")){	
			if(sourceMap.size()==2){
				int start=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				if(start!=0)
					solver.substring(id, base, start);
			}
			else{
				int start=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				int end=Integer.parseInt(actualVals.get(sourceMap.get("s2")));				
				solver.substring(id, base, start, end);
			}
		}
		else if(fName.equals("setLength")){
			int length=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			solver.setLength(id, base, length);
		}
		//TODO implement other insert
		else if(fName.equals("insert")){
			arg = sourceMap.get("s2");
			int offset = sourceMap.get("s1");

			if(string.split("!!")[1].equals("IC")){
				createChar(arg);
			}
			else if(string.split("!!")[1].startsWith("I[C")){
				solver.newSymbolicString(arg);
			}
			else if(sourceMap.size()>3){
				int start=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
				int end=Integer.parseInt(actualVals.get(sourceMap.get("s4")));
				solver.insert(id, base, arg, offset, start, end);
			}
			else{
				solver.insert(id, base, arg, offset);
			}
		}
		else if(fName.equals("setCharAt")){
			arg = sourceMap.get("s2");
			createChar(arg);
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			solver.setCharAt(id, base, arg, offset);
		}
		//TODO: Check for 2 cases: Restricted any string and more then 2 leading white space chars
		
		//For some reason it fails when it is any string of any length. This hack fixes it (woo). Check should be done in strangerlib.
		else if(fName.equals("trim")){
			solver.trim(id, base);
		}
		else if(fName.equals("delete")){
			int start=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			int end=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
			solver.delete(id, base, start, end);
		}
		else if(fName.equals("deleteCharAt")){
			int loc=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			
			solver.deleteCharAt(id, base, loc);
		}
		else if(fName.equals("reverse")){
			solver.reverse(id, base);
		}
		else if(fName.equals("toUpperCase")&&sourceMap.size()==1){
			solver.toUpperCase(id, base);
		}
		else if(fName.equals("toLowerCase")&&sourceMap.size()==1){
			solver.toLowerCase(id, base);
		}
		else if(fName.startsWith("replace")){

			if(fName.equals("replaceAll")||fName.equals("replaceFirst")||string.split("!!")[1].startsWith("II")){
				solver.newSymbolicString(id);
				return;
			}
			//TODO: Make this version work
			if(sourceMap.size()!=3){
				solver.newSymbolicString(id);
				return;
			}
			int argOne = sourceMap.get("s1");
			int argTwo = sourceMap.get("s2");
			if(string.split("!!")[1].equals("CC")){
				createChar(argOne);
				createChar(argTwo);
			}
			solver.replace(id, base, argOne, argTwo);
		}
		else{
			solver.newSymbolicString(id);
		}
	}

	public void addEnd(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {
		if(DEBUG){
			System.out.println("End: " + string);
		}
		
		actualVal= solver.replaceExcapes(actualVal);
		actualVals.put(id, actualVal);

		String fName=string.split("!!")[0];

		if(ExtendedSolver.containsBoolFunction(fName)){
			calculateStats(fName, actualVal, sourceMap);

		}
	}
	
	private void calculateStats(String fName, String actualVal,
			HashMap<String, Integer> sourceMap){
		int base = sourceMap.get("t");
		String stats = "";
		if(solver.isSingleton(base) && (sourceMap.get("s1")==null || 
				solver.isSingleton(sourceMap.get("s1"))))
			stats += "true\t";
		else
			stats += "false\t";
		assertBooleanConstraint(fName, true, sourceMap);
		stats += solver.isSatisfiable(base)+"\t";
		solver.revertLastPredicate();
		assertBooleanConstraint(fName, false, sourceMap);
		stats += solver.isSatisfiable(base)+"\t";
		solver.revertLastPredicate();
		
		if(!actualVal.equals("true")&& !actualVal.equals("false")){
			System.err.println("warning constraint detected without true/false value");
			return;
		}
		boolean result=true;
		if(actualVal.equals("false"))
			result=false;
		assertBooleanConstraint(fName, result, sourceMap);
		assertBooleanConstraint(fName, !result, sourceMap);
		stats += solver.isSatisfiable(base);
		solver.revertLastPredicate();
		System.out.println(stats);
	}
	
	/**
	 * Depricated. Ensures the result is a character.
	 * @param id The id of the constraint.
	 */
	private void createChar(int id){
		solver.newConcreteString(id, actualVals.get(id));
	}

	/**
	 * Assert a predicate on a symbolic value.
	 * @param result Is it a true or false predicate.
	 * @param method The predicate method.
	 * @param actualValue The actual result.
	 * @param id The predicate id.
	 * @param sourceMap The values involved.
	 */
	private void assertBooleanConstraint(String method, boolean result,
				HashMap<String, Integer> sourceMap) {
		String fName = method.split("!!")[0];

		int base =(sourceMap.get("t"));
		
		int arg=-1;
		if(sourceMap.get("s1")!=null){
			arg =sourceMap.get("s1");
		}

		if (fName.equals("contains")) {
			solver.contains(result, base, arg);

		} else if (fName.equals("endsWith")) {	
			solver.endsWith(result, base, arg);
		}
		else if(fName.equals("startsWith")&&sourceMap.size()==2) {
			solver.startsWith(result, base, arg);
		}
		
		else if(fName.equals("equals") || fName.equals("contentEquals")){
			solver.equals(result, base, arg);
			
		} else if (fName.equals("equalsIgnoreCase")) {
			solver.equalsIgnoreCase(result, base, arg);
		}
		else if(fName.equals("isEmpty")){
			solver.isEmpty(result, base);
		}				
	}
	
	public void remove(int id){
		solver.remove(id);
	}

	public void finishUp() {
		// TODO Auto-generated method stub
		
	}
}
