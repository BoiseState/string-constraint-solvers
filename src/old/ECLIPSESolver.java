/**
 * EECLiPSe-str extended string constraint solver.
 */
package old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.parctechnologies.eclipse.CompoundTerm;
import com.parctechnologies.eclipse.CompoundTermImpl;
import com.parctechnologies.eclipse.EclipseEngine;
import com.parctechnologies.eclipse.EclipseEngineOptions;
import com.parctechnologies.eclipse.EclipseException;
import com.parctechnologies.eclipse.EclipseTerminatedException;
import com.parctechnologies.eclipse.EmbeddedEclipse;
import com.parctechnologies.eclipse.OutOfProcessEclipse;

/*
 * Stemp->append
 * T->transformToList
 * CASE->change case
 */
//-Declipse.directory=/Users/scottkausler/Research/ECLiPSe/
//-Declipse.directory=/Users/skausler/ECLiPSe/

public class ECLIPSESolver extends SatSolver{
	private RunEclipse r;
	private Runtime rt;
	
	protected ArrayList<String> base;
	protected ArrayList<String> arg;
	protected int argNum;
	
	private int boolId;
	private boolean verbose;
	protected ECLIPSESolver(boolean verbose, String properties, String tempFile) throws Exception{
		super("ECLiPSe","", "",properties, tempFile);
	    
		rt=Runtime.getRuntime();
	    r=new RunEclipse();
		base=arg=null;
		argNum=-1;
		
		this.verbose=verbose;
		if(verbose){
			fileWrite+="BoolId\tName\tSAT\treg\tnegated\tResult\ttainted?\n";
//			System.err.println(fileWrite);
//			fileWrite="";
		}
		boolId=0;
		timeout=5000;
	}
	
	/**
	 * Transforms a string value to a representation recognized by ECLiPSe-str.
	 * @param value The value to be converted.
	 * @param id The id of the constraint.
	 * @return The converted value.
	 */
	protected String transformToList(String value, int id){
		//TODO: Decide what to do about null!
		if(value==null){
//			LinkedList<Integer> nullList=new LinkedList<Integer>();
//			nullList.add(0);
			return "[0]";
		}
		value=replaceExcapes(value);
//		value=value.replace('\\'+""+'n', ""+'\n');
//		value=value.replace('\\'+""+'t', ""+'\t');
//		value=value.replace('\\'+""+'r', ""+'\r');
//		value=value.replace('\\'+""+'f', ""+'\f');
//		value=value.replace('\\'+""+'b', ""+'\b');
//		value=value.replace('\\'+""+'0', ""+'\0');

//		value=value.replaceAll("\\\\n", "\\\\\\\\\\"+"n");
//		value=value.replaceAll("\\\\t", "\\\\\\\\\\"+"t");
		
		LinkedList<Integer> newValue=new LinkedList<Integer>();
		int j=0;
		for(int i=0; i<value.length(); i++){
			char c=value.charAt(i);
			newValue.add((int)value.charAt(i));

		}
		return newValue.toString();
	}
	
	@Override
	public void addRoot(String value, String actualValue, int id) {
		addNewPastList(value, actualValue, id);
//		if(problemId==id)
//		System.err.println("Root: "+value+" ID: "+id+" "+actualValue);
		String automaton;
		if(value.startsWith("\"")){
			automaton=actualValue;
		}
		else if (value.startsWith("r")||value.startsWith("$r")){
			automaton=null;
			symbolics.add(id);
		}
		else {
				//TODO deal with chars
			automaton=actualValue;
		}
		
		ArrayList<String> stringList=new ArrayList<String>();
		stringList.add("str_len(S"+id+",_)");
		if(!symbolics.contains(id))
			stringList.add(" S"+id+"="+transformToList(automaton, id));
		store.put(id, stringList);
		actualVals.put(id, actualValue);		
	}

	/**
	 * Depricated. Ensures the result is a character.
	 * @param id The id of the constraint.
	 * @param target
	 * @param uniqueid
	 * @return A string representing an ECLiPSe-str constant character.
	 */
	private String grabChar(int id, ArrayList<String>target, int uniqueid){
//		String val1=actualVals.get(id);
//		String key="C"+uniqueid+"_"+id;
//		target.add("str_len("+key+",1)");
//		try{
//			int tempVal=Integer.parseInt(val1);
//			if(!(tempVal<10 && tempVal >=0)){
//				target.add("["+tempVal+"]="+key);
//			}
//			else{
//				target.add("["+(int)val1.charAt(0)+"]="+key);
//				//target.add("(["+tempVal+"]="+key+"; ["+(int)val1.charAt(0)+"]="+key+")");
//			}
//		}
//		catch(NumberFormatException e){
//			target.add("["+(int)val1.charAt(0)+"]="+key);
//		}
//		return key;
		return transformToList(actualVals.get(id), id);
	}
	@Override
	public void addOperation(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
		appendPastList(string, actualVal, id, sourceMap, false);

//		if(problemId==id)
//		System.err.println("Operation: "+string+" ID: "+id+" "+actualVal);
		processTaint(string, id, sourceMap);

		String fName = string.split("!!")[0];
		actualVals.put(id, actualVal);
		
		numOperations++;
		ArrayList<String> target=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
		target.add("str_len(S"+id+",_)");
		
		if((fName.equals("append"))||fName.equals("concat")){
				String newVal;
				ArrayList<String> arg;
				 if(sourceMap.get("s1")==null){
						target.add("str_len(TEMP"+id+",_)");
						newVal="TEMP"+id;
						arg=new ArrayList<String>();
				 }else{
					 newVal="S"+sourceMap.get("s1");
					 arg=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1"))));
				 }
				//TODO: fix this
				if(sourceMap.size()>3){
					if(string.split("!!")[1].startsWith("[CI")){
						target.add("str_len(I0N"+id+",_)");
						newVal="I0N"+id;
					}
					else{
						int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")))+1;
						int argTwoInt=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
						target.add("str_len(I0N"+id+",_)");
						newVal="I0N"+id;
						
						if(argOne==argTwoInt)
							target.add(newVal+"=[]");
						else
							target.add("str_substr(S"+sourceMap.get("s1")+","+argOne+","+argTwoInt+", "+newVal+")");
					}
				}
				else if(string.split("!!")[1].equals("C")){
					String val=grabChar(sourceMap.get("s1"), target, 1);
					arg=new ArrayList<String>();
					arg.add(" S"+sourceMap.get("s1")+"="+val);
				}
				target=mergeLists(target,arg);

				target.add("append(S"+sourceMap.get("t")+","+newVal+",S"+id+")");
				store.put(id, target);
		}
		else if(fName.equals("<init>")){
			if(sourceMap.get("t")!=null && sourceMap.get("s1")!=null &&actualVals.get(sourceMap.get("t")).equals("")){
				int argInt=sourceMap.get("s1");
				target=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1"))));
			
				target.add(" S"+argInt+"= S"+id);
				store.put(id, target);
				numOperations--;
			}
			else
				makeStringSymbolic(id);
		}
		//TODO implement other copyValueOf
		else if(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||(fName.equals("copyValueOf")&&sourceMap.size()==2)){
			int argInt;
			if(sourceMap.containsKey("t")){
				argInt=sourceMap.get("t");
			}
			else{
				argInt=sourceMap.get("s1");
				target=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1"))));
			}
			target.add(" S"+argInt+"= S"+id);
			store.put(id, target);
			numOperations--;
		}
		else if(fName.equals("substring")){
			 String argTwo;
			int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")))+1;
			if(sourceMap.size()==2){
				target.add("str_len(S"+sourceMap.get("t")+",N"+sourceMap.get("t")+")");
				target.add("(S"+id+"=[];str_substr(S"+sourceMap.get("t")+","+
						argOne+",N"+sourceMap.get("t")+",S"+id+"))");

//				argTwo="_";
//				target.add("(S"+id+"=[] ; str_substr(S"+sourceMap.get("t")+","+argOne+","+argTwo+", S"+id+"))");
			}
			else{
				int argTwoInt=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
				argTwo=argTwoInt+"";
				if(argOne==argTwoInt)
					target.add("S"+id+"=[]");
				else
					target.add("str_substr(S"+sourceMap.get("t")+","+argOne+","+argTwo+", S"+id+")");
			}
			store.put(id, target);
		}
		else if(fName.equals("setLength")){
			int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")))+1;
			if(argOne==1){
				target.add("S"+id+"=[]");
			}
			else{
				target.add("str_len(S1L"+id+",_)");
				target.add("str_len(S2L"+id+",_)");
				target.add("N_"+id+"<"+argOne);

				target.add("append(S"+sourceMap.get("t")+",S2L"+id+",S1L"+id+")");
				target.add("(S"+id+"=[];str_substr(S1L"+id+",1,N_"+id+",S"+id+"))");
			}
			store.put(id, target);
		}
		//TODO implement other insert
		else if(fName.equals("insert")){
			String newVal;
			if(string.split("!!")[1].equals("IC")){
				newVal=grabChar(sourceMap.get("s2"), target, 1);
				
			}
			else if(string.split("!!")[1].startsWith("I[C")){
				target.add("str_len(I0N"+id+",_)");
				newVal="I0N"+id;
			}
			else if(sourceMap.size()>3){
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s3")))+1;
				int argTwoInt=Integer.parseInt(actualVals.get(sourceMap.get("s4")));
				target.add("str_len(I0N"+id+",_)");
				newVal="I0N"+id;
				
				if(argOne==argTwoInt)
					target.add(newVal+"=[]");
				else
					target.add("str_substr(S"+sourceMap.get("t")+","+argOne+","+argTwoInt+", "+newVal+")");
			}
			else{
				newVal=" S"+sourceMap.get("s2");
				target=mergeLists(target, new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s2")))));
			}

			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			target.add("str_len(I1N"+id+",_)");
			target.add("str_len(I2N"+id+",_)");
			target.add("str_len(I2N1_"+id+",_)");
			target.add("str_len(I2N2_"+id+",_)");

			target.add("str_len(I3N"+id+",_)");

			if(offset==0)
				target.add("I1N"+id+"="+"[]");
			else
				target.add("str_substr(S"+sourceMap.get("t")+",1,"+offset+",I1N"+id+")");
			
			target.add("append(S"+sourceMap.get("t")+",I2N1_"+id+", I2N2_"+id+")");
			target.add("str_len(S"+sourceMap.get("t")+",LEN"+id+")");
			target.add("I2N"+id+"=[];str_substr(I2N2_"+id+","+(offset+1)+",LEN"+id+",I2N"+id+")");
			
			target.add("append(I1N"+id+","+newVal+", I3N"+id+")");
			target.add("append(I3N"+id+","+"I2N"+id+",S"+id+")");
			store.put(id, target);
		}
		else if(fName.equals("setCharAt")){
			String newVal;
			newVal=grabChar(sourceMap.get("s2"), target, 1);
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			target.add("str_len(I1N"+id+",_)");
			target.add("str_len(I2N"+id+",_)");
			target.add("str_len(I2N1_"+id+",_)");
			target.add("str_len(I2N2_"+id+",_)");

			target.add("str_len(I3N"+id+",_)");

			if(offset==0)
				target.add("I1N"+id+"="+"[]");
			else
				target.add("str_substr(S"+sourceMap.get("t")+",1,"+(offset)+",I1N"+id+")");
			
			target.add("append(S"+sourceMap.get("t")+",I2N1_"+id+", I2N2_"+id+")");
			target.add("str_len(S"+sourceMap.get("t")+",LEN"+id+")");
			target.add("END"+id+"=LEN"+id+"-"+offset+2);
			target.add("(I2N"+id+"=[];str_substr(I2N2_"+id+","+(offset+1)+",LEN"+id+",I2N"+id+"))");
			
			target.add("append(I1N"+id+","+newVal+", I3N"+id+")");
			target.add("append(I3N"+id+","+"I2N"+id+",S"+id+")");
			store.put(id, target);
		}
		else if(fName.equals("trim")){
			
			target.add("(S"+id+"=[];str_substr(S"+sourceMap.get("t")+",_,_,S"+id+"))");
			store.put(id, target);

//			makeStringSymbolic(id);
//				String targetVal;
//				targetVal=getTargetString(sourceMap.get("t"));
//				target.add("str_len(S"+sourceMap.get("t")+",_)");
//				if(targetVal.contains("-1")){
//					target.add("str_substr(S"+sourceMap.get("t")+",_,_,S"+id+")");
//					store.put(id, target);
//					return;
//				}
//				targetVal=targetVal.replace(" ", "");
//				targetVal=targetVal.replace("[","");
//				targetVal=targetVal.replace("]", "");
//				if(targetVal.equals("")){
////					target=new ArrayList<String>();
////					LinkedList<String>l=new LinkedList<String>();
////					target.add(l+"=S"+id);
////					target.add("str_len(S"+id+",_)");
////					store.put(id, target);
//					//TODO: improve this
//					makeStringSymbolic(id);
//					return;
//				}
//				String [] vals=targetVal.split(",");
//				int start=1, end=vals.length;
//				for(int i=0; i<vals.length; i++){
//					int val=Integer.parseInt(vals[i]);
//					if(val==32)
//						start++;
//					else
//						break;
//				}
//				for(int i=vals.length-1; i>=0; i--){
//					int val=Integer.parseInt(vals[i]);
//					if(val==32)
//						end--;
//					else
//						break;
//				}
//				
//				if(!(start<end)){//(start==0 && end ==vals.length-1)||end==1){
//					target=new ArrayList<String>();
//					LinkedList<Integer>l=new LinkedList<Integer>();
//					target.add("str_len(S"+id+",_)");
//					target.add(l+"=S"+id);
//				}
//				else{
//					target.add("str_substr(S"+sourceMap.get("t")+",_,_,S"+id+")");
//					//target.add("str_substr(S"+sourceMap.get("t")+","+start+","+end+",S"+id+")");
//				}
//				store.put(id, target);
		}
		else if(fName.equals("delete")){
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			int offset2=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
			if(offset==offset2){
				target.add(" S"+sourceMap.get("t")+"= S"+id);
				store.put(id, target);
				return;
			}
			
			target.add("str_len(I1N"+id+",_)");
			target.add("str_len(I2N"+id+",_)");
			target.add("str_len(S"+sourceMap.get("t")+",N"+sourceMap.get("t")+")");
			if(offset==0)
				target.add("I1N"+id+"="+"[]");
			else
				target.add("str_substr(S"+sourceMap.get("t")+",1,"+(offset)+",I1N"+id+")");
			
			target.add("(I2N"+id+"=[];str_substr(S"+sourceMap.get("t")+","+
					(offset2+1)+",N"+sourceMap.get("t")+",I2N"+id+"))");
			target.add("append(I1N"+id+","+"I2N"+id+",S"+id+")");
			store.put(id, target);
		}
		else if(fName.equals("deleteCharAt")){
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			target.add("str_len(S"+sourceMap.get("t")+",N"+sourceMap.get("t")+")");
			target.add("NLEN"+id+"=N"+sourceMap.get("t")+"-"+offset+2);

			if(offset==0)
				target.add("I1N"+id+"="+"[]");
			else
				target.add("str_substr(S"+sourceMap.get("t")+",1,"+(offset)+",I1N"+id+")");
			
			target.add("(I2N"+id+"=[];str_substr(S"+sourceMap.get("t")+","+
					(offset+2)+",NLEN"+id+",I2N"+id+"))");
			target.add("append(I1N"+id+","+"I2N"+id+",S"+id+")");
			store.put(id, target);
		}
		else if(fName.equals("reverse")){
			//TODO: This may be possible to implement
			makeStringSymbolic(id);
		}
		else if(fName.equals("toUpperCase")&&sourceMap.size()==1){
			makeStringSymbolic(id);
				///store.put(id, changeCase(id, sourceMap, true, sourceMap.get("t"), " S"+id, "CASE"+id));
		}
		else if(fName.equals("toLowerCase")&&sourceMap.size()==1){
			makeStringSymbolic(id);
				///store.put(id, changeCase(id, sourceMap, false, sourceMap.get("t"), " S"+id, "CASE"+id));
		}
		else if(fName.startsWith("replace")){
			makeStringSymbolic(id);
//			if(fName.equals("replaceAll")||fName.equals("replaceFirst")||string.split("!!")[1].startsWith("II")){
//				makeStringSymbolic(id);
//				return;
//			}
//			if(sourceMap.size()==3){
//				ArrayList<String> solveString=new ArrayList<String>(target);
//
//				String oldVal, newVal;
//				if(string.split("!!")[1].equals("CC")){
//					oldVal=grabChar(sourceMap.get("s1"), solveString, 1);
//					newVal=grabChar(sourceMap.get("s2"), solveString, 1);
//				}
//				else{
//					ArrayList<String> a1=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
//					ArrayList<String> a2=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s2")));
//					solveString=mergeLists(solveString, a1);
//					solveString=mergeLists(solveString, a2);
//					
//					oldVal=" S"+sourceMap.get("s1");
//					newVal=" S"+sourceMap.get("s2");
//				}
//				solveString.add("str_len("+oldVal+",OLD"+id+")");
//				int oldLength=getNumericVal("OLD"+id, solveString);
//				if(oldLength==-1){
//					makeStringSymbolic(id);
//					return;
//				}
//				solveString.add("str_indexof(S"+sourceMap.get("t")+","+oldVal+",N0_"+id+")");
//				int i=getNumericVal("N0_"+id, solveString);
//				if(oldLength==-1){
//					makeStringSymbolic(id);
//					return;
//				}
//				solveString.add(" S"+0+"_"+id+"= S"+sourceMap.get("t"));
//				solveString.add("str_len(S"+0+"_"+id+",N0_N"+id+")");
//				int j=0;
//				int oldI=-1;
//				while(i!=0){
//					solveString.add("str_len(S"+j+"_1_"+id+",_)");
//					solveString.add("str_len(S"+j+"_2_"+id+",_)");
//					solveString.add("str_len(S"+j+"_3_"+id+",_)");
//					
//					if(i!=1)
//						solveString.add("str_substr(S"+j+"_"+id+",1,"+(i-1)+",S"+j+"_1_"+id+")");
//					else
//						solveString.add(" S"+j+"_1_"+id+"=[]");
//					int max=getNumericVal("N"+j+"_N"+id, solveString);
//					if(oldLength==-1){
//						makeStringSymbolic(id);
//						return;
//					}
//					if(i+oldLength<=max)
//						solveString.add("str_substr(S"+j+"_"+id+","+(i+oldLength)+",N"+j+"_N"+id+",S"+j+"_2_"+id+")");
//					else
//						solveString.add(" S"+j+"_2_"+id+"=[]");
//
//					solveString.add("append(S"+j+"_1_"+id+","+newVal+",S"+j+"_3_"+id+")");
//					solveString.add("append(S"+j+"_3_"+id+",S"+j+"_2_"+id+",S"+(j+1)+"_"+id+")");
//					
//					j++;
//					solveString.add("str_len(S"+j+"_"+id+",N"+j+"_N"+id+")");
//					solveString.add("str_indexof(S"+j+"_"+id+","+oldVal+",N"+j+"_N"+id+"_"+id+")");
//					oldI=i;
//					i=Integer.parseInt(getVal("N"+j+"_N"+id+"_"+id, solveString));
//					if(i==oldI){
//						makeStringSymbolic(id);
//						return;
//					}
//				}
//				solveString.add(" S"+j+"_"+id+"=S"+id);
//				solveString.add("str_indexof(S"+id+","+oldVal+",0)");
//				store.put(id, solveString);
//			}
//			else{
//				makeStringSymbolic(id);
//			}
		}
		else{
			makeStringSymbolic(id);
		}
	}
	private int getNumericVal(int id, String string, ArrayList<String> solveString) {
		try{
			return Integer.parseInt(getVal(id, string, solveString));
		}
		catch(NumberFormatException e){
			return -1;
		}
	}

	/**
	 * Not used. Originally attempted to model toLowerCase and toUpperCase, but it requires running ECLiPSe-str at
	 * an operation, which introduces complications.
	 * @param id
	 * @param sourceMap
	 * @param toUpperCase
	 * @param source
	 * @param target
	 * @param caseName
	 * @return
	 */
	protected ArrayList<String> changeCase(int id, HashMap<String, Integer> sourceMap, boolean toUpperCase, int source, String target, String caseName){
		int start, end;
		if(toUpperCase){
			start=(int)'a';
			end=(int)'z';
		}
		else{
			start=(int)'A';
			end=(int)'Z';
		}
		
		ArrayList<String>auto=new ArrayList<String>(((ArrayList<String>) store.get(source)));
		LinkedList<Integer> targetVal;
		
		targetVal = (LinkedList<Integer>)getList(id, " S"+source, auto);
		Iterator<Integer> it=targetVal.iterator();
		auto.add("str_len("+caseName+"_1_"+0+",_)");
		int i=1;
		while(it.hasNext()){
			int next=it.next();
			auto.add("str_len("+caseName+"_1_"+i+",_)");
			auto.add("str_len("+caseName+"_2_"+i+",1)");
			if(next>=start&&next<=end){
				int replacement;
				if(toUpperCase)
					replacement=(next-32);
				else
					replacement=(next+32);
				auto.add(caseName+"_2_"+i+"=["+replacement+"]");
			}
			else if(next != -1)
				auto.add(caseName+"_2_"+i+"=["+next+"]");
			auto.add("append("+caseName+"_1_"+(i-1)+","+caseName+"_2_"+i+","+caseName+"_1_"+i+")");
			i++;
		}
		auto.add("str_len(S"+id+",_)");
		auto.add(" S"+id+"="+caseName+"_1_"+i);
		return auto;
	}

	/**
	 * Collects the value generated by the solver.
	 * @param id The id of the value to be collected.
	 * @return The String value that was generated.
	 */
	protected String getTargetString(int id){
		ArrayList<String> base=(ArrayList<String>)store.get(id);
		return getVal(id, " S"+id, base);
	}
	protected Object getList(int id, String val, ArrayList<String>arg){
	ArrayList<String>base=new ArrayList(arg);
		
		base.add(0,val+"=_");

		try{
			CompoundTerm result=rpc(id, listToString(base), false, false, false);
			Object o=((CompoundTerm)result.arg(1)).arg(1);
			if(o instanceof List){
				if(o instanceof LinkedList)
					return (LinkedList<Integer>)o;
				else
					return new LinkedList<Integer>();
			}
			return o;
		}
		catch(TimeoutException e){
			return new LinkedList<Integer>();
		}
		catch(Exception e){
			System.err.println("point 1");
			e.printStackTrace();
			System.err.println(listToString(base));
			System.exit(1);
		}
		return null;
	}
	protected String getVal(int id, String val, ArrayList<String>arg){
		return getList(id, val, arg).toString();
	}
	
	/**
	 * Creates a new symbolic value. Happens when an operation that cannot be modeled is encountered.
	 * @param id
	 */
	private void makeStringSymbolic(int id){
		ArrayList<String> auto=new ArrayList<String>();
		auto.add(" S"+id+"=_");
		auto.add("str_len(S"+id+",_)");
		store.put(id, auto);
		symbolicOps.add(id);
	}

	@Override
	public void addEnd(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
//		if(problemId==id)
//		System.err.println("End: "+string+" ID: "+id+" "+actualValue);
		addTaints( id, sourceMap.values());
		String fName=string.split("!!")[0];
		if(SatSolver.containsBoolFunction(fName)){
			appendPastList(string, actualValue, id, sourceMap, true);
			constraintSatisfiability(string, actualValue, id, sourceMap);
			solveBooleanConstraint(fName, actualValue, id, sourceMap);
			assertBooleanConstraint(fName, actualValue, id, sourceMap);
			numConstraints++;
			if(isTrackedTimeout(id))
				newToTaint(id, sourceMap);
		}
		else if(AnalysisInfo.getEndMethods(properties).contains(fName)){
			int sourceId;
			if(store.get(sourceMap.get("t"))==null){
				sourceId=sourceMap.get("s1");
			}
			else
				sourceId=sourceMap.get("t");
			if(pastLists.containsKey(sourceId)&&pastLists.get(sourceId).size()>1){
				hotSpots++;

				//System.out.println(getConstraintValue(sourceId));
				//System.err.println(string+" "+actualValue);
				int sourceResult=testConstraint(id, sourceId, -1);
				if(sourceResult==-2){
					fileWrite+="ECLiPSeIncorrect end-"+id+"\nactualVal-"+actualValue+" "+"\n";
					incorrectHotspot.add(id);	
				}
				else if(sourceResult<1){
					unknownHotspot.add(id);
				}
				else
					correctHotspot.add(id);
			}
		}
		else if(containsIntFunction(string))
			store.setTaint(sourceMap.get("t"), true);
	}
	

	/**
	 * Assert a predicate on a symbolic value. Does not store the result.
	 * @param result Is it a true or false predicate.
	 * @param method The predicate method.
	 * @param actualValue The actual result.
	 * @param id The predicate id.
	 * @param sourceMap The values involved.
	 */
	protected void setConditionalLists(boolean result,String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap) {
		String fName = method.split("!!")[0];

		 base =new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("t")));
		 arg=null;
		
		 argNum=-1;
		if(sourceMap.get("s1")!=null){
			arg =new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
			argNum=sourceMap.get("s1");
		}

		if (fName.equals("contains")) {
			base=mergeLists(base, arg);
			//contained in

			if(result){
				base.add("N"+id+"#\\=0");
				base.add("str_indexof(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",N"+id+")");
			}
			else{
				base.add("NF"+id+"#=0");
				base.add("str_len(S"+sourceMap.get("t")+",NF"+id+"_1)");
				base.add("str_len(S"+sourceMap.get("s1")+",NF"+id+"_2)");
				base.add("(NF"+id+"_1#<NF"+id+"_2; str_indexof(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",NF"+id+"))");
			}
		} else if (fName.equals("endsWith")) {
			base=mergeLists(base, arg);
			
			if(result){
				base.add("N"+id+"#\\=0");
				base.add("str_indexof(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",N"+id+")");
				base.add("str_len(S"+sourceMap.get("t")+",N_1_"+id+")");
				base.add("str_len(S"+sourceMap.get("s1")+",N_2_"+id+")");
				base.add("N_3_"+id+" is N_1_"+id+" - N_2_"+id);
				base.add("str_indexof(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",N_3_"+id+")");
			}
			else{
				base.add("str_len(S"+sourceMap.get("t")+",NF_1_"+id+")");
				base.add("str_len(S"+sourceMap.get("s1")+",NF_2_"+id+")");
				base.add("NF_3_"+id+" is NF_1_"+id+" - NF_2_"+id);
				base.add("NF_3_"+id+" #\\= NF_4_"+id);
				base.add("(NF_1_"+id+"<NF_2_"+id);
				base.add("NF_4_"+id+"=0;str_indexof(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",NF_4_"+id+"))");

			}
		}
		else if(fName.equals("startsWith")) {
			base=mergeLists(base, arg);
			
			String argument;
			if(sourceMap.size()==2){
				argument="S"+sourceMap.get("t");
			}
			else{
				argument="T"+id;
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")))+1;
				base.add("str_len(S"+sourceMap.get("t")+",N"+sourceMap.get("t")+")");
				base.add("("+argument+"=[];str_substr(S"+sourceMap.get("t")+","+
						argOne+",N"+sourceMap.get("t")+","+argument+"))");
			}
			
			if(result){
				base.add("str_len("+argument+",N_1_"+id+")");
				base.add("str_len(S"+sourceMap.get("s1")+",N_2_"+id+")");
				base.add("N"+id+" #= 1");
				base.add("str_indexof("+argument+", S"+sourceMap.get("s1")+",N"+id+")");
				
			}
			else{
				base.add("str_len("+argument+",NF_1_"+id+")");
				base.add("str_len(S"+sourceMap.get("s1")+",NF_2_"+id+")");
				base.add("NF"+id+" #\\= 1");
				base.add("(NF_1_"+id+"<NF_2_"+id+",NF"+id+"=0;str_indexof("+argument+", S"+sourceMap.get("s1")+",NF"+id+"))");
				
			}
		}
		
		
		else if(fName.equals("equals") || fName.equals("contentEquals")){
			base=mergeLists(base, arg);
			if(result){
				base.add(" S"+sourceMap.get("t")+"= S"+sourceMap.get("s1"));
			}
			else{
				base.add("str_eq(S"+sourceMap.get("t")+", S"+sourceMap.get("s1")+",0)");
			}
		} else if (fName.equals("equalsIgnoreCase")) {
			base=arg=null;
//			if(result){
//				try {
//					base=changeCase(id, sourceMap, true, sourceMap.get("t"), " S"+id+"_1", "CASE"+id+"_1_");
//					arg=changeCase(id, sourceMap, true, sourceMap.get("s1"), " S"+id+"_2", "CASE"+id+"_2_" );
//				} catch (Exception e) {
//					return;
//				}
//				base=mergeLists(base, arg);
//				base.add(" S"+id+"_1 = S"+id+"_2");
//			}
//			else{
//				try {
//					base=changeCase(id, sourceMap, true, sourceMap.get("t"), " SF"+id+"_1", "CASEF"+id+"_1_");
//					arg=changeCase(id, sourceMap, true, sourceMap.get("s1"), " SF"+id+"_2", "CASEF"+id+"_2_" );
//				} catch (Exception e) {
//					return;
//				}
//				base=mergeLists(base, arg);
//				base.add("str_eq( SF"+id+"_1, SF"+id+"_2,0)");
//			}
		}
		else if(fName.equals("isEmpty")){
			if(result){
				base.add("str_len(S"+sourceMap.get("t")+", 0)");
			}
			else{
				base.add("N"+id+"#\\=0");
				base.add("str_len(S"+sourceMap.get("t")+", N"+id+")");
			}
		}				
		else if(fName.equals("matches")){
			base=arg=null;
		}
		else if(fName.equals("regionMatches")){
			base=arg=null;
			if(sourceMap.size()==5){
				argNum=sourceMap.get("s2");
			}
			else{
				argNum=sourceMap.get("s3");
			}
		}
	}
	
	/**
	 * Actually makes a predicate assertion.
	 * @param method The predicate method.
	 * @param actualValue The actual value.
	 * @param id The id.
	 * @param sourceMap Values involved.
	 */
	private void assertBooleanConstraint(String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap) {
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("warning constraint detected without true/false value");
			return;
		}
		boolean result=true;
		if(actualValue.equals("false"))
			result=false;
		setConditionalLists(result, method, actualValue, id, sourceMap);
		if(base!=null){
			store.put(sourceMap.get("t"), base);
			store.put(id,base);
		}
		if(arg!=null)
			store.put(argNum, base);
		}		

	/**Collects results for a branching point.
	 * 
	 * @param method The method encountered.
	 * @param actualValue the actual value.
	 * @param id Id of the branching point.
	 * @param sourceMap Values involved.
	 */
	protected void solveBooleanConstraint(String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap) {
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("warning constraint detected without true/false value");
			return;
		}
		String fName=method.split("!!")[0];
		if(!SatSolver.containsBoolFunction(fName))
			return;
		int argInt=-1;
		if(fName.equals("regionMatches")){
			if(sourceMap.size()==5){
				argInt=sourceMap.get("s2");
			}
			else{
				argInt=sourceMap.get("s3");
			}
		}else{
			if(sourceMap.containsKey("s1")){
				argInt=sourceMap.get("s1");
			}
		}
		int sourceResult=testConstraint(id, sourceMap.get("t"), argInt);
		if(sourceResult==-2){
			fileWrite+="ECLiPSeIncorrect: "+method+" ";
			fileWrite+=actualVals.get(sourceMap.get("t"))+" "+actualVals.get(argInt)+" "+"\n";
			incorrectConstraint.add(id);	
		}
		else if(sourceResult==-1){
			toSingleton.add(id);
		}
		else if(sourceResult<1)
			unknownConstraint.add(id);
		else{
				correctConstraint.add(id);
		}
	}
	/**
	 * Tests if a value is singleton.
	 * @param id The id of the constraint.
	 * @param valId First value tested.
	 * @param val2Id Optional second value tests
	 * @return -1 if timeout. 0 if not singleton, 1 if singleton.
	 */
	protected int testConstraint(int id, int valId, int val2Id){
		ArrayList<String> base=new ArrayList<String>((ArrayList<String>)store.get(valId));
		if(val2Id>=0)
			base=mergeLists(base, new ArrayList<String>((ArrayList<String>)store.get(val2Id)));
		
		ArrayList<String> solveString=new ArrayList<String>();
		if(val2Id>=0){
			solveString.add(0," S"+val2Id+"="+transformToList(actualVals.get(val2Id),val2Id));
		}
		solveString.add(0," S"+valId+"="+transformToList(actualVals.get(valId),valId));

		solveString=mergeLists(solveString, base);
		try{
			if(val2Id>=0)
				rpc(valId, listToString(solveString), false, false, false);
			else
				rpc(valId, listToString(solveString), false, true, false);

		}
		catch(TimeoutException e){
			return -1;
		}
		catch(Exception e){
			System.err.println("point 3");
			e.printStackTrace();
			System.err.println(listToString(solveString));
			System.err.println(pastLists.get(id));
			System.exit(1);
		}
		
		solveString=new ArrayList<String>();
		if(val2Id>=0){
			solveString.add(0," S"+val2Id+"=_");
		}
		solveString.add(0," S"+valId+"=_");
		solveString=mergeLists(solveString, base);

		String resultString=null;
		String resultString2=null;
		try{
			CompoundTerm result=rpc(valId, listToString(solveString), false, false, false);
			if(((CompoundTerm)result.arg(1)).arg(1)==null)
				return 0;
			resultString=((CompoundTerm)result.arg(1)).arg(1).toString();
			if(val2Id>=0){
				if(((CompoundTerm)((CompoundTerm)result.arg(2)).arg(1)).arg(1)==null)
					return 0;
				resultString2=((CompoundTerm)((CompoundTerm)result.arg(2)).arg(1)).arg(1).toString();
			}
		}
		catch(TimeoutException e){
			return 0;
		}
		catch(Exception e){
			System.err.println("point 4");
			e.printStackTrace();
			System.err.println(listToString(solveString));
			System.exit(1);
		}
		if(resultString.equals(transformToList(actualVals.get(valId), valId))&&!resultString.contains("-1")){
			if(val2Id<0){
				return 1;
			}
			if(resultString2.equals(transformToList(actualVals.get(val2Id), val2Id))&&!resultString2.contains("-1"))
				return 1;
		}
		return 0;
	}

	/**
	 * Converts an arraylist of ECLiPSe-str constraints to a string used by ECLiPSe-str.
	 * @param solveString The arraylist to be used
	 * @return ECLiPSe-str input.
	 */
	protected String listToString(ArrayList<String> solveString) {
		String result=solveString.toString();
		return result.substring(1,result.length()-1).concat(", str_labeling");
	}

	@Override
	public void remove(int id) {
		store.remove(id);		
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
	 * Executes in a separate instance of eclipse.
	 * @param id current constraint
	 * @param term Term to pass to eclipse (the constraints).
	 * @param trackConstraintTime True if branching point time should be tracked.
	 * @param trackHotTime True if hotspot time should be tracked.
	 * @param isTrueBranch True if the true branch is the one taken.
	 * @return A CompundTerm representing the result.
	 * @throws Exception In several cases, i.e. timeouts or unsatisfiable PCs.
	 */
	protected CompoundTerm rpc(int id, String term, boolean trackConstraintTime, boolean trackHotTime, boolean isTrueBranch)throws Exception{
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(r);
        CompoundTerm temp=null;
        r.setTerm(term);
        long endTime;
        long startTime=System.nanoTime();
        boolean isTerminated=true;
        while(isTerminated){
	        try{
	          future.get(timeout, TimeUnit.MILLISECONDS);
	          temp=r.getResult();
	          isTerminated=false;
	        }
			catch(TimeoutException e){
				endTime=System.nanoTime();
				tempTime=endTime-startTime;
				if(trackConstraintTime){
 	  				long temp2=0;
 	  				if(constraintTime.containsKey(id))
 	  					temp2=constraintTime.get(id);
					constraintTime.put(id, temp2+tempTime);
				}
				else if(trackHotTime){
 	  				long temp2=0;
 	  				if(hotSpotTime.containsKey(id))
 	  					temp2=hotSpotTime.get(id);
					hotSpotTime.put(id, temp2+tempTime);
				}
				executor.shutdownNow();
//		    		  System.err.println("Warning... killall");
		    		  rt.exec("killall -9 eclipse.exe");
//		    	  }
//		    	  else{
////		    		  System.err.println("Killed one process");
//		    		  rt.exec("kill -9 "+pid);
//		    	  }
		        r=new RunEclipse();
		        if(trackConstraintTime){
					if(isTrueBranch)
						trackedTimeouts.add(id);
					else
						otherTimeouts.add(id);
		        }
				throw e;
			}
	        catch(Exception e){
				if(e.getCause() instanceof com.parctechnologies.eclipse.Fail){
					endTime=System.nanoTime();
					tempTime=endTime-startTime;
					if(trackConstraintTime){
	 	  				long temp2=0;
	 	  				if(constraintTime.containsKey(id))
	 	  					temp2=constraintTime.get(id);
						constraintTime.put(id, temp2+tempTime);
					}
					else if(trackHotTime){
	 	  				long temp2=0;
	 	  				if(hotSpotTime.containsKey(id))
	 	  					temp2=hotSpotTime.get(id);
						hotSpotTime.put(id, temp2+tempTime);
					}
					executor.shutdownNow();
					throw e;
				}
				else if(e.getCause() instanceof com.parctechnologies.eclipse.EclipseTerminatedException
						|| e.getCause() instanceof java.net.SocketException){
					System.err.println("Warning: Terminated or Sockect Exception");
		        	executor.shutdownNow();
		        	r=new RunEclipse();
		        	executor = Executors.newSingleThreadExecutor();
		            future = executor.submit(r);
		        	r.setTerm(term);
		        	startTime=System.nanoTime();
				}
				else{
					System.err.println("point 7");
					e.printStackTrace();
					System.err.println(listToString(base));
					System.exit(1);
				}
	        }
        }
		endTime = System.nanoTime();
		tempTime=endTime-startTime;
		if(trackConstraintTime){
				long temp2=0;
				if(constraintTime.containsKey(id))
					temp2=constraintTime.get(id);
			constraintTime.put(id, temp2+tempTime);
		}
		else if(trackHotTime){
				long temp2=0;
				if(hotSpotTime.containsKey(id))
					temp2=hotSpotTime.get(id);
			hotSpotTime.put(id, temp2+tempTime);
		}
        executor.shutdownNow();
        return temp;
	}
	public void finishUp(){
		r.destroy();
	}
	/** Checks if the branching point is sat/unsat/unknown/timeout.
	 * 
	 * @param method name of the method.
	 * @param actualValue Actual value captured in DSE.
	 * @param id id of the branching point.
	 * @param sourceMap A map of source values.
	 */
	protected void constraintSatisfiability(String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap) {
		
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("Warning: actualValue not true or false");
			return;
		}
		boolean result=true;
		if(actualValue.equals("false"))
			result=false;
		
		String fName = method.split("!!")[0];
		
		if(!SatSolver.containsBoolFunction(fName)){
			System.err.println("Warning: bad call of constraintSatisfiability");
			return;
		}
			setConditionalLists(true, method, actualValue, id, sourceMap);
			ArrayList<String> trueSide;
			if(base==null)
				trueSide=null;
			else
				trueSide=new ArrayList<String>(base);
			
			setConditionalLists(false, method, actualValue, id, sourceMap);
			ArrayList<String> falseSide;
			if(base==null)
				falseSide=null;
			else
				falseSide=new ArrayList<String>(base);
			
			boolean trueSat=true;
			boolean falseSat=true;
			boolean isUnknown=true;
			taint=store.getTaint(id);
			if(taint&&(!fName.startsWith("equals")||actualValue.equals("true"))){
				int val=testConstraint(id, sourceMap.get("t"), argNum);
				if(val==1){
					taint=false;
					store.setTaint(sourceMap.get("t"), false);
					if(argNum>=0)
						store.setTaint(argNum, false);
				}
			}

			if(trueSide==null && falseSide==null){
				if(verbose){
					fileWrite+=++boolId+"\t"+fName+"\t"+"UNKNOWN\t0"+"\t"+actualValue+"\t"+taint+"\n";
//					System.err.println(fileWrite);
//					fileWrite="";
				}
				completeResult="UNKNOWN";
				newTaint(sourceMap);
				return;
			}
			if(trueSide !=null){
				try{
					CompoundTerm t=rpc(id, listToString(trueSide),true, false, !actualValue.equals("true"));
//					if(result){
//						testStringCode(trueSide, t, id);
//					}
				}
				catch(TimeoutException e){
					if(verbose){
						fileWrite+=++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint+"\n";
//						System.err.println(fileWrite);
//						fileWrite="";
					}
					completeResult="TIMEOUT";
					newTaint(sourceMap);
					return;
				}
				catch(Exception e){
					if(e.getCause() instanceof com.parctechnologies.eclipse.Fail)
						trueSat=false;
					else{
						System.err.println("point 5");
						e.printStackTrace();
						System.err.println(listToString(base));
						System.exit(1);
					}
				}
			}

			if(falseSide !=null){
				try{
					CompoundTerm t=rpc(id, listToString(falseSide), true, false, !actualValue.equals("false"));
//					if(!result){
//						testStringCode(falseSide, t, id);
//					}
				}
				catch(TimeoutException e){
					if(verbose){
						fileWrite+=++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint+"\n";
//						System.err.println(fileWrite);
//						fileWrite="";
					}
					completeResult="TIMEOUT";
					newTaint(sourceMap);
					return;
				}
				catch(Exception e){
					if(e.getCause() instanceof com.parctechnologies.eclipse.Fail)
						falseSat=false;
					else{
						System.err.println("point 6");
						e.printStackTrace();
						System.err.println(listToString(base));
						System.exit(1);
					}
				}
			}
			
			if(trueSide!=null && falseSide !=null){
				try{
					rpc(id, listToString(mergeLists(trueSide, falseSide)), false, false, false);
				}
				catch(TimeoutException e){
					if(verbose){
						fileWrite+=++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint+"\n";
//						System.err.println(fileWrite);
//						fileWrite="";
					}
					completeResult="TIMEOUT";
					newTaint(sourceMap);
					return;
				}
				catch(Exception e){
					if(e.getCause() instanceof com.parctechnologies.eclipse.Fail)
						isUnknown=false;
					else{
						System.err.println("point 7.2");
						e.printStackTrace();
						System.err.println(listToString(base));
						System.exit(1);
					}
				}
			}
			else
				isUnknown=true;
		
		if(!trueSat&&!falseSat){
			completeResult="UNSAT";
			unsat.add(id);
		}
		else if(falseSat&&!trueSat){
			completeResult="TRUEUNSAT";
			trueUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("true")){
				System.err.println("\nInaccurate trueunsat"+id+": "+sourceMap.get("t")+":"+actualVals.get(sourceMap.get("t"))+" "+sourceMap.get("s1")+":"+actualVals.get(sourceMap.get("s1")));
			}
		}
		else if(trueSat&&!falseSat){
			completeResult="FALSEUNSAT";
			falseUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("false")){
				System.err.println("\nInaccurate falseunsat"+id+": "+sourceMap.get("t")+":"+actualVals.get(sourceMap.get("t"))+" "+sourceMap.get("s1")+":"+actualVals.get(sourceMap.get("s1")));
			}
		}
		else if(isUnknown){
				completeResult="UNKNOWN";
				unknown.add(id);
				newTaint(sourceMap);
				store.setTaint(id, true);
//				checkSubset(id, sourceMap, trueSide, falseSide);
			}
		else{
			sat.add(id);
			if(!toTaints.contains(id))
				satTimeout.add(id);
			completeResult="SAT";
		}

		if(verbose){
			fileWrite+=++boolId+"\t"+fName+"\t"+completeResult;
			//TODO: fix true/false times
			if(actualValue.equals("true"))
				fileWrite+="\t"+0+"\t"+0;
			else
				fileWrite+="\t"+0+"\t"+0;
	
			fileWrite+="\t"+actualValue+"\t"+taint+"\n";
//			System.err.println(fileWrite);
//			fileWrite="";
		}
	}
	
	/**
	 * Not used. Test if the result from one branch is a subset of another. Doesn't work!
	 */
	private void checkSubset(int id, HashMap<String, Integer> sourceMap,
			ArrayList<String> trueSide, ArrayList<String> falseSide) {
		ArrayList<String>tempFalse=new ArrayList<String>(falseSide);
		for(int i=0; i<tempFalse.size(); i++){
			tempFalse.set(i, tempFalse.get(i).replace("S"+sourceMap.get("t"), "FALSE"));
		}
		ArrayList<String>subsetCheck=mergeLists(trueSide, tempFalse);
		
		subsetCheck.add("subset(S"+sourceMap.get("t")+",FALSE)");
				
		try{
			rpc(id, listToString(subsetCheck), false, false, false);
			subsets.add(id);
		}
		catch(TimeoutException e){
			return;
		}
		catch(Exception e){
			if(!(e.getCause() instanceof com.parctechnologies.eclipse.Fail))
			{
				System.err.println("point 7.3");
				e.printStackTrace();
				System.err.println(listToString(base));
				System.exit(1);
			}
		}
		
		subsetCheck=mergeLists(trueSide, tempFalse);
		subsetCheck.add("subset(S"+sourceMap.get("t")+",FALSE)");

				
		try{
			rpc(id, listToString(subsetCheck), false, false, false);
			subsets.add(id);
		}
		catch(TimeoutException e){
			return;
		}
		catch(Exception e){
			if(!(e.getCause() instanceof com.parctechnologies.eclipse.Fail))
			{
				System.err.println("point 7.4");
				e.printStackTrace();
				System.err.println(listToString(base));
				System.exit(1);
			}
		}
	}
	
	public void getStats() {
		String result="********************************\n";
		result+="JSASolver Stats:\n";
		result+="Sat: "+sat+"\n";
		result+="Unsat: "+unsat+"\n";
		result+="True Unsat: "+trueUnsat+"\n";
		result+="False Unsat: "+falseUnsat+"\n";
		result+="Unknown: "+unknown+"\n";
		result+="Num Unsound: "+numUnsound+"\n";
		result+="Accurate Paths: "+accuratePaths+"\n";

		fileWrite+=result;
	}

	public String getInfo(int id) {
		return (new ArrayList<String>((ArrayList<String>) store.get(id))).toString();
	}
	/**
	 * Not used. Tests if input values lead down the correct path.
	 */
	private void testStringCode(ArrayList<String> list, CompoundTerm term, int id){
		ArrayList<String>newList=new ArrayList<String>(list);
		String label=getNextLabel(newList);
		CompoundTerm temp=term;

		HashMap<String, String> terms=new HashMap<String, String>();
		HashSet<String>labelSet=new HashSet<String>();
		while(label!=null){
			CompoundTerm argOne=(CompoundTerm)temp.arg(1);
			if(argOne.functor().equals("str_len")){
				if(labelSet.contains(label))
					labelSet.add(label);
				else
					terms.put(label.toLowerCase(),processArg(argOne.arg(1).toString()));
				label=getNextLabel(newList);
			}
			temp=(CompoundTerm)temp.arg(2);
		}		
		if(getStringCodeResult(terms, id))
			accuratePaths.add(id);
	}

	/**
	 * helps the testStringCode method to convert an ECLiPSe-str string into a normal string.
	 * @param arg The ECLiPSe-str string.
	 * @return The normal string value. May contain wildcards.
	 */
	private String processArg(String arg) {
		arg=arg.substring(1, arg.length()-1);
		if(arg.length()==0)
			return "";
		else if(!arg.contains(",")){
			if(arg.contains("-1"))
				return "*";
			else
				return new String(Character.toChars(Integer.parseInt(arg.trim())));
		}
			
		String[] chars=arg.split(",");
		StringBuilder result=new StringBuilder();
		for(int i=0; i<chars.length; i++){
			if(chars[i].contains("-1"))
				result.append("*");
			else
				result.append(new String(Character.toChars(Integer.parseInt(chars[i].trim()))));
		}
		return result.toString();
	}

	/**
	 * Used to get a the first value used in a list of constraints.
	 * @param newList A list of string constraints.
	 * @return The first value used in a constraint.
	 */
	private String getNextLabel(ArrayList<String> newList) {
		int mark=-1;
		String next=null;
		for(int i=0; i<newList.size(); i++){
			next=newList.get(i);
			if(next.contains("str_len")){
				mark=i;
				break;
			}
		}
		if(mark!=-1){
			for(int i=0; i<=mark; i++)
				newList.remove(0);
			return next.split("\\(")[1].split(",")[0].trim();
		}
		return null;
	}
}