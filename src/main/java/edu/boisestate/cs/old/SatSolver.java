package edu.boisestate.cs.old;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class SatSolver implements Solver{
	private String solver;
	private Process p=null;
	private BufferedReader in=null;
	private ExecutorService executor;
	private String fileName;
	private String solverName;
	protected StoreToFile store;
	protected String option;
	
	protected String properties;
	protected String fileWrite;
	protected String execResult;
	private static StringCode stringCode=new StringCode();
	
	protected Set<Integer> symbolicOps;
	protected Map<Integer, String> actualVals;
	protected Set<Integer> symbolics;
	protected boolean taint;
	protected String completeResult;
	protected long tempTime;
	protected long startupTime;
	protected long timeout;
	protected Set<Integer> toTaints;
	
	protected Set<Integer> numUnsound;
	protected Set<Integer> numOver;
	protected Set<Integer> unknown;
	protected Set<Integer> trueUnsat;
	protected Set<Integer> falseUnsat;
	protected Set<Integer> unsat;
	protected Set<Integer> sat;
	protected Set<Integer> satTimeout;
	protected Set<Integer> unsatTimeout;

	protected Set<Integer> subsets;
	
	protected Set<Integer> accuratePaths;
	protected int hotSpots;
	protected Set<Integer> trackedTimeouts;
	protected Set<Integer> otherTimeouts;
	protected int numConstraints;
	protected int numOperations;
	protected Set<Integer> correctHotspot, incorrectHotspot, unknownHotspot;
	protected Set<Integer> correctConstraint, incorrectConstraint, unknownConstraint;
	protected Set<Integer> toSingleton;
	
	protected HashMap<Integer,Long> hotSpotTime;
	protected HashMap<Integer,Long> constraintTime;

	protected static HashMap<Integer, LinkedList<String>> pastLists;

	public abstract void addRoot(String value, String actualValue, int id);
	public abstract void addOperation(String string, String actualVal, int id, HashMap<String, Integer> sourceMap);
	public abstract void addEnd(String string, String actualValue, int id, HashMap<String, Integer> sourceMap);
	public abstract void remove(int id);
	protected Runtime rt;
	private String killString;
	
	protected SatSolver(String solverName, String solver, String killString, String properties, String tempFile){
		hotSpotTime=new HashMap<Integer,Long>();
		constraintTime=new HashMap<Integer,Long>();
		pastLists=new HashMap<Integer, LinkedList<String>>();

		option="";
		this.properties=properties;
		this.solver=solver;
		this.solverName=solverName;
		fileWrite="";
		this.killString=killString;
		symbolics=new HashSet<Integer>();
		symbolicOps=new HashSet<Integer>();
		actualVals=new HashMap<Integer, String>();
		numUnsound=new HashSet<Integer>();
		numOver=new HashSet<Integer>();
		unknown=new HashSet<Integer>();
		trueUnsat=new HashSet<Integer>();
		falseUnsat=new HashSet<Integer>();
		unsat=new HashSet<Integer>();
		sat=new HashSet<Integer>();
		subsets=new HashSet<Integer>();
	  	toTaints=new HashSet<Integer>();
	  	satTimeout=new HashSet<Integer>();
	  	unsatTimeout=new HashSet<Integer>();
	  	toSingleton=new HashSet<Integer>();
		hotSpots=0;
		numOperations=0;
		correctHotspot=new HashSet<Integer>();
		incorrectHotspot=new HashSet<Integer>();
		unknownHotspot=new HashSet<Integer>();
		correctConstraint=new HashSet<Integer>();
		incorrectConstraint=new HashSet<Integer>();
		unknownConstraint=new HashSet<Integer>();
		startupTime=0;
		tempTime=0;
		numConstraints=0;
		accuratePaths=new HashSet<Integer>();
		solverName="";
		fileName=tempFile;
		
		trackedTimeouts=new HashSet<Integer>();
		otherTimeouts=new HashSet<Integer>();
		timeout= Long.MAX_VALUE;
        executor = Executors.newFixedThreadPool(2);
        rt = Runtime.getRuntime();
        
		store=new StoreToFile();
	}
	/**
	 * When a timeout occurs and a branching point is no long considered, reverts the values in an already evaluated solver.
	 * @param id
	 */
	public void revertBP(int id){
		numUnsound.remove(id);
		numOver.remove(id);
		unknown.remove(id);
		trueUnsat.remove(id);
		falseUnsat.remove(id);
		unsat.remove(id);
		sat.remove(id);
		satTimeout.remove(id);
		unsatTimeout.remove(id);
		subsets.remove(id);
		accuratePaths.remove(id);
		correctConstraint.remove(id);
		incorrectConstraint.remove(id);
		unknownConstraint.remove(id);
		constraintTime.remove(id);
	}
	/** Used to create a new list of constraints encountered so far.
	 * @param value The constraint value
	 * @param actualValue The actual value
	 * @param id the unique id.
	 */
	protected void addNewPastList(String value, String actualValue, int id){
		if(!pastLists.containsKey(id)){
			LinkedList<String> pastList=new LinkedList<String>();
			pastList.add(value);
			pastLists.put(id, pastList);
			stringCode.addRoot(value, actualValue, id);
		}
	}
	/** Adds to a previous list of constraints encountered so far.
	 * @param value The constraint value
	 * @param actualValue The actual value
	 * @param id the unique id.
	 * @param sourceMap The map of values involved
	 * @param isPred True if the constraint is at a branching point.
	 */
	protected void appendPastList(String value, String actualValue, int id,
								  HashMap<String, Integer> sourceMap, boolean isPred){
		if(!pastLists.containsKey(id)){
			LinkedList<String> pastList=new LinkedList<String>();
			pastList.addAll(pastLists.get(sourceMap.get("t")));
			for(int i=0; i<6; i++){
				if(sourceMap.containsKey("s"+i)){
					pastList.addAll(pastLists.get(sourceMap.get("s"+i)));
				}
			}
			pastList.add(value);
			pastLists.put(id, pastList);
			if(isPred){
				pastLists.put(sourceMap.get("t"), pastList);
				stringCode.addEnd(value, actualValue, id, sourceMap);
			}
			else
				stringCode.addOperation(value, actualValue, id, sourceMap);

		}		
	}
	public void addHeight(int height){}
	
	/**
	 * Used in tracking if initial values produced lead down the correct path.
	 * @param assignments
	 * @param id
	 * @return
	 */
	protected boolean getStringCodeResult(HashMap<String,String> assignments, int id){
		return stringCode.exec(assignments, id);
	}
	
	public static LinkedList<String> getPastList(int id){
		return pastLists.get(id);
	}
	/**
	 * Check if a pastList contains a value.
	 * @param id
	 * @param val
	 * @return
	 */
	public static boolean checkPastListContains(int id, String val){
		for(String s:pastLists.get(id)){
			if(s.startsWith(val))
				return true;
		}
		return false;
	}
	public int getAccuratePaths(){
		return accuratePaths.size();
	}
	public int getNumUnsound(){
		return numUnsound.size();
	}
	public int getNumOver(){
		return numOver.size();
	}
	public int getNumConstraints(){
		return numConstraints;
	}
	public int getNumHotSpots(){
		return hotSpots;
	}
	public int getNumOperations(){
		return numOperations;
	}
	public int getCorrectHotspot(){
		return correctHotspot.size();
	}
	public int getIncorrectHotspot(){
		return incorrectHotspot.size();
	}
	public int getUnknownHotspot(){
		return unknownHotspot.size();
	}
	public int getCorrectConstraint(){
		return correctConstraint.size();
	}
	public int getIncorrectConstraint(){
		return incorrectConstraint.size();
	}
	public int getUnknownConstraint(){
		return unknownConstraint.size();
	}
	public int getNumSymbolicOps(){
		return symbolicOps.size();
	}
	public int getUnknown(){
		return unknown.size();
	}
	public int getTrueUnsat(){
		return trueUnsat.size();
	}
	public int getFalseUnsat(){
		return falseUnsat.size();
	}
	public int getUnsat(){
		return unsat.size();
	}
	public int getSat(){
		return sat.size();
	}
	public int getSatTimeout(){
		return satTimeout.size();
	}
	public int getUnsatTimeout(){
		return unsatTimeout.size();
	}
	public int getSubsets(){
		return subsets.size();
	}
	public String getName(){
		return solverName;
	}
	
	public void finishUp(){
		executor.shutdown();
	}
	@Override
	protected void finalize(){
		executor.shutdown();
	}
	public int getTimeouts(){
		return trackedTimeouts.size()+otherTimeouts.size();
	}
	public boolean isTimeout(int id){
		return trackedTimeouts.contains(id) || otherTimeouts.contains(id);
	}
	
	public boolean isOtherTimeout(int id){
		return otherTimeouts.contains(id);
	}
	public boolean isTrackedTimeout(int id){
		return trackedTimeouts.contains(id);
	}
	
	public long getTempTime(){
		return tempTime;
	}
	public boolean getToTaint(int id){
		return toTaints.contains(id);
	}
	public long getTotalConstraintTime(){
		return calcTotalTime(constraintTime);
	}
	public long getTotalHotSpotTime(){
		return calcTotalTime(hotSpotTime);
	}
	public long getMediumConstraintTime(){
		return calcMediumTime(constraintTime);
	}
	public long getMediumHotSpotTime(){
		return calcMediumTime(hotSpotTime);
	}
	private long calcTotalTime(HashMap<Integer,Long> list){
		long time=0;
		Iterator<Long> it=list.values().iterator();
		while(it.hasNext()){
			time+=it.next();
		}
		return time;
	}
	private long calcMediumTime(HashMap<Integer,Long> list){
		if(list.size()==0)
			return 0;
		ArrayList<Long> newList=new ArrayList<Long>(list.values());
		Collections.sort(newList);
		return newList.get(newList.size()/2);
	}
	public boolean getTaint(){
		return taint;
	}
	public String getResult(){
		return completeResult;
	}
	
	public void getStats() {
		fileWrite+=endResults();
		fileWrite+="\nBoolean constraints:\n";
		fileWrite+="Correct bools:\t"+correctConstraint;
		fileWrite+=" Unknown bools:\t"+unknownConstraint;
		fileWrite+=" Incorrect bools:\t"+incorrectConstraint+"\n";
	}
	public String endResults() {
		String result="********************************\n";
		result+=solverName+" Stats:\n";
		result+="Hotspots: "+hotSpots+"\n";
		result+="Number of String Constraints:\t"+numOperations+"\n";
		result+="Number of Boolean Assertions:\t"+numConstraints+"\n";
		result+="Constraint solver time:\t"+getTotalConstraintTime()+"\tnano seconds\n";
		result+="Symbolic values:\t"+symbolics.size()+"\n";
		result+="Approximated operations:\t"+symbolicOps.size()+"\n";
		result+="Timeouts:\t"+getTimeouts()+"\n";
		result+="Hotspots:\n";
		result+= "Number correct:\t"+correctHotspot;
		result+= " Number unknown:\t"+unknownHotspot;
		result+= " Number incorrect:\t"+incorrectHotspot;
		return result;
	}
	
	/**
	 * Executes a query for solvers that run in a stanalone program (Z3-str).
	 * @param solveString Contains the constraints to be evaluated in the language of the solver.
	 * @param trackConstraintTime True if this should go into evaluation of time at branching points.
	 * @param trackHotTime True if this should go into evaluation of time at hotspots.
	 * @param isTrueBranch True if this if for evaluation of a true branch.
	 * @param id An id for the constraint to be evaluated.
	 */
	protected void exec(String solveString, boolean trackConstraintTime, boolean trackHotTime, boolean isTrueBranch, int id) {
		execResult="";
        FileWriter fileWriter = null;
        File textFile = new File(fileName);

        	long startTime=0;
        	int pid=-1;
	        try {  
	             textFile.delete();
	              fileWriter = new FileWriter(textFile);
	              fileWriter.write(solveString);
	              fileWriter.close();

			      String[] args;
			      if(!option.equals("")){
			    	  args=new String[3];
			    	  args[0]=solver;
			    	  args[1]=option;
			    	  args[2]=textFile.getAbsolutePath();
			      }
			      else{
			    	  args=new String[2];
			    	  args[0]=solver;
			    	  args[1]=textFile.getAbsolutePath();
			      }
			    	  
		  		 startTime = System.nanoTime();
		         p = rt.exec(args);
//		         Field f = p.getClass().getDeclaredField("pid");
//		            f.setAccessible(true);
//		            pid = (Integer) f.get(p);
			        }
	       catch (Exception ioe) {
		         System.err.println("Error:    in I/O processing of tempfile\n");
	 		    System.err.println("       or in calling external command");
	 		    ioe.printStackTrace();
		   }
		         in = new BufferedReader(
	                 new InputStreamReader(p.getInputStream()));
		         String line="";
		         
		         // Read data with timeout
		         Callable<String> readTask = new Callable<String>() {
		             @Override
		             public String call() throws Exception {
		            	 String next=in.readLine();
		                 return next;
		             }
		         };
		         while (line!=null) {
		             Future<String> future = executor.submit(readTask);

		             try {
						line = future.get(timeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException f) {
						// TODO Auto-generated catch block
						f.printStackTrace();
					} catch (TimeoutException g) {
						if(trackConstraintTime){
							if(isTrueBranch)
								trackedTimeouts.add(id);
							else
								otherTimeouts.add(id);
						}

					      try
					      {
//					    	  if(pid==-1){
//					    		  System.err.println("Warning... killall");
					    		  rt.exec("killall -9 "+killString);
//					    	  }
//					    	  else
//					    		  rt.exec("kill -9 "+pid);
					      }
					      catch(IOException e2)
					      {
					         System.err.println("Error on exec() method");
					         e2.printStackTrace();  
					      }
						line=null;
						execResult=null;
						break;
					}
		             if (line !=null)
		            	 execResult+= convertascii(line)+"\n";
		          }
	 	  			long endTime = System.nanoTime();
	 	  			
	 	  			try{
		 	  			if(execResult!=null){
							double tempDouble=
									Double.parseDouble(execResult.split("Time\\(avg\\) =")[1].split("\\(s\\)")[0]);
							double reportedTime= tempDouble *
												 Integer.parseInt(execResult.split("Runs: ")[1].split("\n")[0]);
							tempTime=(long) (1000000000*reportedTime);
		 	  			}
		 	  			else{
		 	  				tempTime=endTime-startTime-startupTime;
		 	  			}
	 	  			}
	 	  			catch(java.lang.ArrayIndexOutOfBoundsException e){
	 	  				tempTime=endTime-startTime-startupTime;
	 	  			}
//	 	  			tempTime=endTime-startTime;
	 	  			if(trackConstraintTime){
	 	  				long temp=0;
	 	  				if(constraintTime.containsKey(id))
	 	  					temp=constraintTime.get(id);
	 	  				long computation=tempTime;
	 	  				if(computation<=0){
	 	  					System.err.println("Negitive time:" + tempTime);
	 	  					System.err.println(solveString);
	 	  				}
	 	  				constraintTime.put(id, temp+(computation));
	 	  			}
	 	  			else if(trackHotTime){
	 	  				long temp=0;
	 	  				if(hotSpotTime.containsKey(id))
	 	  					temp=hotSpotTime.get(id);
	 	  				hotSpotTime.put(id, temp+(tempTime));
	 	  			}
		         if (textFile.delete() == false) 
			            System.err.println("Warning: " + textFile.getAbsolutePath() + " could not be deleted!");
	    }
	
	//Should be ignored unless for some reason you want to convert the output
	protected String convertascii(String line) {
		return line;		
	}
	public static boolean isInteger(String s) {
		int radix=10;
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i), radix) < 0) return false;
	    }
	    return true;
	}
	/**
	 * Uses string methods to check if the actual result for concrete values should be true or false.
	 * @param fName Contains the method name.
	 * @param baseGuess The target
	 * @param argGuess The argument
	 * @param sourceMap A map of ids for source symbolic values.
	 * @return A string for a result of the string predicate.
	 */
	protected String getBooleanResult(String fName, String baseGuess, String argGuess, HashMap<String, Integer> sourceMap){
		String result=null;
		if(baseGuess==null)
			return null;
		if(fName.equals("contains")||fName.equals("contentEquals")||fName.equals("endsWith")||fName.equals("startsWith")||fName.equals("equals")||fName.equals("equalsIgnoreCase")||fName.equals("matches")){
			if(fName.equals("contains")){
				result= Boolean.toString(baseGuess.contains(argGuess));
			}
			else if(fName.equals("contentEquals")){
				result= Boolean.toString(baseGuess.contentEquals(argGuess));
			}
			else if(fName.equals("endsWith")){
				result= Boolean.toString(baseGuess.endsWith(argGuess));
			}
			else if(fName.equals("equalsIgnoreCase")){
				result= Boolean.toString(baseGuess.equalsIgnoreCase(argGuess));
			}
			else if(fName.equals("equals")){
				result= Boolean.toString(baseGuess.equals(argGuess));
			}
			//TODO add second version of startsWith
			else if(fName.equals("startsWith")){
				result= Boolean.toString(baseGuess.startsWith(argGuess));
			}
			else{
				result= Boolean.toString(baseGuess.matches(argGuess));
			}
		}
		else if(fName.equals("isEmpty")){
			result= Boolean.toString(baseGuess.isEmpty());
		}
		else if(fName.equals("regionMatches")){
			if(sourceMap.size()==5){
				int s1= Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				int s3= Integer.parseInt(actualVals.get(sourceMap.get("s3")));
				int s4= Integer.parseInt(actualVals.get(sourceMap.get("s4")));
	
				result= Boolean.toString(baseGuess.regionMatches(s1, argGuess, s3, s4));
			}
			else{
				boolean s1=
						Boolean.parseBoolean(actualVals.get(sourceMap.get("s1")));
				int s2= Integer.parseInt(actualVals.get(sourceMap.get("s2")));
				int s4= Integer.parseInt(actualVals.get(sourceMap.get("s4")));
				int s5= Integer.parseInt(actualVals.get(sourceMap.get("s5")));
				result= Boolean.toString(baseGuess.regionMatches(s1, s2, argGuess, s4, s5));
			}
		}
		else
			System.err.println("Warning: inproper boolean call");
		return result;
	}
	/**
	 * Used only with JSASolver. Replaces certain expressions with the actual escape character.
	 * @param value The string expression to change
	 * @return The modified version of the argument.
	 */
	protected static String replaceExcapes(String value){
//		value=value.replace("\\\\", "\\\\");
		if(value==null)
			return null;

		value=value.replaceAll("\\\\b", "\b");
		value=value.replaceAll("\\\\t", "\t");
		value=value.replaceAll("\\\\n", "\n");
		value=value.replaceAll("\\\\f", "\f");
		value=value.replaceAll("\\\\r", "\r");
		value=value.replaceAll("\\\\\"", "\"");
		value=value.replaceAll("\\\\\'", "\'");
		
		return value;
	}
	
	public void writeToFile(){
		System.out.println(fileWrite);
//		try {
//		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("stats.txt", true)));
//		    out.println(fileWrite);
//		    out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		}
	
	/**
	 * Checks if the parameter contains a predicate method.
	 * @param string The name of the method to be checked.
	 * @return true if the parameter is a predicate.
	 */
	protected static boolean containsBoolFunction(String string){
		String fName=string.split("!!")[0];
		if(fName.equals("equals")||fName.equals("contains")||fName.equals("contentEquals")||fName.equals("endsWith")||fName.equals("startsWith")
				||fName.equals("equalsIgnoreCase")||fName.equals("matches")||fName.equals("isEmpty")||fName.equals("regionMathes"))
			return true;
		return false;
	}
	/**
	 * Checks if the parameter contains a hybrid integer method.
	 * @param string The name of the method to be checked.
	 * @return true if the parameter is an integer method.
	 */
	protected static boolean containsIntFunction(String string){
		String fName=string.split("!!")[0];
		if(fName.equals("charAt")||fName.equals("indexOf")||fName.equals("length")||fName.equals("lastIndexOf"))
			return true;
		return false;
	}
	/**
	 * Propagates taints for a value.
	 * @param val The id of this constraint.
	 * @param sourceMap The list of source vertices which may be tainted.
	 */
	protected void addTaints(int val, Collection<Integer> sourceMap){
		Iterator<Integer> it=sourceMap.iterator();
		while(it.hasNext()){
			int i=it.next();
			if(store.getTaint(i)){
				store.setTaint(val, true);
				return;
			}
		}
		store.setTaint(val, false);
	}
	/**
	 * Sets a symbolic value's sources to be tainted with over-approximation (for a predicate method).
	 * @param sourceMap The map of constraints to be tainted.
	 */
	protected void newTaint(HashMap<String, Integer> sourceMap){
		Iterator<Integer> it=sourceMap.values().iterator();
		while(it.hasNext()){
			int i=it.next();
			store.setTaint(i, true);
		}
	}
	
	/** Processes taint for an operation. If the operation modifies a value, it is tainted.
	 * 
	 * @param string The name of the operation
	 * @param id The id.
	 * @param sourceMap The sources of the operation..
	 */
	protected void processTaint(String string, int id,
								HashMap<String, Integer> sourceMap){
		addTaints(id, sourceMap.values());

		String fName = string.split("!!")[0];
		if(!(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||fName.equals("copyValueOf")
			//	don't include if concat is precise
			//	|| (fName.equals("append")&&sourceMap.size()==2)||fName.equals("concat")
				))
			store.setTaint(id, true);
	}
	
	@Override
	public boolean caresAboutHeight() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Used to propagate potential values affected by timeouts.
	 * @param val id The constraint to be considered
	 * @param sourceMap The potentially tained sources.
	 */
	protected void propoagateToTaints(int val, Collection<Integer> sourceMap){
		Iterator<Integer> it=sourceMap.iterator();
		while(it.hasNext()){
			int i=it.next();
			if(toTaints.contains(i)){
				toTaints.add(val);
				return;
			}
		}
	}
	/**
	 * Used to propagate create taint for values affected by timeouts.
	 * @param val id The current constraint.
	 * @param sourceMap The newly tainted sources.
	 */
	protected void newToTaint(int id, HashMap<String, Integer> sourceMap){
		Iterator<Integer> it=sourceMap.values().iterator();
		while(it.hasNext()){
			int i=it.next();
			toTaints.add(i);
		}
		toTaints.add(id);
	}
}
