/**
 * Not used in the end analysis, but tests input values generated by a solver by creating a program based on the PC
 * and running it.
 */

package old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class StringCode implements Solver{
	private HashMap<Integer, ArrayList<String>>codeMap;
	private HashMap<Integer, String> actualVals;
	private HashMap<Integer, String> stringVals;
	private HashMap<Integer,  Integer> numOpenings;
	private LinkedList<String>notNeeded;
	private LinkedList<String>needed;
	private HashSet<String> leftOver;
	
	StringCode(){
		codeMap=new HashMap<Integer, ArrayList<String>>();
		actualVals=new HashMap<Integer, String>();
		stringVals=new HashMap<Integer, String>();
		numOpenings=new HashMap<Integer, Integer>();
		notNeeded=new LinkedList<String>();
		leftOver=new HashSet<String>();
		needed=new LinkedList<String>();
	}

	@Override
	public void addRoot(String value, String actualValue, int id) {
		if (value.startsWith("r")||value.startsWith("$r")){
			leftOver.add("s"+id);
		}
		else if(value.startsWith("\"")&&!actualValue.startsWith("\"")){
			actualValue=sanitizeInput(actualValue);
			stringVals.put(id, actualValue);
		}
		else{
			if(actualValue.length()==1 && (actualValue.charAt(0)<'0'||actualValue.charAt(0)>'9')){
				actualVals.put(id, "'"+actualValue+"'");
			}
			else
				actualVals.put(id, actualValue);
		}
	}

	@Override
	public void addOperation(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		notNeeded.add("s"+id);
		addNeeded(sourceMap);
		String fName = string.split("!!")[0];
		ArrayList<String> operations=getOperation(sourceMap);
		StringBuilder operation=new StringBuilder();
		updateNumOpenings(sourceMap, id, false);
		boolean isString=false;
		boolean isStatic=false;
		if(fName.equals("length")){
			fName="toString";
			actualVals.put(id,  actualValue);
		}
			
		if(fName.equals("concat")||fName.equals("<init>")||fName.equals("copyValueOf")||fName.equals("format")
				||fName.equals("intern")||fName.equals("replace")||fName.equals("replaceAll")
				||fName.equals("replaceFirst")|| fName.equals("split")||fName.equals("subSequence")
				||fName.equals("substring")||fName.equals("toLowerCase")||fName.equals("toString")
				||fName.equals("toUpperCase")||fName.equals("trim")||fName.equals("valueOf")){
			isString=true;
			operation.append("String");
			if(fName.equals("copyValueOf")||fName.equals("valueOf")||fName.equals("format")){
				isStatic=true;
				int temp=sourceMap.get("t");
				for(int i=1; i<sourceMap.size();i++){
					int newTemp=sourceMap.get("s"+i);
					sourceMap.put("s"+i, temp);
					temp=newTemp;
				}
				sourceMap.put("s"+sourceMap.size(), temp);
			}
		}
		else if(fName.equals("charAt")){
			operation.append("Character");
			String value=actualValue;
			value.replace("\n", "\\n");
			actualVals.put(id,  "\""+value+"\"");
		}
		else if(fName.contains("indexOf")){
			isString=true;
			operation.append("int");
			actualVals.put(id,  actualValue);
		}
		else if(fName.equals("length")){
			operation.append("int");
		}
		else if(fName.equals("split")){
			operation.append("String[]");
		}
		else{
			operation.append("StringBuilder");
		}
		operation.append(" s"+id+"=");
		
		if(isStatic)
			operation.append("String");
		else if(isString){
			if(stringVals.containsKey(sourceMap.get("t")))
				operation.append("\""+stringVals.get(sourceMap.get("t"))+"\"");
			else
				operation.append("s"+sourceMap.get("t"));
		}
		else{
			if(stringVals.containsKey(sourceMap.get("t")))
				operation.append("(new StringBuilder(\""+stringVals.get(sourceMap.get("t"))+"\"))");
			else
				operation.append("(new StringBuilder(s"+sourceMap.get("t")+"))");
		}

		if(fName.equals("<init>")||fName.equals("setLength"))
			operation.append(";\n");
		else{
			operation.append("."+fName+"(");
				for(int i=1; i<sourceMap.size(); i++){
					if(actualVals.containsKey(sourceMap.get("s"+i))){
						operation.append(actualVals.get(sourceMap.get("s"+i)));
					}
					else if(stringVals.containsKey(sourceMap.get("s"+i))){
						String val=stringVals.get(sourceMap.get("s"+i));
	//					if(fName.equals("replaceAll")||fName.equals("replaceFirst"))
	//						val=val.replace("\\", "\\\\");
						
						operation.append("\""+val+"\"");
					}
					else{
						operation.append("s"+sourceMap.get("s"+i));
					}
					operation.append(",");
				}
				if(sourceMap.size()>1)
					operation.deleteCharAt(operation.length()-1);
			operation.append(");\n");
		}
		if(fName.equals("setLength"))
			operation.append(" s"+id+".setLength("+actualVals.get(sourceMap.get("s"+1))+");\n");

		operations.add(operation.toString());
		codeMap.put(id, operations);
	}
	
	/**
	 * Tracks the number of opening parentheses in the constraint.
	 * @param sourceMap Sources of this constraint.
	 * @param id The id of this constraint.
	 * @param isPred True if a predicate method is encountered.
	 */
	private void updateNumOpenings(HashMap<String, Integer> sourceMap, int id, boolean isPred) {
		int newNum=0;
		if(numOpenings.containsKey(sourceMap.get("t"))){
			newNum+=numOpenings.get(sourceMap.get("t"));
		}
		for(int i=1; i<sourceMap.size(); i++){
			if(numOpenings.containsKey(sourceMap.get("s"+i))){
				newNum+=numOpenings.get(sourceMap.get("s"+i));
			}
		}
		if(isPred){
			newNum++;
			numOpenings.put(sourceMap.get("t"), newNum);
		}
		numOpenings.put(id, newNum);
	}

	/**
	 * Returns a string of the operations encountered so far.
	 * @param sourceMap The sources that contains the operations.
	 * @return The arraylist of all merged operations.
	 */
	private ArrayList<String> getOperation(HashMap<String, Integer> sourceMap){
		ArrayList<String> operation=new ArrayList<String>();
		if(codeMap.containsKey(sourceMap.get("t"))){
			operation=mergeLists(operation,codeMap.get(sourceMap.get("t")));
		}
		for(int i=1; i<sourceMap.size(); i++){
			if(codeMap.containsKey(sourceMap.get("s"+i))){
				operation=mergeLists(operation,codeMap.get(sourceMap.get("s"+i)));
			}
		}
		return operation;
	}

	@Override
	public void addEnd(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		addNeeded(sourceMap);
		String fName = string.split("!!")[0];
		ArrayList<String> operations=getOperation(sourceMap);
		StringBuilder operation=new StringBuilder();
		updateNumOpenings(sourceMap, id, true);

		boolean result=true;
		if(actualValue.equals("false"))
			result=false;
		
		if(!SatSolver.containsBoolFunction(fName))
			return;
		
		operation.append("if(");
		if(!result)
			operation.append("!(");
		if(stringVals.containsKey(sourceMap.get("t")))
			operation.append("\""+stringVals.get(sourceMap.get("t"))+"\"");
		else
			operation.append("s"+sourceMap.get("t"));
		operation.append("."+fName+"(");
		for(int i=1; i<sourceMap.size(); i++){
			if(actualVals.containsKey(sourceMap.get("s"+i))){
				operation.append(actualVals.get(sourceMap.get("s"+i)));
			}
			else if(stringVals.containsKey(sourceMap.get("s"+i))){
				operation.append("\""+stringVals.get(sourceMap.get("s"+i))+"\"");
			}
			else{
				operation.append("s"+sourceMap.get("s"+i));
			}
			operation.append(",");
		}
		if(sourceMap.size()>1)
			operation.deleteCharAt(operation.length()-1);
		operation.append(")");
		if(!result)
			operation.append(")");
		operation.append("){\n");
		operations.add(operation.toString());
		codeMap.put(id, operations);
		codeMap.put(sourceMap.get("t"), operations);
	}
	
	/**Creates the file program to be ran.
	 * 
	 * @param stringAssignments The values gathered by the solver.
	 * @param id The id of this point
	 * @return A stringbuiler of the code.
	 */
	private StringBuilder finilizeOperation(HashMap<String,String> stringAssignments, int id){
		if(!codeMap.containsKey(id))
			return null;
		StringBuilder intro=new StringBuilder("public class StringPath{\n"
				+"\tpublic static void main(String[]args){\n");
		HashSet<String>leftOverTemp=new HashSet<String>(leftOver);
		Iterator<String> it=notNeeded.iterator();
		while(it.hasNext()){
			String next=it.next();
			stringAssignments.remove(next);
			leftOverTemp.remove(next);
		}
		Iterator<Entry<String,String>>entryIt=stringAssignments.entrySet().iterator();
		while(entryIt.hasNext()){
			Entry e=entryIt.next();
//			if(builderSet.contains(e.getKey()))
//				intro.append("StringBuilder "+e.getKey()+"=new StringBuilder(\""+e.getValue()+"\");");
//			else
			leftOverTemp.remove(e.getKey());
			String value=e.getValue().toString();
			value.replace('-', '_');
			value=sanitizeInput(value);
				intro.append("String "+e.getKey()+"=\""+value+"\";");
		}
		it=leftOverTemp.iterator();
		while(it.hasNext()){
			String next=it.next();
			if(needed.contains(next))
				intro.append("String "+next+"=\"\";\n");
		}
		
		StringBuilder operation=new StringBuilder(listToString(codeMap.get(id)));
		operation=intro.append(operation);
		operation.append("System.err.println(\"Path Reached!\");\n");
		int num=numOpenings.get(id);
		for(int i=0; i<num; i++)
			operation.append("}\n");
		operation.append("\n}}\n");
		return operation;
	}
	
	/**
	 * Executes a program describing the PC.
	 * @param assignments Assignments gathered from the solver.
	 * @param id The id of this point.
	 * @return true if the correct path was reached.
	 */
	public boolean exec(HashMap<String,String> assignments, int id){
	      FileWriter fileWriter = null;
	        File textFile = new File("StringPath.java");
	        StringBuilder operation=finilizeOperation(assignments, id);
	        	long startTime=0;
	         Runtime   rt = Runtime.getRuntime();
	         Process p=null;
		        try {  
		             textFile.delete();
		              fileWriter = new FileWriter(textFile);
		              fileWriter.write(operation.toString());
		              fileWriter.close();
				    	  
			  		 startTime = System.nanoTime();
			  		 
			    	  String []args=new String[2];
			    	  args[0]="javac";
			    	  args[1]=textFile.getAbsolutePath();
			         p = rt.exec(args);
			         InputStream stderr = p.getErrorStream();
			            InputStreamReader isr = new InputStreamReader(stderr);
			            BufferedReader br = new BufferedReader(isr);
			            String line = null;
			            boolean exit=false;
			            while ( (line = br.readLine()) != null){
			                //System.err.println(line);
			                exit=true;
			            }
			            if(exit){
			            	//System.err.println(operation);
			            	//System.err.println("\n////////////////////////////////////////////////////////\n////////////////////////////////////////////");
			            }
//			            if(exit)
//			            	System.exit(0);
			         p.waitFor();

			         args=new String[2];
			    	  args[0]="java";
		              args[1]=("StringPath");
		              
				         p = rt.exec(args);
				         stderr = p.getErrorStream();
				         isr = new InputStreamReader(stderr);
				         br = new BufferedReader(isr);
				         line = null;
				         
				            while ( (line = br.readLine()) != null){
				            	if(line.equals("Path Reached!"))
				            		return true;
//				                System.err.println(line);
				            }
				         p.waitFor();
				        }
		       catch (Exception e) {
		 		    e.printStackTrace();
			   }


//			         if (textFile.delete() == false) 
//				            System.err.println("Warning: " + textFile.getAbsolutePath() + " could not be deleted!");
			            
			            return false;
	}

	@Override
	public void getStats() {
		// TODO Auto-generated method stub
	}

	@Override
	public void remove(int id) {
		codeMap.remove(id);
		actualVals.remove(id);
		stringVals.remove(id);
		numOpenings.remove(id);
		notNeeded.remove("s"+id);
		needed.remove("s"+id);
	}

	@Override
	public void finishUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToFile() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Merges 2 lists of string constraints into 1. Useful for any operation/branching point with multiple values.
	 * @param l1 First list to be merged.
	 * @param l2 Second list to be merged.
	 * @return Two lists merged into one.
	 */
	protected ArrayList<String> mergeLists(ArrayList<String> l1, ArrayList<String> l2){
		ArrayList<String> temp1=new ArrayList<String>(l1);
		ArrayList<String> temp2=new ArrayList<String>(l2);
		for(int i=0; i<temp1.size(); i++){
			for(int j=temp2.size()-1; j>=0; j--){
				if(temp1.get(i).equals(temp2.get(j)))
					temp2.remove(j);
			}
		}
		temp1.addAll(temp2);
		return temp1;
	}
	/**
	 * Converts an arraylist of ECLiPSe-str constraints to a string used by ECLiPSe-str.
	 * @param solveString The arraylist to be used
	 * @return ECLiPSe-str input.
	 */
	protected String listToString(ArrayList<String> list){
		StringBuilder result=new StringBuilder();
		for(int i=0; i<list.size(); i++){
			result.append(list.get(i));
		}
		return result.toString();
	}
	
	/**
	 * Replaces escape characters in a string.
	 * @param actualValue The string that may need the escape characters.
	 * @return The replaced string.
	 */
	private String sanitizeInput(String actualValue){
		actualValue=actualValue.replace("\\", "\\\\");
		actualValue=actualValue.replaceAll("\\\\b", "\b");
		actualValue=actualValue.replaceAll("\\\\t", "\t");
//		actualValue=actualValue.replaceAll("\\\\n", "\n");
		actualValue=actualValue.replaceAll("\\\\f", "\f");
		actualValue=actualValue.replaceAll("\\\\r", "\r");
		actualValue=actualValue.replace("\"", "\\\"");
		actualValue=actualValue.replaceAll("\\\\\'", "\'");
		return actualValue;
	}
	
	/**
	 * Adds values that are needed.
	 * @param sourceMap
	 */
	private void addNeeded(HashMap<String,Integer>sourceMap){
		needed.add("s"+sourceMap.get("t"));
		for(int i=1; i<sourceMap.size(); i++){
			needed.add("s"+sourceMap.get("s"+i));
		}
	}

	@Override
	public void addHeight(int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean caresAboutHeight() {
		// TODO Auto-generated method stub
		return false;
	}
}