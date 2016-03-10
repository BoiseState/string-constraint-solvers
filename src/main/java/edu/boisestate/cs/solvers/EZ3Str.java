package edu.boisestate.cs.solvers;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;


public class EZ3Str extends ExtendedSolver<StringBuilder>{
	
	private Set<Integer> concreteVals = new HashSet<Integer>();
	private Runtime runtime;
	private BufferedReader in=null;
	private ExecutorService executor;
	
	private String killString;
	private String path;
	private long timeout;
	private String tempFile;
	
	public EZ3Str(long timeout, String path, String killString, String tempFile) {
		super();
		runtime = Runtime.getRuntime();
        executor = Executors.newFixedThreadPool(2);
        
        this.killString = killString;
        this.path = path;
        this.timeout = timeout;
        this.tempFile = tempFile;
	}
	
	@Override
	public StringBuilder getValue(int id) {
		return new StringBuilder(symbolicStringMap.get(id));
	}
	
	@Override
	public void newSymbolicString(int id) {
		symbolicStringMap.put(id, new StringBuilder("(declare-variable s" + id + " String)\n"));
	}

	@Override
	public void newConcreteString(int id, String string) {
		//Z3-str requires "" around each string;
		string="\""+string+"\"";

		StringBuilder result = new StringBuilder("(declare-variable s" + id + " String)\n");
		result.append("(assert (= s"+id+" " + string + " ))\n");
		symbolicStringMap.put(id, result);
		concreteVals.add(id);
	}

	@Override
	public String replaceEscapes(String value) {
		if(value==null){
			return "\"\\\\0\"";
		}
		if(value.equals("\"\"")) {
			System.err.println("Error: unsupported string using best guess: " + value);
			return "";
		}
		value=value.replace('\\'+""+'n', ""+'\n');
		value=value.replace('\\'+""+'t', ""+'\t');
		value=value.replace('\\'+""+'r', ""+'\r');
		value=value.replace('\\'+""+'f', ""+'\f');
		value=value.replace('\\'+""+'b', ""+'\b');
		value=value.replace('\\'+""+'0', ""+'\0');
		
		StringBuilder newValue=new StringBuilder();
		
		//Because only certain charactors are supported we map unsupported values to supported values.
		for(int i=0; i<value.length(); i++){
			char c=value.charAt(i);
			if((int)c<128 && (int)c>31){
				newValue.append(c);
			}
			else{
				newValue.append((char)((int)c%96 +32));
			}
		}
		value=newValue.toString();
		value=value.replaceAll("\\\\\'", "'");
		value=value.replace("\\\"", "\"");
		value=value.replaceAll("\\\\", "\\\\\\\\");
		value=value.replace("\"", "\\\"");
		return value;
	}

	@Override
	public void propagateSymbolicString(int id, int base) {
		StringBuilder result = getValue(base);
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= s"+id+" s" + base + " ))\n");
		symbolicStringMap.put(id, result);
	}

	@Override
	public void append(int id, int base, int arg, int start, int end) {
		String temp = "temp_" + id;
		StringBuilder result = getValue(base).append(getValue(arg));
		
		result.append("(declare-variable in0_" + id + " String)\n");
		result.append("(assert (= " + temp + " (Substring s" + arg + " " + start + " " + end + ")))\n");		
	
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= s"+id+" (Concat s" + base + " " + temp + ")))\n");
		symbolicStringMap.put(id, result);
	}

	@Override
	public void append(int id, int base, int arg) {
		StringBuilder result = getValue(base).append(getValue(arg));
			
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= s"+id+" (Concat s" + base + " s" + arg + ")))\n");
		symbolicStringMap.put(id, result);		
	}

	@Override
	public void substring(int id, int base, int start) {
		StringBuilder result = getValue(base);
		
		if(start==0){
			result.append("(declare-variable s" + id + " String)\n");
			result.append("(assert (= s"+id+" s" + base + " ))\n");
		}
		else {
			String symbolicLength = "i" + id;
			result.append("(declare-variable i" + id + " Int)\n");
			result.append("(assert (= i"+id+" (- (Length s" + base + ") " + symbolicLength + ")))\n");
			result.append("(declare-variable s"+id+" String)\n");
			result.append("(assert (= s"+id+" (Substring s" + base + " " + start + " " + symbolicLength + ")))\n");
		}
		symbolicStringMap.put(id, result);
	}

	@Override
	public void substring(int id, int base, int start, int end) {
		StringBuilder result = getValue(base);
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= s"+id+" (Substring s" + base + " " + start + " " + end + ")))\n");
		symbolicStringMap.put(id, result);
	}

	@Override
	public void setLength(int id, int base, int length) {
		StringBuilder result = getValue(base);
		result.append("(declare-variable temp1_"+id+" String)\n");
		result.append("(declare-variable temp2_"+id+" String)\n");

		result.append("(assert (= temp1_"+id+" (Concat s" + base + " temp2_" + id + ")))");
		
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(declare-variable i"+id+" Int)\n");
		
		//If the string length is already less then the length, it doesn't change
		result.append("(assert (> "+(length+1)+" i"+id+"))");
		result.append("(assert (= s"+id+" (Substring temp1_"+id+" 0 i"+id+")))");
		symbolicStringMap.put(id, result);
	}

	@Override
	public void insert(int id, int base, int arg, int offset) {
		StringBuilder result = getValue(base).append(getValue(arg));
		result.append("(declare-variable temp1_"+id+" String)\n");
		result.append("(declare-variable temp2_"+id+" String)\n");

		if(offset==0)
			result.append("(assert (= temp1_"+id+" \"\"))\n");
		else
			result.append("(assert (= temp1_"+id+" (Substring s"+base+" 0 "+offset+")))\n");
		
		result.append("(declare-variable i"+id+" Int)\n");
		result.append("(assert (= i"+id+" (- (Length s"+base+") "+(offset+1)+")))\n");
		result.append("(assert (= temp2_"+id+" (Substring s"+base+" "+(offset+1)+" i"+id+")))\n");
		
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= (Concat temp1_"+id+" (Concat s"+arg+" temp2_"+id+")) s"+id+"))\n");
		symbolicStringMap.put(id, result);
	}

	@Override
	public void insert(int id, int base, int arg, int offset, int start, int end) {
		StringBuilder result = getValue(base).append(getValue(arg));

		result.append("(declare-variable temp0_"+id+" String)\n");
		result.append("(assert (= temp0_"+id+" (Substring s"+base+" "+start+" "+end+")))\n");
		
		result.append("(declare-variable temp1_"+id+" String)\n");
		result.append("(declare-variable temp2_"+id+" String)\n");

		if(offset==0)
			result.append("(assert (= temp1_"+id+" \"\"))\n");
		else
			result.append("(assert (= temp1_"+id+" (Substring s"+base+" 0 "+offset+")))\n");
		
		result.append("(declare-variable i"+id+" Int)\n");
		result.append("(assert (= i"+id+" (- (Length s"+base+") "+(offset+1)+")))\n");
		result.append("(assert (= temp2_"+id+" (Substring s"+base+" "+(offset+1)+" i"+id+")))\n");
		
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= (Concat temp1_"+id+" (Concat temp0_"+id+" temp2_"+id+")) s"+id+"))\n");
		symbolicStringMap.put(id, result);		
	}

	@Override
	public void setCharAt(int id, int base, int arg, int offset) {
		StringBuilder result = getValue(base).append(getValue(arg));

		result.append("(declare-variable temp1_"+id+" String)\n");
		result.append("(declare-variable temp2_"+id+" String)\n");

		result.append("(declare-variable i"+id+" Int)\n");

		result.append("(assert (= i"+id+" (- (Length s"+base+") "+(offset+1)+")))\n");
		if(offset==0){
			result.append("(assert (= temp1_"+id+" \"\"))\n");
		}
		else{
			result.append("(assert (= temp1_"+id+" (Substring s"+base+" 0 "+offset+")))\n");
		}

		result.append("(assert (= temp2_"+id+" (Substring s"+base+" "+(offset+1)+" i"+id+")))\n");
		
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= (Concat temp1_"+id+" (Concat s"+arg+" temp2_"+id+")) s"+id+"))\n");
		symbolicStringMap.put(id, result);		
	}

	@Override
	public void trim(int id, int base) {
		StringBuilder result = getValue(base);

		result.append("(declare-variable s"+id+" String)\n");
		result.append("(declare-variable i1_"+id+" Int)\n");
		result.append("(declare-variable i2_"+id+" Int)\n");
		result.append("(assert (or (= s"+base+" s"+id+" ) (= s"+id+" (Substring s"+base+" i1_"+id+" i2_"+id+"))))\n");		
		symbolicStringMap.put(id, result);		
	}

	@Override
	public void delete(int id, int base, int start, int end) {
		StringBuilder result = getValue(base);

		if(start>=end) {
			result.append("(declare-variable s"+id+" String)\n");
			result.append("(assert (= s"+base+" s"+id+"))\n");
		}
		else{
			result.append("(declare-variable temp1_"+id+" String)\n");
			result.append("(declare-variable temp2_"+id+" String)\n");

			if(start==0) {
				result.append("(assert (= temp1_"+id+" \"\"))\n");
			}
			result.append("(assert (= temp1_"+id+" (Substring s"+base+" 0 "+start+")))\n");

			result.append("(declare-variable s"+id+" String)\n");
			result.append("(assert (= (Concat temp1_"+id+" temp2_"+id+") s"+id+"))\n");
		}
		symbolicStringMap.put(id, result);
	}

	@Override
	public void deleteCharAt(int id, int base, int loc) {
		StringBuilder result = getValue(base);
		result.append("(declare-variable temp1_"+id+" String)\n");
		result.append("(declare-variable temp2_"+id+" String)\n");

		result.append("(declare-variable i"+id+" Int)\n");
		result.append("(assert (= i"+id+" (- (Length s"+base+") "+(loc+1)+")))\n");
		
		result.append("(assert (= temp1_"+id+" (Substring s"+base+" 0 "+loc+")))\n");
		result.append("(assert (= temp2_"+id+" (Substring s"+base+" "+(loc+1)+" i"+id+")))\n");
		
		result.append("(declare-variable s"+id+" String)\n");
		result.append("(assert (= (Concat temp1_"+id+" temp2_"+id+") s"+id+"))\n");		
		symbolicStringMap.put(id, result);
	}

	@Override
	public void reverse(int id, int base) {
		newSymbolicString(id);		
	}

	@Override
	public void toUpperCase(int id, int base) {
		newSymbolicString(id);
	}

	@Override
	public void toLowerCase(int id, int base) {
		newSymbolicString(id);
	}

	@Override
	public void replace(int id, int base, int argOne, int argTwo) {
		newSymbolicString(id);
	}

	@Override
	public void contains(boolean result, int base, int arg) {
		StringBuilder builder = getValue(base).append(getValue(arg));
		
		if(result){
			builder.append("(assert (Contains s"+base+" s"+arg+" ))\n");
		}
		else{
			builder.append("(assert (not (Contains s"+base+" s"+arg+" )))\n");
		}
		symbolicStringMap.put(base, builder);
		symbolicStringMap.put(arg, builder);
	}

	@Override
	public void endsWith(boolean result, int base, int arg) {
		StringBuilder builder = getValue(base).append(getValue(arg));
		String id = base + "_" + arg;
		
		if(result){
			builder.append("(declare-variable s_ends_"+id+"_t String)\n");
			builder.append("(assert (= s"+base+" (Concat s_ends_"+id+"_t s"+arg+")))\n");
		}
		else{
			builder.append("(declare-variable i"+id+"_1 Int)\n");
			builder.append("(declare-variable i"+id+"_2 Int)\n");
			builder.append("(assert (not (= (Substring s"+base+" i"+id+"_1 i"+id+"_2 ) s"+arg+")))\n");
		}
		symbolicStringMap.put(base, builder);
		symbolicStringMap.put(arg, builder);
	}

	@Override
	public void startsWith(boolean result, int base, int arg) {
		StringBuilder builder = getValue(base).append(getValue(arg));
		String id = base + "_" + arg;

		if(result){
			builder.append("(declare-variable i"+id+"_1_t Int)\n");
			builder.append("(assert (= (Substring "+base+" 0 i"+id+"_1_t ) s"+arg+"))\n");
		}
		else{
			if(concreteVals.contains(arg)){
				builder.append("(declare-variable i"+id+"_1_f Int)\n");
				builder.append("(assert (not (= (Substring "+base+" 0 i"+id+"_1_f ) s"+arg+")))\n");
			}
			else{
				builder.append("(declare-variable i"+id+"_1_f Int)\n");
				builder.append("(assert (not (= (Substring "+base+" 0 i"+id+"_1_f ) s"+arg+")))\n");
			}
		}
		symbolicStringMap.put(base, builder);
		symbolicStringMap.put(arg, builder);
	}

	@Override
	public void equals(boolean result, int base, int arg) {
		StringBuilder builder = getValue(base).append(getValue(arg));
		
		if(result){
			builder.append("(assert (= s"+base+" s"+arg+" ))\n");
		}
		else{
			builder.append("(assert (not (= s"+base+" s"+arg+" )))\n");
		}
		symbolicStringMap.put(base, builder);
		symbolicStringMap.put(arg, builder);
	}

	@Override
	public void equalsIgnoreCase(boolean result, int base, int arg) {
	}

	@Override
	public void isEmpty(boolean result, int base) {
		StringBuilder builder = getValue(base);
		
		if(result){
			builder.append("(assert (= (Length s"+base+") 0))\n");
		}
		else{
			builder.append("(assert (> (Length s"+base+") 0))\n");
		}
		symbolicStringMap.put(base, builder);
	}

	@Override
	public String getSatisfiableResult(int id) {
		String execResult = exec(getValue(id).toString());
		return collectResult(execResult, id);
	}

	@Override
	public boolean isSatisfiable(int id) {
		String query = getValue(id).toString();
		String execResult = exec(query);
		return execResult == null || !execResult.contains(">> UNSAT");
	}

	@Override
	public boolean isSingleton(int id) {
		String result = getSatisfiableResult(id);
		if(result == null) {
			return false;
		}
		return isSingleton(id, result);
	}

	@Override
	public boolean isSingleton(int id, String actualValue) {
		String query = getValue(id).toString();
		query = query + "(assert (not (= s"+id+" "+actualValue+")))\n";
		
		String execResult = exec(query);
		return execResult != null && execResult.contains(">> UNSAT");
	}

	@Override
	public boolean isSound(int id, String actualValue) {
		String query = getValue(id).toString();
		query = query + "(assert (= s"+id+" " + actualValue + "))\n";
		
		String execResult = exec(query);
		return execResult == null || execResult.contains(">> SAT");
	}
	/**
	 * Executes a query for solvers that run in a stanalone program (Z3-str).
	 * @param solveString Contains the constraints to be evaluated in the language of the solver.
	 * @param trackConstraintTime True if this should go into evaluation of time at branching points.
	 * @param trackHotTime True if this should go into evaluation of time at hotspots.
	 * @param isTrueBranch True if this if for evaluation of a true branch.
	 * @param id An id for the constraint to be evaluated.
	 */
	protected String exec(String solveString) {
		solveString = solveString + "(get-model)\n";
		solveString = removeDuplicateLines(solveString);
				
		String extension = ".txt";
		
        FileWriter fileWriter = null;
        Process p = null;
        try{
	        File textFile = File.createTempFile(tempFile, extension);
	
	        textFile.delete();
	        fileWriter = new FileWriter(textFile);
	        fileWriter.write(solveString);
	        fileWriter.close();
	
	        String[] args = new String[2];
	        args[0]=path;
	        args[1]=textFile.getAbsolutePath();
	
	        p = runtime.exec(args);
        }
        catch(IOException e) {
        	System.err.println("Error reading a file\n" + e);
        	return null;
        }
        
		in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		         
		// Read data with timeout
		Callable<String> readTask = new Callable<String>() {
			@Override
			public String call() throws Exception {
				String next=in.readLine();
				return next;
			}
		};
		
		String line = null;
		String result = "";
		do {
			Future<String> future = executor.submit(readTask);

			try {
				line = future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException f) {
				f.printStackTrace();
			} catch (TimeoutException g) {
				try {
					runtime.exec("killall -9 "+killString);
				}
				catch(IOException e2)
				{
					System.err.println("Error on exec() method");
					e2.printStackTrace();  
				}
				line=null;
				result=null;
				break;
			}
			
			if (line !=null)
				result+= line+"\n";
		} while (line != null);
	 	return result;		
	}
	
	private String removeDuplicateLines(String solveString) {
		String[] lines = solveString.split("\\n");
		for(int i = 0; i<lines.length; i++) {
			for(int j = i+1; j<lines.length; j++) {
				if(lines[i] != null && lines[i].equals(lines[j])) {
					lines[j] = null;
				}
			}
		}
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i<lines.length; i++) {
			if(lines[i] != null) {
				result.append(lines[i]).append("\n");
			}
		}
		return result.toString();
	}

	/**
	 * Collects the value generated by the solver.
	 * @param id The id of the value to be collected.
	 * @return The String value that was generated.
	 */
	private String collectResult(String execResult, int id) {
		String name = "s" + id;
		
		if(execResult==null){
			return null;
		}
		if(!execResult.startsWith("************************")){
			System.err.println("Invalid Query:\n" + getValue(id) + "(get-model)\n");
			throw new IllegalArgumentException("Bad syntax point in query...exiting");
		}
		String[] lines=execResult.split("\n");
		String targetVal=null;
		for(int i=0; i<lines.length; i++){
			if(lines[i].startsWith(name+" ->")){
				String[] value=lines[i].split(" -> ");
				if(value.length>1)
					targetVal=value[1];
				else
					targetVal="";
				break;
			}
		}
		
		//The empty string is given in quotes.
		if(targetVal!=null && targetVal.equals("\"\""))
			targetVal="";
		return targetVal;
	}

	@Override
	public void shutDown() {
		executor.shutdown();
	}
}
