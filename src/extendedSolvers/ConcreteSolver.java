package extendedSolvers;

import analysis.Parser;
import stringRandom.RandomString;


public class ConcreteSolver extends ExtendedSolver<String> {

	@Override
	public void newSymbolicString(int id) {
		System.out.println("Symb " + id);
		//in the concrete solver this should be substituted for the
		//concrete string with the next value to analyze
		//String s = RandomString.randomString();
		//get actual value for now
		String s = Parser.actualVals.get(id);
		System.out.println(id + " - " + s);
		symbolicStringMap.put(id, s);

	}

	@Override
	public void newConcreteString(int id, String string) {
		System.out.println("Concr " + id + " " + string);
		symbolicStringMap.put(id, string);

	}

	@Override
	public String replaceEscapes(String value) {
		//in concrete case all chars are legal
		//at least in the given random set we generate
		return value;
	}

	@Override
	public void propagateSymbolicString(int id, int base) {
		//take the value of base and put it into id
		String baseStr = symbolicStringMap.get(base);
		symbolicStringMap.put(id, baseStr);

	}

	@Override
	public void append(int id, int base, int arg, int start, int end) {
		//the actual call to the method
		//Retrieve the base and the arg strings
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		StringBuilder argStr = new StringBuilder(symbolicStringMap.get(arg));
		String resultStr = baseStr.append(argStr, start, end).toString();
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);
		

	}

	@Override
	public void append(int id, int base, int arg) {
		//the actual call to the method
		//Retrieve the base and the arg strings
		//System.out.println(symbolicStringMap + " " + base);
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		StringBuilder argStr = new StringBuilder(symbolicStringMap.get(arg));
		String resultStr = baseStr.append(argStr).toString();
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);

	}

	@Override
	public void substring(int id, int base, int start) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		String resultStr = baseStr.substring(start);
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);

	}

	@Override
	public void substring(int id, int base, int start, int end) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		String resultStr = baseStr.substring(start,end);
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);

	}

	@Override
	public void setLength(int id, int base, int length) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		baseStr.setLength(length);
		//put result into the map for that id
		symbolicStringMap.put(id, baseStr.toString());

	}

	@Override
	public void insert(int id, int base, int arg, int offset) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		String argStr = symbolicStringMap.get(arg);
		String resultStr = baseStr.insert(offset, argStr).toString();
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);

	}

	@Override
	public void insert(int id, int base, int arg, int offset, int start, int end) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		String argStr = symbolicStringMap.get(arg);
		String resultStr = baseStr.insert(offset, argStr, start, end).toString();
		//put result into the map for that id
		symbolicStringMap.put(id, resultStr);

	}

	@Override
	public void setCharAt(int id, int base, int arg, int offset) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		String argStr = symbolicStringMap.get(arg);
		baseStr.setCharAt(offset, argStr.toCharArray()[0]);
		//put result into the map for that id
		symbolicStringMap.put(id, baseStr.toString());

	}

	@Override
	public void trim(int id, int base) {
		String retStr = symbolicStringMap.get(base).trim();
		symbolicStringMap.put(id, retStr);

	}

	@Override
	public void delete(int id, int base, int start, int end) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		baseStr.delete(start, end);
		symbolicStringMap.put(id, baseStr.toString());

	}

	@Override
	public void deleteCharAt(int id, int base, int loc) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		baseStr.deleteCharAt(loc);
		symbolicStringMap.put(id, baseStr.toString());
	}

	@Override
	public void reverse(int id, int base) {
		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		baseStr.reverse();
		symbolicStringMap.put(id, baseStr.toString());

	}

	@Override
	public void toUpperCase(int id, int base) {
		String retStr = symbolicStringMap.get(base).toUpperCase();
		symbolicStringMap.put(id, retStr.toString());

	}

	@Override
	public void toLowerCase(int id, int base) {
		String retStr = symbolicStringMap.get(base).toLowerCase();
		symbolicStringMap.put(id, retStr.toString());

	}

	@Override
	public void replace(int id, int base, int argOne, int argTwo) {
		String baseStr = symbolicStringMap.get(base);
		String argOneStr = symbolicStringMap.get(argOne);
		String argTwoStr = symbolicStringMap.get(argTwo);
		symbolicStringMap.put(id, baseStr.replace(argOneStr, argTwoStr));

	}

	/*
	 * Now we have predicates
	 */
	@Override
	public void contains(boolean result, int base, int arg) {
		setLast(base,arg);
		String baseStr = symbolicStringMap.get(base);
		String argStr = symbolicStringMap.get(arg);

		//If we see at least one argument being null then
		//nothing is changed and no counts are added
		//just to need to make sure that there is no "null"
		//in the concrete input, which should not be
		
		//if the current result is different
		//from the result of the actual trace
		//then set the base to null
		setToNull(result,baseStr.contains(argStr),base);
	}

	@Override
	public void endsWith(boolean result, int base, int arg) {
		setLast(base,arg);
		String baseStr = symbolicStringMap.get(base);
		String argStr = symbolicStringMap.get(arg);
		setToNull(result,baseStr.endsWith(argStr),base);
	}

	@Override
	public void startsWith(boolean result, int base, int arg) {
		setLast(base,arg);
		String baseStr = symbolicStringMap.get(base);
		String argStr = symbolicStringMap.get(arg);
		setToNull(result,baseStr.startsWith(argStr),base);
	}

	@Override
	public void equals(boolean result, int base, int arg) {
		setLast(base,arg);
		String baseStr = symbolicStringMap.get(base);
		String argStr = symbolicStringMap.get(arg);	
		setToNull(result,baseStr.equals(argStr),base);

	}

	@Override
	public void equalsIgnoreCase(boolean result, int base, int arg) {
		setLast(base,arg);
		String baseStr = symbolicStringMap.get(base);
		String argStr = symbolicStringMap.get(arg);
		setToNull(result,baseStr.equalsIgnoreCase(argStr),base);

	}

	@Override
	public void isEmpty(boolean result, int base) {
		setLast(base,-1);
		String baseStr = symbolicStringMap.get(base);
		//System.out.println(base + " -> " + baseStr);
		setToNull(result, baseStr.isEmpty(), base);

	}

	@Override
	public String getSatisfiableResult(int id) {
		//return what is in the map
		return symbolicStringMap.get(id);
	}

	@Override
	public boolean isSatisfiable(int id) {
		//it is satifiable if the string is not null;
		return symbolicStringMap.get(id) == null ? false : true;
	}

	@Override
	public boolean isSingleton(int id) {
		//in concrete case it is always a singelton
		return true;
	}

	@Override
	public boolean isSingleton(int id, String actualValue) {
		//not always the same as the actual value
		//but should be on the same path
		//otherwise always return true
		return true;
	}

	@Override
	public boolean isSound(int id, String actualValue) {
		//cannot do that since we work with individual values
		//but perhaps can collect the strings that follow the same path
		//maybe later if needed
		return true;
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}
	
	private void setToNull(boolean result, boolean actual, int id){
		System.out.println("Setting to null? " + id);
		if(result!=actual){
			System.out.println(" -- yes");
			symbolicStringMap.put(id, null);
		}
	}

}
