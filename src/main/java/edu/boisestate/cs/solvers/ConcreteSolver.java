package edu.boisestate.cs.solvers;

import java.util.Set;

import edu.boisestate.cs.analysis.Parser;


public class ConcreteSolver extends ModelCountSolver<ConcreteValue> {

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
	//		//return !infeasible.contains(id);
	//		return symbolicStringMap.get(id).isFeasible();
	//	}
	
	//in debug mode it will check weather the resulting
	//value is the same as the actual value collected
	//only applicable in re-run mode
	public static boolean DEBUG = true;

	public ConcreteSolver(int setBound) {
		super(setBound);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void newSymbolicString(int id) {
		//System.out.println("Symb " + id);
		//in the concrete solver this should be substituted for the
		//concrete string with the next value to analyze
		//String s = RandomString.randomString();
		//get actual value for now
		String s = Parser.actualVals.get(id);
//		System.out.println("New " + id + " - " + s);
//		if(id == 106319 || id == 106316 || id == 106314){
//			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
//			System.exit(2);
		//	System.out.println("New " + id + " - " + s);
		//}
		symbolicStringMap.put(id, new ConcreteValue(s));

	}

	@Override
	public void newConcreteString(int id, String string) {
//		if(id == 4182){ 
//			System.out.println("Concr " + id + " " + string);
//			}
		symbolicStringMap.put(id, new ConcreteValue(string));

	}

	@Override
	public void propagateSymbolicString(int id, int base) {
		//take the value of base and put it into id
		ConcreteValue baseStr = symbolicStringMap.get(base);
		symbolicStringMap.put(id, baseStr.copy());
		//System.out.println("propagate " + base + " to " + id + " as " + baseStr.getValue() + " to " + 
		//symbolicStringMap.get(id).getValue());
	}
	
	public void charAt(int id, int base, int index){
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			String resultStr = String.valueOf(baseCV.getValue().charAt(index));
			System.out.println(id + " " + base + " " + baseCV.getValue() + " " + index + " " + resultStr);
			symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}
		
	}

	@Override
	public void append(int id, int base, int arg, int start, int end) {
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		//first check for feasibility
		if(baseCV.isFeasible() && argCV.isFeasible()){
			//the actual call to the method
			//Retrieve the base and the arg strings
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			StringBuilder argStr = new StringBuilder(argCV.getValue());

			String resultStr = baseStr.append(argStr, start, end).toString();
			//put result into the map for that id
			symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			//set base as infeasible
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void append(int id, int base, int arg) {
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		//first check for feasibility
		if(baseCV.isFeasible() && argCV.isFeasible()){
			StringBuilder baseStr;
			StringBuilder argStr;
			//the actual call to the method
			//Retrieve the base and the arg strings
			if(baseCV.getValue() == null || argCV.getValue() == null){
				//System.out.println("Map " + symbolicStringMap + " b " + base + " a " + arg);
				//actual concrete value
				//System.out.println("acutal val for base " + Parser.actualVals.containsKey(base) + " " + Parser.actualVals.get(base));
				//System.out.println("acutal val for arg " + Parser.actualVals.containsKey(arg) + " " + Parser.actualVals.get(arg) +
				//		" result " + Parser.actualVals.containsKey(id) + " " + Parser.actualVals.get(id));
				//We can create a new value based on the concrete one and have arg
				//to have that concrete value
				String actualVal = Parser.actualVals.get(id);
				if(baseCV.getValue() != null && argCV.getValue() == null){
					String baseVal = baseCV.getValue();
					//System.out.println("actualVal " + actualVal + " baseVal " + baseVal);
					//now get the proper offset
					String retrivedVal = actualVal.substring(baseVal.length(), actualVal.length());
					//System.out.println("retrived " + retrivedVal);
					//fix arg
					argCV.setValue(retrivedVal);
				} else if (baseCV.getValue() == null && argCV.getValue() != null){
					System.err.println("Base is null -- unhandeled");
					System.exit(1);
				} else {
					//both of them are null
					System.err.println("Both base and arg are null - use concrete values -- unhandeled");
					System.exit(1);
				}
				
			} 
			
			//do regular
			 baseStr = new StringBuilder(baseCV.getValue());
			 argStr = new StringBuilder(argCV.getValue());
//			 if(id==106331){
//				 System.out.println(base + " " + baseStr + " " + arg + " " + argStr);
//			 }
			String resultStr = baseStr.append(argStr).toString();
			//put result into the map for that id
			symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void substring(int id, int base, int start) {
		ConcreteValue baseCV = symbolicStringMap.get(base);
		//first check for feasibility
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			String resultStr = baseStr.substring(start);
			//put result into the map for that id
			symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} 

	}

	@Override
	public void substring(int id, int base, int start, int end) {
				//first check for feasibility

		ConcreteValue baseCV = symbolicStringMap.get(base);
		//first check for feasibility
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			String resultStr = baseStr.substring(start, end);
			//put result into the map for that id
			symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			//put it as infeasible
			symbolicStringMap.put(id, new ConcreteValue());
		}
	}

	@Override
	public void setLength(int id, int base, int length) {
		//		if(isFeasible(base)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		baseStr.setLength(length);
		//		//put result into the map for that id
		//		symbolicStringMap.put(id, baseStr.toString());
		//		} else {
		//			infeasible.add(id);
		//		}
		
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			baseStr.setLength(length);
			symbolicStringMap.put(id, new ConcreteValue(baseStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void insert(int id, int base, int arg, int offset) {
		//first check for feasibility
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
					String argStr = argCV.getValue();
//					System.out.println("base " + Parser.actualVals.get(base) + " arg " + Parser.actualVals.get(arg) + " "
//							+ " offset " + offset + " idActual " + Parser.actualVals.get(id) + "\n basStr " + baseStr + 
//							" argStr " + argStr);
					String resultStr = baseStr.insert(offset, argStr).toString();
					//put result into the map for that id
//					System.out.println("Result is " + resultStr);
//					System.exit(1);
					symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}
	}

	@Override
	public void insert(int id, int base, int arg, int offset, int start, int end) {
		//		//first check for feasibility
		//				if(isFeasible(base) && isFeasible(arg)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		String argStr = symbolicStringMap.get(arg);
		//		String resultStr = baseStr.insert(offset, argStr, start, end).toString();
		//		//put result into the map for that id
		//		symbolicStringMap.put(id, resultStr);
		//				} else {
		//					infeasible.add(id);
		//				}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
				String argStr = argCV.getValue();
				String resultStr = baseStr.insert(offset, argStr, start, end).toString();
					//put result into the map for that id
					symbolicStringMap.put(id, new ConcreteValue(resultStr));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void setCharAt(int id, int base, int arg, int offset) {
		//		//first check for feasibility
		//				if(isFeasible(base) && isFeasible(arg)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		String argStr = symbolicStringMap.get(arg);
		//		baseStr.setCharAt(offset, argStr.toCharArray()[0]);
		//		//put result into the map for that id
		//		symbolicStringMap.put(id, baseStr.toString());
		//				} else {
		//					infeasible.add(id);
		//				}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
					String argStr = argCV.getValue();
					baseStr.setCharAt(offset, argStr.toCharArray()[0]);
					//put result into the map for that id
					symbolicStringMap.put(id, new ConcreteValue(baseStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void trim(int id, int base) {
		//		if(isFeasible(base)){
		//		String retStr = symbolicStringMap.get(base).trim();
		//		symbolicStringMap.put(id, retStr);
		//		} else {
		//			infeasible.add(id);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			String retStr = baseCV.getValue().trim();
			symbolicStringMap.put(id, new ConcreteValue(retStr));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void delete(int id, int base, int start, int end) {
		//		//first check for feasibility
		//				if(isFeasible(base)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		baseStr.delete(start, end);
		//		symbolicStringMap.put(id, baseStr.toString());
		//				} else {
		//					infeasible.add(id);
		//				}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			baseStr.delete(start, end);
			symbolicStringMap.put(id, new ConcreteValue(baseStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void deleteCharAt(int id, int base, int loc) {
		//		if(isFeasible(base)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		baseStr.deleteCharAt(loc);
		//		symbolicStringMap.put(id, baseStr.toString());
		//		} else {
		//			infeasible.add(id);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			baseStr.deleteCharAt(loc);
			symbolicStringMap.put(id, new ConcreteValue(baseStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}
	}

	@Override
	public void reverse(int id, int base) {
		//		if(isFeasible(base)){
		//		StringBuilder baseStr = new StringBuilder(symbolicStringMap.get(base));
		//		baseStr.reverse();
		//		symbolicStringMap.put(id, baseStr.toString());
		//		} else {
		//			infeasible.add(id);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			StringBuilder baseStr = new StringBuilder(baseCV.getValue());
			baseStr.reverse();
			symbolicStringMap.put(id, new ConcreteValue(baseStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void toUpperCase(int id, int base) {
		//		if(isFeasible(base)){
		//		String retStr = symbolicStringMap.get(base).toUpperCase();
		//		symbolicStringMap.put(id, retStr.toString());
		//		} else {
		//			infeasible.add(id);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			String retStr = baseCV.getValue().toUpperCase();
			symbolicStringMap.put(id, new ConcreteValue(retStr.toString()));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	@Override
	public void toLowerCase(int id, int base) {
		//		if(isFeasible(base)){
		//		String retStr = symbolicStringMap.get(base).toLowerCase();
		//		symbolicStringMap.put(id, retStr.toString());
		//		} else {
		//			infeasible.add(id);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		//System.out.println(baseCV);
		if(baseCV.isFeasible()){
			String retStr = baseCV.getValue().toLowerCase();
			symbolicStringMap.put(id, new ConcreteValue(retStr));
		} else {
			//put it as infeasible
			symbolicStringMap.put(id, new ConcreteValue());
		}
	}

	@Override
	public void replace(int id, int base, int argOne, int argTwo) {
		//		if(isFeasible(base) && isFeasible(argOne) && isFeasible(argTwo)){
		//		String baseStr = symbolicStringMap.get(base);
		//		String argOneStr = symbolicStringMap.get(argOne);
		//		String argTwoStr = symbolicStringMap.get(argTwo);
		//		symbolicStringMap.put(id, baseStr.replace(argOneStr, argTwoStr));
		//		
		//	} else {
		//		infeasible.add(id);
		//	}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argOneCV = symbolicStringMap.get(argOne);
		ConcreteValue argTwoCV = symbolicStringMap.get(argTwo);
		if(baseCV.isFeasible() && argOneCV.isFeasible()&& argTwoCV.isFeasible()){

			String baseStr = baseCV.getValue();
			String argOneStr = argOneCV.getValue();
			String argTwoStr = argTwoCV.getValue();
					//put result into the map for that id
		symbolicStringMap.put(id, new ConcreteValue(baseStr.replace(argOneStr, argTwoStr)));
		} else {
			symbolicStringMap.put(id, new ConcreteValue());
		}

	}

	/*
	 * Now we have predicates
	 */
	@Override
	public void contains(boolean result, int base, int arg) {
				setLast(base,arg);
		//		if(isFeasible(base) && isFeasible(arg)){
		//		String baseStr = symbolicStringMap.get(base);
		//		String argStr = symbolicStringMap.get(arg);
		//
		//		//If we see at least one argument being null then
		//		//nothing is changed and no counts are added
		//		//just to need to make sure that there is no "null"
		//		//in the concrete input, which should not be
		//		
		//		//if the current result is different
		//		//from the result of the actual trace
		//		//then set the base to null
		//		setToNull(result,baseStr.contains(argStr),base);
		//		} else {
		//			infeasible.add(base);
		//		}
			ConcreteValue baseCV = symbolicStringMap.get(base);
			ConcreteValue argCV = symbolicStringMap.get(arg);
			if(baseCV.isFeasible() && argCV.isFeasible()){
				String baseStr = baseCV.getValue();
				String argStr = argCV.getValue();
				checkResult(result, baseStr.contains(argStr), base, arg);
			} else {
				//set both to infeasible, but can do more checks
				baseCV.setInfeasible();
				argCV.setInfeasible();
			}
	}

	@Override
	public void endsWith(boolean result, int base, int arg) {
				setLast(base,arg);
		//		if(isFeasible(base) && isFeasible(arg)){
		//		String baseStr = symbolicStringMap.get(base);
		//		String argStr = symbolicStringMap.get(arg);
		//		setToNull(result,baseStr.endsWith(argStr),base);
		//		} else {
		//			infeasible.add(base);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			String baseStr = baseCV.getValue();
			String argStr = argCV.getValue();
			checkResult(result, baseStr.endsWith(argStr), base, arg);
		} else {
			//set both to infeasible, but can do more checks
			baseCV.setInfeasible();
			argCV.setInfeasible();
		}
	}

	@Override
	public void startsWith(boolean result, int base, int arg) {
				setLast(base,arg);
		//		if(isFeasible(base) && isFeasible(arg)){
		//		String baseStr = symbolicStringMap.get(base);
		//		String argStr = symbolicStringMap.get(arg);
		//		setToNull(result,baseStr.startsWith(argStr),base);
		//		} else {
		//			infeasible.add(base);
		//		}
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			String baseStr = baseCV.getValue();
			String argStr = argCV.getValue();
			checkResult(result, baseStr.startsWith(argStr), base, arg);
		} else {
			//set both to infeasible, but can do more checks
			baseCV.setInfeasible();
			argCV.setInfeasible();
		}
	}

	@Override
	public void equals(boolean result, int base, int arg) {
		setLast(base,arg);
		ConcreteValue baseCV = symbolicStringMap.get(base);
		ConcreteValue argCV = symbolicStringMap.get(arg);
		if(baseCV.isFeasible() && argCV.isFeasible()){
			String baseStr = baseCV.getValue();
			String argStr = argCV.getValue();
			checkResult(result,baseStr.equals(argStr),base, arg);
		} else {
			//both base and arg should be set to infeasible
			baseCV.setInfeasible();
			argCV.setInfeasible();
		}

	}

	@Override
	public void equalsIgnoreCase(boolean result, int base, int arg) {
				setLast(base,arg);
		//		if(isFeasible(base) && isFeasible(arg)){
		//		String baseStr = symbolicStringMap.get(base);
		//		String argStr = symbolicStringMap.get(arg);
		//		setToNull(result,baseStr.equalsIgnoreCase(argStr),base);
		//		} else {
		//			infeasible.add(base);
		//		}
				ConcreteValue baseCV = symbolicStringMap.get(base);
				ConcreteValue argCV = symbolicStringMap.get(arg);
				if(baseCV.isFeasible() && argCV.isFeasible()){
					String baseStr = baseCV.getValue();
					String argStr = argCV.getValue();
					checkResult(result,baseStr.equalsIgnoreCase(argStr),base, arg);
				} else {
					//both base and arg should be set to infeasible
					baseCV.setInfeasible();
					argCV.setInfeasible();
				}

	}

	@Override
	public void isEmpty(boolean result, int base) {
		setLast(base,-1);
		ConcreteValue baseCV = symbolicStringMap.get(base);
		if(baseCV.isFeasible()){
			String baseStr = symbolicStringMap.get(base).getValue();
			//System.out.println(base + " -> " + baseStr);
			checkResult(result, baseStr.isEmpty(), base, -1);
		}
		//base already should be in the set
		//if it is infeasible

	}

	@Override
	public String getSatisfiableResult(int id) {
		//return what is in the map
		return symbolicStringMap.get(id).getValue();
	}

	@Override
	public boolean isSatisfiable(int id) {
		//it is satisfiable if it is feasible
		return symbolicStringMap.get(id).isFeasible();
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

	//for predicates we need to set infeasible for both base and arg
	private void checkResult(boolean result, boolean actual, int base, int arg){
		//System.out.println("Setting to infeasible? " + base);
		if(result!=actual){
			//System.out.println(" -- yes");
			//create a whole new object
			symbolicStringMap.put(base, new ConcreteValue());
			//-1 values is taken where there is not arg in the predicate
			//as in case of isEmpty()
			if(arg!=-1){
				symbolicStringMap.put(arg, new ConcreteValue());
			}
		}
	}

	//	public void setInfeasible(int id){
	//		//infeasible.add(id);
	//		symbolicStringMap.get(id).setInfeasible();
	//		
	//	}

	@Override
	public int getModelCount(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<String> getAllVales(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String replaceEscapes(String value) {
		// TODO Auto-generated method stub
		return value;
	}

}
