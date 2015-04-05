package old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * s-> string variables
 * r, ra-> replace variables
 * i-> auxillary integers
 * in-> insert
 * case-> upper/lower case
 * c->any charactor
 * @author scottkausler
 *
 */
public class Z3StrSolver extends SatSolver{
	private int boolId;
		
	private boolean verbose;
	private ArrayList<String> base;
	private ArrayList<String> arg;
	private int argNum;
	private Set<Integer>concreteVals;
		
		public Z3StrSolver(boolean verbose, String properties, String tempFile) {
			super("Z3Str","/usr/local/bin/Z3-str/Z3-str.py", "str",properties, tempFile);
//			option="-f";
			this.verbose=verbose;
			if(verbose)
				System.out.print("BoolId\tName\tSAT\treg\tnegated\tResult\ttainted?\n");
			boolId=0;
			concreteVals=new HashSet<Integer>();

			
			long sumTempTime=0;
			double reportedTime=0;
			argNum=-1;
			int iterations=10;
			for( int i=0; i<iterations; i++){
				exec("(declare-variable s2455 String) (declare-variable s2458 String) (assert (= s2458 \"y\" )) (assert (not (= s2455 s2458 ))) (declare-variable s2461 String) (assert (= s2461 \"/restart\" )) (assert (not (= s2455 s2461 ))) (check-sat)"
						,false,false, false, -1);
			}

			for( int i=0; i<iterations; i++){
				exec("(declare-variable s2455 String) (declare-variable s2458 String) (assert (= s2458 \"y\" )) (assert (not (= s2455 s2458 ))) (declare-variable s2461 String) (assert (= s2461 \"/restart\" )) (assert (not (= s2455 s2461 ))) (check-sat)"
						,false,false, false, -1);

						
				sumTempTime+=tempTime;
				if(!execResult.contains("Time(avg) =")){
					System.err.println("Warning: startup time could not be determined");
					System.err.println(execResult);
					reportedTime=.1;
					break;
				}
				else{
					double temp=Double.parseDouble(execResult.split("Time\\(avg\\) =")[1].split("\\(s\\)")[0]);
					reportedTime+=temp*Integer.parseInt(execResult.split("Runs: ")[1].split("\n")[0]);
				}
			}
			
			startupTime=(long) (((1.0)*sumTempTime-1000000000*reportedTime)/iterations);
			timeout=(int) (5000+(startupTime/1000000));
		}
	public void getStats() {
		String result="********************************\n";
		result+="Z3SolverStats:\n";
		result+="Sat: "+sat+"\n";
		result+="Unsat: "+unsat+"\n";
		result+="True Unsat: "+trueUnsat+"\n";
		result+="False Unsat: "+falseUnsat+"\n";
		result+="Unknown: "+unknown+"\n";
		result+="Time: "+getTotalConstraintTime()+"\n";
		fileWrite+=result;
	}

	@Override
	public void addRoot(String value, String actualValue, int id) {
		addNewPastList(value, actualValue, id);

	//	value=replaceExcapes(value);
		//value=addExcapes(value);
		//actualValue=changeActualExcapes(actualValue);

		String automaton;
		ArrayList<String>addValue=new ArrayList<String>();
	 if(value.startsWith("\"")){
//			char [] trimmedQuotes=new char[value.length()-2];
//			for(int i=1; i<value.length()-1; i++){
//				trimmedQuotes[i-1]=value.charAt(i);
//			}
//			automaton=new String(trimmedQuotes);
			automaton=actualValue;
			concreteVals.add(id);
		}

		else if (value.startsWith("r")||value.startsWith("$r")){
			//automaton=addExcapes(actualValue);
			automaton=null;
			symbolics.add(id);
		}
		else {
			automaton=actualValue;
		}
		
		addValue.add("(declare-variable s"+id+" String)\n");
		//Currently null implies UNSAT
		if(automaton==null){
			//System.err.println("Warning: null string encountered");
		}
		else{
			if(automaton.equals("\"\""))
				System.err.println("Error: unsupported string");
			addValue.add("(assert (= s"+id+" "+addExcapes(automaton, addValue, id)+" ))\n");
		}
		store.put(id, addValue);
		actualVals.put(id, actualValue);
	}

	@Override
	public void addOperation(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
		appendPastList(string, actualVal, id, sourceMap, false);

		processTaint(string, id, sourceMap);

		actualVal=changeActualExcapes(actualVal);
		String fName = string.split("!!")[0];
		actualVals.put(id, actualVal);
		ArrayList<String> auto=null;
		
		numOperations++;
		
		if((fName.equals("append"))||fName.equals("concat")){

			 auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));

				ArrayList<String> a2;
				String target;
				 if(sourceMap.get("s1")==null){
						auto.add("(declare-variable in0_"+id+" String)\n");
						target="in0_"+id;
						a2=new ArrayList<String>();
				 }
				 else{
					 a2=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1"))));
					if(sourceMap.size()>3){
						if(string.split("!!")[1].startsWith("[CI")){
							auto.add("(declare-variable in0_"+id+" String)\n");
							target="in0_"+id;
						}
						else{
							int start=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
							int end=Integer.parseInt(actualVals.get(sourceMap.get("s3")))-start;
							auto.add("(declare-variable in0_"+id+" String)\n");
							target="in0_"+id;
							auto.add("(assert (= "+target+" (Substring s"+sourceMap.get("s1")+" "+start+" "+end+")))\n");
						}
					}
					else if(string.split("!!")[1].equals("C")){
						target=grabChar(sourceMap.get("s1"), a2, 1);
					}
					else{
						target="s"+sourceMap.get("s1");
					}
				 }
				auto=mergeLists(auto, a2);
				auto.add("(declare-variable s"+id+" String)\n");
				auto.add("(assert (= s"+id+" (Concat s"+sourceMap.get("t")+" "+target+")))\n");
				store.put(id, auto);
		}
		else if(fName.equals("<init>")){
			if(sourceMap.get("t")!=null && sourceMap.get("s1")!=null &&actualVals.get(sourceMap.get("t")).equals("")){
				auto=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
				int oldId=sourceMap.get("s1");
				auto.add("(declare-variable s"+id+" String)\n");
				auto.add("(assert (= s"+id+" s"+oldId+" ))\n");
				store.put(id, auto);
				numOperations--;
			}
			else
				makeStringSymbolic(id);
		}
		//TODO implement other copyValueOf
		else if(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||(fName.equals("copyValueOf")&&sourceMap.size()==2)){
			int oldId;
			if(sourceMap.containsKey("t")){
				auto=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("t")));
				oldId=sourceMap.get("t");
			}
			else{
				auto=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
				oldId=sourceMap.get("s1");
			}
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= s"+id+" s"+oldId+" ))\n");
			store.put(id, auto);
			numOperations--;
		}
		else if(fName.equals("substring")){
			 auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			 int argOne;
			 String argTwo;
			if(sourceMap.size()==2){
				auto.add("(declare-variable i"+id+" Int)\n");

				argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				
				if(argOne==0){
					auto.add("(declare-variable s"+id+" String)\n");
					auto.add("(assert (= s"+id+" s"+sourceMap.get("t")+" ))\n");
					store.put(id, auto);
					return;
				}
				argTwo="i"+id;
				auto.add("(assert (= i"+id+" (- (Length s"+sourceMap.get("t")+") "+(argOne)+")))\n");
			}
			else{
				argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				argTwo=(Integer.parseInt(actualVals.get(sourceMap.get("s2")))-argOne)+"";
			}
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= s"+id+" (Substring s"+sourceMap.get("t")+" "+argOne+" "+argTwo+")))\n");
			store.put(id, auto);
		}
		else if(fName.equals("setLength")){
			 auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			int length=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			
			auto.add("(declare-variable s1_"+id+" String)\n");
			auto.add("(declare-variable s2_"+id+" String)\n");

			auto.add("(assert (= s1_"+id+" (Concat s"+sourceMap.get("t")+" s2_"+id+")))");
			
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(declare-variable i"+id+" Int)\n");
			//If the string length is already less then the length, it doesn't change
			auto.add("(assert (> "+(length+1)+" i"+id+"))");
			auto.add("(assert (= s"+id+" (Substring s1_"+id+" 0 i"+id+")))");
			store.put(id, auto);
		}
		//TODO implement other insert
		else if(fName.equals("insert")){
			auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			auto=mergeLists(auto, new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1")))));

			String newVal;
			if(string.split("!!")[1].equals("IC")){
				newVal=grabChar(sourceMap.get("s2"), auto, 1);
			}
			else if(string.split("!!")[1].startsWith("I[C")){
				auto.add("(declare-variable in0_"+id+" String)\n");
				newVal="in0_"+id;
			}
			else if(sourceMap.size()>3){
				int start=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
				int end=Integer.parseInt(actualVals.get(sourceMap.get("s4")))-start;
				auto.add("(declare-variable in0_"+id+" String)\n");
				newVal="in0_"+id;
				auto.add("(assert (= "+newVal+" (Substring s"+sourceMap.get("t")+" "+start+" "+end+")))\n");
			}
			else{
				newVal="s"+sourceMap.get("s2");
				auto=mergeLists(auto, new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s2")))));
			}

			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			
			
//			String targetVal;
//			try {
//				targetVal = getTargetString(id, listToString(auto), "s"+sourceMap.get("t"));
//			} catch (Exception e) {
//				targetVal=null;
//			}
//
//			 String end=null;
//			if(targetVal==null || targetVal.contains("@")){
////				auto.add("(declare-variable i"+id+" Int)\n");
////				//auto.add("(assert (= i"+id+" (+ (Length "+newVal+") (Length s"+sourceMap.get("t")+"))))\n");
////				end="i"+id;
//			}
//			else{
//				end=Integer.toString(targetVal.length()-(offset));
//			}
			
			auto.add("(declare-variable in1_"+id+" String)\n");
			auto.add("(declare-variable in2_"+id+" String)\n");

			if(offset==0)
				auto.add("(assert (= in1_"+id+" \"\"))\n");
			else
				auto.add("(assert (= in1_"+id+" (Substring s"+sourceMap.get("t")+" 0 "+offset+")))\n");
		//	if(end!=null)
			
			auto.add("(declare-variable in3_"+id+" Int)\n");
			auto.add("(assert (= in3_"+id+" (- (Length s"+sourceMap.get("t")+") "+(offset+1)+")))\n");
				auto.add("(assert (= in2_"+id+" (Substring s"+sourceMap.get("t")+" "+(offset+1)+" in3_"+id+")))\n");
			
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= (Concat in1_"+id+" (Concat "+newVal+" in2_"+id+")) s"+id+"))\n");
			store.put(id, auto);
		}
		else if(fName.equals("setCharAt")){
			int index=Integer.parseInt(actualVals.get(sourceMap.get("s1")));

			auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			String newVal=grabChar(sourceMap.get("s2"), auto, 1);

//			String targetVal;
//			try {
//				targetVal = getTargetString(id, listToString(auto), "s"+sourceMap.get("t"));
//			} catch (Exception e) {
//				makeStringSymbolic(id);
//				return;
//			}
//	
//
//			String end;
//			if(targetVal==null || targetVal.contains("@")){
//				auto.add("(declare-variable i"+id+" Int)\n");
//				end="i"+id;
//			}
//			else{
//				end=Integer.toString(targetVal.length()-(index+1));
//			}
			
			auto.add("(declare-variable in1_"+id+" String)\n");
			auto.add("(declare-variable in2_"+id+" String)\n");

			auto.add("(declare-variable in3_"+id+" Int)\n");

			auto.add("(assert (= in3_"+id+" (- (Length s"+sourceMap.get("t")+") "+(index+1)+")))\n");
			if(index==0){
				auto.add("(assert (= in1_"+id+" \"\"))\n");
			}
			else
				auto.add("(assert (= in1_"+id+" (Substring s"+sourceMap.get("t")+" 0 "+index+")))\n");

			auto.add("(assert (= in2_"+id+" (Substring s"+sourceMap.get("t")+" "+(index+1)+" in3_"+id+")))\n");
			
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= (Concat in1_"+id+" (Concat "+newVal+" in2_"+id+")) s"+id+"))\n");
			store.put(id, auto);
		}
		else if(fName.equals("trim")){
			 auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
//			String targetVal;
//			try {
//				targetVal=getTargetString(id, listToString(auto), "s"+sourceMap.get("t"));
//			} catch (Exception e) {
//				makeStringSymbolic(id);
//				symbolics.remove(id);
//				return;
//			}
//			if(targetVal==null||targetVal.contains("@")){
//				makeStringSymbolic(id);
//				return;
//			}
//			if(targetVal==null){
//				targetVal="";
//			}
//			int start=0, end=targetVal.length();
//			for(int i=0; i<targetVal.length(); i++){
//				if(targetVal.charAt(i)==' ')
//					start++;
//				else
//					break;
//			}
//			for(int i=targetVal.length()-1; i>=0; i--){
//				if(targetVal.charAt(i)==' ')
//					end--;
//				else
//					break;
//			}
//			auto.add("(declare-variable s"+id+" String)\n");
//			if((start==0 && end ==targetVal.length()-1)||end==1){
//				auto.add("(assert (= s"+sourceMap.get("t")+" s"+id+" ))\n");
//			}
//			else{
//				auto.add("(assert (= s"+id+" (Substring s"+sourceMap.get("t")+" "+start+" "+(end-start)+")))\n");
//			}
				auto.add("(declare-variable s"+id+" String)\n");
				auto.add("(declare-variable i"+id+"_1 Int)\n");
				auto.add("(declare-variable i"+id+"_2 Int)\n");
				auto.add("(assert (or (= s"+sourceMap.get("t")+" s"+id+" ) (= s"+id+" (Substring s"+sourceMap.get("t")+" i"+id+"_1 i"+id+"_2))))\n");

			store.put(id, auto);
		}
		else if(fName.equals("delete")){
			int index=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			int endInt=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
			
			auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			if(index>=endInt){
				auto.add("(declare-variable s"+id+" String)\n");
				auto.add("(assert (= s"+sourceMap.get("t")+" s"+id+"))\n");
				store.put(id, auto);
				return;
			}
//			String targetVal;
//			try {
//				targetVal = getTargetString(id, listToString(auto), "s"+sourceMap.get("t"));
//			} catch (Exception e) {
//				makeStringSymbolic(id);
//				return;
//			}
//
//			boolean makeEmpty=false;
//			String end;
//			if(targetVal==null || targetVal.contains("@")){
//				auto.add("(declare-variable i"+id+" Int)\n");
//				end="i"+id;
//			}
//			else{
//				if(targetVal.length()<=endInt)
//					makeEmpty=true;
//				end=Integer.toString(targetVal.length()-(endInt));
//			}
			
			auto.add("(declare-variable in1_"+id+" String)\n");
			auto.add("(declare-variable in2_"+id+" String)\n");

			if(index==0)
				auto.add("(assert (= in1_"+id+" \"\"))\n");
			auto.add("(assert (= in1_"+id+" (Substring s"+sourceMap.get("t")+" 0 "+index+")))\n");
//			if(makeEmpty)
//				auto.add("(assert (= in2_"+id+" \"\"))\n");
//			else
			
//			auto.add("(declare-variable in3_"+id+" Int)\n");
//			auto.add("(declare-variable in4_"+id+" Int)\n");

			//Only a rough approximation can be made...
//			auto.add("(assert (= in3_"+id+" (- (Length s"+sourceMap.get("t")+") "+(endInt)+")))\n");
//			auto.add("(assert (= in2_"+id+" (Substring s"+sourceMap.get("t")+" in4_"+id+" in3_"+id+")))\n");

//				auto.add("(assert (or (= in2_"+id+" (Substring s"+sourceMap.get("t")+" "+(endInt)+" in3_"+id+")) (= in2_"+id+" \"\")))\n");
			
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= (Concat in1_"+id+" in2_"+id+") s"+id+"))\n");
			store.put(id, auto);
		}
		else if(fName.equals("deleteCharAt")){
			int index=Integer.parseInt(actualVals.get(sourceMap.get("s1")));

			auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
			
			auto.add("(declare-variable in1_"+id+" String)\n");
			auto.add("(declare-variable in2_"+id+" String)\n");

			auto.add("(declare-variable in3_"+id+" Int)\n");
			auto.add("(assert (= in3_"+id+" (- (Length s"+sourceMap.get("t")+") "+(index+1)+")))\n");
			
			auto.add("(assert (= in1_"+id+" (Substring s"+sourceMap.get("t")+" 0 "+(index)+")))\n");
			auto.add("(assert (= in2_"+id+" (Substring s"+sourceMap.get("t")+" "+(index+1)+" in3_"+id+")))\n");
			
			auto.add("(declare-variable s"+id+" String)\n");
			auto.add("(assert (= (Concat in1_"+id+" in2_"+id+") s"+id+"))\n");
			store.put(id, auto);
		}
		else if(fName.equals("reverse")){
			makeStringSymbolic(id);
		}
		else if(fName.equals("toUpperCase")&&sourceMap.size()==1){
			makeStringSymbolic(id);
//			try {
//				store.put(id, changeCase(id, sourceMap, true, sourceMap.get("t"), "s"+id, "case"+id));
//			} catch (Exception e) {
//				makeStringSymbolic(id);
//				symbolics.remove(id);
//				return;
//			}
		}
		else if(fName.equals("toLowerCase")&&sourceMap.size()==1){
			makeStringSymbolic(id);
//			try {
//				store.put(id, changeCase(id, sourceMap, false, sourceMap.get("t"), "s"+id, "case"+id));
//			} catch (Exception e) {
//				makeStringSymbolic(id);
//				symbolics.remove(id);
//				return;
//			}
		}
		else if(fName.startsWith("replace")){
			makeStringSymbolic(id);
//			if(fName.equals("replaceAll")||fName.equals("replaceFirst")||string.split("!!")[1].startsWith("II")){
//				makeStringSymbolic(id);
//				return;
//			}
//			if(sourceMap.size()!=3){
//				makeStringSymbolic(id);
//				return;
//			}
//				 auto=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
//
//				ArrayList<String> a1=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
//				ArrayList<String> a2=new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s2")));
//
//				String oldVal, newVal, targetVal;
//				oldVal=newVal=targetVal="";
//				ArrayList<String> solveString=auto;
//				if(fName.equals("replaceAll")||fName.equals("replaceFirst")||string.split("!!")[1].startsWith("II")){
//					makeStringSymbolic(id);
//					return;
//				}
//				else if(string.split("!!")[1].equals("CC")){
//					String val1=actualVals.get(sourceMap.get("s1"));
//					String val2=actualVals.get(sourceMap.get("s2"));
//					
//					
//					try{
//						int tempVal=Integer.parseInt(val1);
//						if(tempVal<10 && tempVal >=0){
//							makeStringSymbolic(id);
//							return;
//						}
//						oldVal=Character.toString(((char)tempVal));
//					}
//					catch(NumberFormatException e){
//						oldVal=val1;
//					}
//					
//					try{
//						int tempVal=Integer.parseInt(val2);
//						if(tempVal<10 && tempVal >=0){
//							makeStringSymbolic(id);
//							return;
//						}
//						newVal=Character.toString(((char)tempVal));
//					}
//					catch(NumberFormatException e){
//						newVal=val2;
//					}
//					
//					try {
//						targetVal=getTargetString(id, listToString(solveString), "s"+sourceMap.get("t"));
//					} catch (Exception e) {
//						makeStringSymbolic(id);
//						symbolics.remove(id);
//						return;
//					}
//					if(targetVal==null){
//						makeStringSymbolic(id);
//						return;
//					}
//				}
//				else{ 
//					solveString=mergeLists(solveString, a1);
//					solveString=mergeLists(solveString, a2);
//					exec(id, listToString(solveString).concat("(get-model)\n"));
//					if(execResult==null)
//						return;
//					if(!execResult.startsWith("************************")){
//						System.err.println(listToString(solveString).concat("(get-model)\n"));
//						System.err.println("Bad syntax point 1...exiting");
//						System.exit(1);
//					}
//					String [] lines=execResult.split("\n");
//					for(int i=0; i<lines.length; i++){
//						if(lines[i].startsWith("s"+sourceMap.get("s1")+" ->")){
//							String [] value=lines[i].split(" -> ");
//							if(value.length>1)
//								oldVal=value[1];
//						}
//						else if(lines[i].startsWith("s"+sourceMap.get("s2")+" ->")){
//							String [] value=lines[i].split(" -> ");
//							if(value.length>1)
//								newVal=value[1];
//						}
//						else if(lines[i].startsWith("s"+sourceMap.get("t")+" ->")){
//							String [] value=lines[i].split(" -> ");
//							if(value.length>1)
//								targetVal=value[1];
//						}
//					}
//					if(oldVal.contains("@")||newVal.contains("@")){
//						makeStringSymbolic(id);
//						return;
//					}
//				}
//				if(!newVal.contains(oldVal)){
//					solveString.add("(declare-variable s"+id+" String)\n");
//					if(targetVal.length()==0){
//						solveString.add("(assert (= s"+sourceMap.get("t")+" s"+id+"))\n");
//					}
//					else{
//						int numTargets=0;
//						while(targetVal.contains(oldVal)){
//							numTargets++;
//							int index=targetVal.indexOf(oldVal);
//							String valOne=targetVal.substring(0, index);
//							String valTwo=targetVal.substring(index+oldVal.length(), targetVal.length());
//							targetVal=valOne.concat(valTwo);
//						}
//							oldVal=addExcapes(oldVal, solveString, id);
//						newVal=addExcapes(newVal, solveString, id);
//
//						solveString.add("(declare-variable r"+(-1)+"_"+id+" String)\n");
//						solveString.add("(assert (= r"+(-1)+"_"+id+" s"+sourceMap.get("t")+"))\n");
//						for(int i=0; i<numTargets; i++){
//							solveString.add("(declare-variable r"+i+"_"+id+" String)\n");
//							solveString.add("(assert (= r"+i+"_"+id+" (Replace r"+(i-1)+"_"+id+" "+oldVal+" "+newVal+")))\n");
//						}
//						solveString.add("(assert (not (Contains r"+(numTargets-1)+"_"+id+" "+oldVal+")))\n");
//						solveString.add("(assert (= r"+(numTargets-1)+"_"+id+" s"+id+"))\n");
//
//					}
//					store.put(id, solveString);
//				}
//				else
//					makeStringSymbolic(id);
		}
	else{
		makeStringSymbolic(id);
	}	
	}
	
	/**
	 * Not Used Originally used to replace a string, but the Replace operaiton didn't work well.
	 * @param auto
	 * @param id
	 * @param targetId
	 */
	private void replaceAnyString(ArrayList<String> auto, int id, int targetId){
		auto.add("(declare-variable ra_1_"+id+" String)\n");
		auto.add("(declare-variable ra_2_"+id+" String)\n");
		auto.add("(declare-variable s"+id+" String)\n");
		auto.add("(assert (= s"+id+" (Replace s"+targetId+" ra_1_"+id+" ra_2_"+id+")))\n");
		store.put(id, auto);
	}
	
	/**
	 * Executes a query for solvers that run in (Z3-str).
	 * 	 * @param id An id for the constraint to be evaluated.
	 * @param z3Val Contains the constraints to be evaluated in the language of the solver.
	 * @param trackConstraintTime True if this should go into evaluation of time at branching points.
	 * @param trackHotTime True if this should go into evaluation of time at hotspots.
	 * @param isTrueBranch True if this if for evaluation of a true branch.
	 */
	protected void exec(int id, String z3Val, boolean trackConstraintTime, boolean trackHotTime, boolean isTrueBranch) {
		exec(z3Val, trackConstraintTime, trackHotTime, isTrueBranch, id);
//		if(execResult==null)
//			makeStringSymbolic(id);
//		Runnable r = new ExecThread(z3Val, this);
//		Thread thread=new Thread(r);
//	    thread.start();
//	    try {
//			thread.join(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} // waits 100 milliseconds for slave to complete
//	    if(thread.isAlive()){
//	    	thread.stop();
//	    	destroyProcess();
//	    	makeStringSymbolic(id);
//	    	execResult=null;
//	    }
	 }

	/**
	 * Creates a new symbolic value. Happens when an operation that cannot be modeled is encountered.
	 * @param id
	 */
	private void makeStringSymbolic(int id){
		ArrayList<String> auto=new ArrayList<String>();
		auto.add("(declare-variable s"+id+" String)\n");
		store.put(id, auto);
		symbolicOps.add(id);
	}
	
	/**
	 * Collects the value generated by the solver.
	 * @param id The id of the value to be collected.
	 * @return The String value that was generated.
	 */
	private String getTargetString(int id, String solveString, String name)throws Exception{
		exec(id, solveString.concat("(get-model)\n"), false, false, false);
		if(execResult==null){
			throw new Exception();
		}
		if(!execResult.startsWith("************************")){
			System.out.flush();
			System.err.flush();
			System.err.println(solveString.concat("(get-model)\n"));
//			for(int i=0; i<solveString.length(); i++){
//				System.err.println((int)solveString.charAt(i)+" ");
//			}
			System.err.println("Bad syntax point 2...exiting");
			System.err.println(execResult);
//			for(int i=0; i<solveString.length(); i++){
//				System.out.print("\\"+(int)solveString.charAt(i)+" ");
//			}
//			new Exception().printStackTrace();
			System.exit(1);
		}
		String [] lines=execResult.split("\n");
		String targetVal=null;
		for(int i=0; i<lines.length; i++){
			if(lines[i].startsWith(name+" ->")){
				String [] value=lines[i].split(" -> ");
				if(value.length>1)
					targetVal=value[1];
				else
					targetVal="";
				break;
			}
		}
		//For some reason, empty string is given in qutos.
		if(targetVal!=null && targetVal.equals("\"\""))
			targetVal="";
		return targetVal;
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
	 * Not used. Originally attempted to model toLowerCase and toUpperCase, but it requires running Z3-str at
	 * an operation, which introduces complications.
	 */
	protected ArrayList<String> changeCase(int id, HashMap<String, Integer> sourceMap, boolean toUpperCase, int source, String target, String caseName) throws Exception{
		char start, end;
		if(toUpperCase){
			start='a';
			end='z';
		}
		else{
			start='A';
			end='Z';
		}
		
		ArrayList<String>auto=new ArrayList<String>(((ArrayList<String>) store.get(source)));
		String targetVal;
		
		targetVal = getTargetString(id, listToString(auto), "s"+source);
		
		HashMap<Character, Integer> talleys=new HashMap<Character, Integer>();
		
		if(targetVal==null){
			targetVal="";
		}
		for(int i=0; i<targetVal.length(); i++){
			char ch=targetVal.charAt(i);
			if(talleys.containsKey(ch))
				talleys.put(ch, talleys.get(ch)+1);
			else
				talleys.put(ch, 1);
		}
		auto.add("(declare-variable "+caseName+"_"+((int)start)+"_"+(-1)+" String)\n");
		//TODO: This is dumb! Should be equals, but = is a two way relationship, so it fails if we try to a variable other then source. Tried to constrain length too, but that wouldn't work.
		auto.add("(assert (Contains "+caseName+"_"+((int)start)+"_"+(-1)+" s"+source+"))\n");
		for(int i=(int)start; i<=(int)end; i++){
			char ch=(char)i;
			int count=0;
			if(talleys.containsKey(ch))
				count=talleys.get(ch);
			for(int j=0; j<count; j++){
				char replacement;
				if(toUpperCase)
					replacement=(char)(i-32);
				else
					replacement=(char)(i+32);
				auto.add("(declare-variable "+caseName+"_"+i+"_"+j+" String)\n");
				auto.add("(assert (= "+caseName+"_"+i+"_"+j+" (Replace "+caseName+"_"+i+"_"+(j-1)+" "+addExcapes(Character.toString(ch), auto, id)+" \""+replacement+"\")))\n");
			}
			auto.add("(declare-variable "+caseName+"_"+(i+1)+"_"+(-1)+" String)\n");
			auto.add("(assert (= "+caseName+"_"+(i+1)+"_"+(-1)+" "+caseName+"_"+(i)+"_"+(count-1)+"))\n");
			auto.add("(assert (not (Contains "+caseName+"_"+(i+1)+"_"+(-1)+" "+addExcapes(Character.toString(ch), auto, id)+")))\n");
		}
		auto.add("(declare-variable "+target+" String)\n");
		int temp=0;
		if(talleys.containsKey(end))
			temp=talleys.get(end);
		auto.add("(assert (= "+caseName+"_"+((int)end+1)+"_"+(temp-1)+" "+target+"))\n");
		return auto;
	}

	
	@Override
	public void addEnd(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
		addTaints(id, sourceMap.values());
		actualValue=changeActualExcapes(actualValue);
		String fName=string.split("!!")[0];
		if(SatSolver.containsBoolFunction(fName)){
			appendPastList(string, actualValue, id, sourceMap, true);
			checkSatisfiability(string, actualValue, id, sourceMap);
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

				int sourceResult=testConstraint(sourceId, -1);
				if(sourceResult==-2){
					System.err.println("Z3-strIncorrect end-"+id+"\nactualVal-"+actualValue+" "+"\n");
					System.err.println(listToString(new ArrayList<String>((ArrayList<String>)store.get(sourceId))));
					incorrectHotspot.add(id);	
					System.exit(1);
				}
				else if(sourceResult<1)
					unknownHotspot.add(id);
				else
					correctHotspot.add(id);
			
//			 ArrayList<String> solveString=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("t"))));
//			 boolean isTarget=true;
//			if(store.get(sourceMap.get("t"))==null){
//				isTarget=false;
//				solveString=new ArrayList<String>(((ArrayList<String>) store.get(sourceMap.get("s1"))));
//			}
//			long startTime=System.nanoTime();
//			String target;
//			try {
//				target = getTargetString(id, listToString(solveString), "s"+sourceMap.get("t"));
//			} catch (Exception e) {
//				hotSpotTimeout++;
//				return;
//			}
//			long endTime = System.nanoTime();
//			totalTime += endTime - startTime;
//			//System.out.println("result: "+target);
//			hotSpots++;
//			//System.out.println(listToString(solveString).concat("(check-sat)\n(get-model)\n"));
//
//			//apparently Z3-str automatically trims trailing whitespace, but it still accepts original answers.
////			if(target==null){
////				System.err.println("target: "+string+" "+actualVals.get(sourceMap.get("t"))+" "+actualVals.get(sourceMap.get("s1")));
////			}
//
//			if(target==null&&actualValue==null || (!(target==null) && target.equals(actualValue.replace("\\s+$", "")))){
//				numCorrect++;
//			}
//			else{
//				int temp;
//				if(isTarget)
//					temp=sourceMap.get("t");
//				else
//					temp=sourceMap.get("s1");
//
//				solveString.add("(assert (= "+addExcapes(actualValue, solveString, id)+" s"+temp+"))\n");
//				String execString=listToString(solveString).concat("(check-sat)\n");
//				exec(id, execString);
//				if(execResult==null){
//					hotSpotTimeout++;
//					return;
//				}
//				if(!execResult.startsWith("************************")){
//					System.err.println(execString);
//					System.err.println("result:" +execResult);
//					System.err.println(actualValue);
//				}
//				if(execResult.contains(">> SAT")){
//					//fileWrite+="unknown-"+id+": approximation-"+target+"\nactualVal-"+actualValue+" "+"\n";
//					numUnknown++;
//				}
//				else{
//					System.out.println(listToString(solveString));
//					System.exit(1);
//					fileWrite+="Z3-strIncorrect end-"+id+": approximation-"+target+"\nactualVal-"+actualValue+" "+"\n";
//					numIncorrect++;
//				}
//			}
			}
		}
		else if(containsIntFunction(string))
			store.setTaint(sourceMap.get("t"), true);
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
		if(base!=null)
			store.put(sourceMap.get("t"), base);
		if(arg!=null)
			store.put(argNum, base);
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
			int id, HashMap<String, Integer> sourceMap){
		
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("warning constraint detected without true/false value");
			return;
		}
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
//					base.add("(declare-variable i"+id+"_1-t Int)\n");
//					base.add("(declare-variable i"+id+"_2-t Int)\n");
					base.add("(assert (Contains s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" ))\n");
				}
				else{
//					base.add("(declare-variable i"+id+"_1-f Int)\n");
//					base.add("(declare-variable i"+id+"_2-f Int)\n");
					base.add("(assert (not (Contains s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" )))\n");
				}
			} else if (fName.equals("endsWith")) {
				base=mergeLists(base, arg);

				if(result){
					base.add("(declare-variable s_ends_"+id+"_t String)\n");
					base.add("(assert (= s"+sourceMap.get("t")+" (Concat s_ends_"+id+"_t s"+sourceMap.get("s1")+")))\n");

					
//					base.add("(assert (Contains s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" ))\n");			
//					//contained in

				}
				else{

//					if(concreteVals.contains(sourceMap.get("s1"))){
//						base.add("(assert (not (= (Substring s"+sourceMap.get("t")+" (- (Length s"+sourceMap.get("t")+") (Length s"+sourceMap.get("s1")+")) (Length s"+sourceMap.get("s1")+")) s"+sourceMap.get("s1")+")))\n");
//					}else{
						base.add("(declare-variable i"+id+"_1 Int)\n");
						base.add("(declare-variable i"+id+"_2 Int)\n");
						base.add("(assert (not (= (Substring s"+sourceMap.get("t")+" i"+id+"_1 i"+id+"_2 ) s"+sourceMap.get("s1")+")))\n");
//						base.add("(declare-variable s_ends_"+id+"-f String)\n");
//						base.add("(assert (not (= s"+sourceMap.get("t")+" (Concat s_ends_"+id+"-f s"+sourceMap.get("s1")+"))))\n");
//					}
					//base.add("(assert (not (= s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" )))\n");
				}
			}
			else if(fName.equals("startsWith")) {
				base=mergeLists(base, arg);
				
				String argument;
				if(sourceMap.size()==2){
					argument="s"+sourceMap.get("t");
				}
				else{
					int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
					
					if(argOne==0){
						argument="s"+sourceMap.get("t");
					}
					else{
						argument="t"+id;
				
						base.add("(declare-variable "+argument+" String)\n");
						base.add("(declare-variable i"+id+" Int)\n");

						base.add("(assert (= i"+id+" (- (Length s"+sourceMap.get("t")+") "+(argOne)+")))\n");
						base.add("(assert (= "+argument+" (Substring s"+sourceMap.get("t")+" "+argOne+" i"+id+")))\n");
					}
				}
				
				
				if(result){
					base.add("(declare-variable i"+id+"_1_t Int)\n");
					base.add("(assert (= (Substring "+argument+" 0 i"+id+"_1_t ) s"+sourceMap.get("s1")+"))\n");
				}
				else{
					if(concreteVals.contains(sourceMap.get("s1"))){
						base.add("(declare-variable i"+id+"_1_f Int)\n");
						base.add("(assert (not (= (Substring "+argument+" 0 i"+id+"_1_f ) s"+sourceMap.get("s1")+")))\n");
						//(Substring "+argument+" 0 (Length s"+sourceMap.get("s1")+") )
					}
					else{
						base.add("(declare-variable i"+id+"_1_f Int)\n");
						base.add("(assert (not (= (Substring "+argument+" 0 i"+id+"_1_f ) s"+sourceMap.get("s1")+")))\n");
					}
				}
			}
			else if(fName.equals("equals") || fName.equals("contentEquals")){
				base=mergeLists(base, arg);
				if(result){
					base.add("(assert (= s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" ))\n");
				}
				else{
					base.add("(assert (not (= s"+sourceMap.get("t")+" s"+sourceMap.get("s1")+" )))\n");
				}
			} else if (fName.equals("equalsIgnoreCase")) {
				base=arg=null;
//				try {
//					base=changeCase(id, sourceMap, true, sourceMap.get("t"), "s"+id+"_1", "case1-"+id);
//					arg=changeCase(id, sourceMap, true, sourceMap.get("s1"), "s"+id+"_2", "case2-"+id );
//				} catch (Exception e) {
//					return;
//				}
//				base=mergeLists(base, arg);
//				if(result){
//					base.add("(assert (= s"+id+"_1 s"+id+"_2))\n");
//				}
//				else{
//					base.add("(assert (not (= s"+id+"_1 s"+id+"_2)))\n");
//				}
			}
			else if(fName.equals("isEmpty")){
				if(result){
					base.add("(assert (= (Length s"+sourceMap.get("t")+") 0))\n");
				}
				else{
					base.add("(assert (> (Length s"+sourceMap.get("t")+") 0))\n");
				}
			}				
			else if(fName.equals("matches")){
				base=arg=null;
			}
			else if(fName.equals("regionMatches")){
				int toffset, ooffset, len;
				boolean ignoreCase=false;
				if(sourceMap.size()==5){
					toffset=sourceMap.get("s1");
					argNum=sourceMap.get("s2");
					arg=new ArrayList<String>((ArrayList<String>) store.get(argNum));
					ooffset=sourceMap.get("s3");
					len=sourceMap.get("s4");
				}
				else{
					toffset=sourceMap.get("s2");
					argNum=sourceMap.get("s3");
					arg=new ArrayList<String>((ArrayList<String>) store.get(argNum));
					ooffset=sourceMap.get("s4");
					len=sourceMap.get("s5");
					if(actualVals.get(sourceMap.get("s1")).equals("true")){
						ignoreCase=true;
					}
				}
				base=mergeLists(base, arg);
				if(ignoreCase){
					makeStringSymbolic(id);
					return;
//					try {
//						base=changeCase(id, sourceMap, true, sourceMap.get("t"), "s"+id+"_1", "case1-"+id);
//						arg=changeCase(id, sourceMap, true, argNum, "s"+id+"_2", "case2-"+id);
//					} catch (Exception e) {
//						return;
//					}
//					base=mergeLists(base, arg);
//					if(result){
//						base.add("(assert (= (Substring s"+id+"_1 "+toffset+" "+len+") (Substring s"+id+"_2 "+ooffset+" "+len+")))\n");
//					}
//					else{
//						base.add("(assert (or (not (= (Substring s"+id+"_1 "+toffset+" "+len+") (Substring s"+id+"_2 "+ooffset+" "+len+"))) (> "+(toffset+len)+" (Length s"+sourceMap.get("t")+"))(> "+(ooffset+len)+" (Length s"+argNum+"))))\n");
//
//					}
//				}
//				else{
//					if(result){
//							base.add("(assert (= (Substring s"+sourceMap.get("t")+" "+toffset+" "+len+") (Substring s"+argNum+" "+ooffset+" "+len+")))\n");
//					}
//					else{
//						if(toffset>=0 && ooffset >=0){
//							base.add("(assert (or (not (= (Substring s"+sourceMap.get("t")+" "+toffset+" "+len+") (Substring s"+argNum+" "+ooffset+" "+len+"))) (> "+(toffset+len)+" (Length s"+sourceMap.get("t")+"))(> "+(ooffset+len)+" (Length s"+argNum+"))))\n");
//						}
//					}
				}
				store.put(argNum, base);
				arg=null;
			}
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
//		if(!actualValue.equals("true")&& !actualValue.equals("false")){
//			System.err.println("Warning: actualValue not true or false");
//			return;
//		}
		String fName=method.split("!!")[0];
		if(!SatSolver.containsBoolFunction(fName))
			return;
//		
//		String result;
//		ArrayList<String> base=new ArrayList((ArrayList<String>) store.get(sourceMap.get("t")));
//		
//		ArrayList<String> arg=null;
		int argInt=-1;
		if(fName.equals("regionMatches")){
			if(sourceMap.size()==5){
//				arg=new ArrayList((ArrayList<String>) store.get(sourceMap.get("s2")));
				argInt=sourceMap.get("s2");
			}
			else{
//				arg=new ArrayList((ArrayList<String>)store.get(sourceMap.get("s3")));
				argInt=sourceMap.get("s3");
			}
		}else{
			if(sourceMap.containsKey("s1")){
//				arg=new ArrayList((ArrayList<String>) store.get(sourceMap.get("s1")));
				argInt=sourceMap.get("s1");
			}
		}
//		
//		String baseGuess=null;
//		String argGuess=null;
//		try {
//			baseGuess = getTargetString(id, listToString(base), "s"+sourceMap.get("t"));
//			if(argInt>=0)
//				argGuess=getTargetString(id, listToString(arg), "s"+argInt);
//
//
//		} catch (Exception e) {
//			return;
//		}
//
//		if(!(baseGuess==null || (argInt >=0 && argGuess==null))){
//			result=this.getBooleanResult(fName, baseGuess, argGuess, sourceMap);
//		}
//		else
//			result=null;
//		if(actualValue.replace("\\s+$", "").equals(result))
//			correctBool++;
//		else{
//			base.add("(assert (= "+addExcapes(actualVals.get(sourceMap.get("t")), base, id)+" s"+sourceMap.get("t")+"))\n");
//			if(argInt>0)
//				arg.add("(assert (= "+addExcapes(actualVals.get(argInt), arg, id)+" s"+argInt+"))\n");
//			
//			try {
//				baseGuess=getTargetString(id, listToString(base), "s"+sourceMap.get("t"));
//				if(argInt>0)
//					argGuess=getTargetString(id, listToString(arg), "s"+argInt);
//			} catch (Exception e) {
//				return;
//			}
//
//			result=this.getBooleanResult(fName, baseGuess, argGuess, sourceMap);
//
//			if(actualValue.replace("\\s+$", "").equals(result))
//				unknownBool++;
//			else{
//				fileWrite+="Z3-strIncorrect: "+method+" ";
//				fileWrite+=id+" "+ baseGuess+" "+argGuess+"\n";
//				fileWrite+=actualVals.get(sourceMap.get("t"))+" "+actualVals.get(argInt)+" "+"\n";
//				incorrectBool++;
//			}
//		}
		int sourceResult=testConstraint(sourceMap.get("t"), argInt);
		if(sourceResult==-2){
			System.err.println("Z3-strIncorrect: "+method+" ");
			System.err.println(actualVals.get(sourceMap.get("t"))+" "+actualVals.get(argInt)+" "+"\n");
			incorrectConstraint.add(id);	
			System.exit(1);
		}
		else if(sourceResult==-1){
			toSingleton.add(id);
		}
		else if(sourceResult<1){
			unknownConstraint.add(id);
		}
		else{
				correctConstraint.add(id);
		}
	}
	
	/**
	 * Tests if a value is singleton.
	 * @param id The id of the constraint.
	 * @param valId First value tested.
	 * @param val2Id Optional second value tests
	 * @return -2 if error, -1 if timeout, 0 if not singleton, 1 if singleton.
	 */
	protected int testConstraint(int valId, int val2Id){
		ArrayList<String> base=new ArrayList<String>((ArrayList<String>)store.get(valId));
		if(val2Id>=0)
			base=mergeLists(base, new ArrayList<String>((ArrayList<String>)store.get(val2Id)));
		
		ArrayList<String> solveString=new ArrayList<String>();
		solveString.add("(assert (= s"+valId+" "+addExcapes(actualVals.get(valId), solveString, valId)+"))\n");
		if(val2Id>=0){
			solveString.add("(assert (= s"+val2Id+" "+addExcapes(actualVals.get(val2Id), solveString, val2Id)+"))\n");
		}
		solveString=mergeLists(base, solveString);
		if(val2Id>=0)
			exec(valId, listToString(solveString).concat("(get-model)\n"), false, false, false);
		else
			exec(valId, listToString(solveString).concat("(get-model)\n"), false, true, false);

		if(execResult==null)
			return -1;
		if(!execResult.startsWith("************************")){
			System.err.println(listToString(solveString).concat("(get-model)\n"));
			System.err.println("Bad syntax point 4...exiting");
			System.err.println(execResult);
			System.exit(1);
		}
		if(!execResult.contains(">> SAT")){
			System.err.println("UNSOUND RESULT");
			return -2;
//			System.err.println("Z3-strIncorrect testResult");
//			System.err.println(listToString(solveString));
//			System.err.println(actualVals.get(valId));
//			System.exit(-1);
		}
		solveString=new ArrayList<String>();
		solveString.add("(assert (not (= s"+valId+" "+addExcapes(actualVals.get(valId), solveString, valId)+")))\n");
		solveString=mergeLists(base,solveString);
		exec(valId, listToString(solveString).concat("(get-model)\n"), false, false, false);
		if(execResult==null)
			return 0;
		if(!execResult.startsWith("************************")){
			System.err.println(listToString(solveString).concat("(get-model)\n"));
			System.err.println("Bad syntax point 1...exiting");
			System.exit(1);
		}
		if(!execResult.contains(">> UNSAT"))
			return 0;
		else if(val2Id<0)
			return 1;
		
		solveString=new ArrayList<String>();
		solveString.add("(assert (not (= s"+val2Id+" "+addExcapes(actualVals.get(val2Id), solveString, val2Id)+")))\n");
		solveString=mergeLists(base, solveString);
		exec(valId, listToString(solveString).concat("(get-model)\n"), false, false, false);
		if(execResult==null)
			return 0;
		if(!execResult.startsWith("************************")){
			System.err.println(listToString(solveString).concat("(get-model)\n"));
			System.err.println("Bad syntax point 1...exiting");
			System.exit(1);
		}
		if(!execResult.contains(">> UNSAT"))
			return 0;
		else
			return 1;
	}
	
	/**
	 * Gets rid of escape characters.
	 * @param value Value to rid the characters of.
	 * @param list List of constraints
	 * @param id The id.
	 * @return
	 */
	protected String addExcapes(String value, ArrayList<String> list, int id){
		//TODO: Decide what to do about null!
		if(value==null){
//			list.add("(declare-variable stemp"+id+" String)\n");
//			list.add("(assert (= stemp"+id+" \"\\\\0\"))\n");
//			return "stemp"+id;
			return "\"\\\\0\"";
		}
		value=value.replace('\\'+""+'n', ""+'\n');
		value=value.replace('\\'+""+'t', ""+'\t');
		value=value.replace('\\'+""+'r', ""+'\r');
		value=value.replace('\\'+""+'f', ""+'\f');
		value=value.replace('\\'+""+'b', ""+'\b');
		value=value.replace('\\'+""+'0', ""+'\0');

//		value=value.replaceAll("\\\\n", "\\\\\\\\\\"+"n");
//		value=value.replaceAll("\\\\t", "\\\\\\\\\\"+"t");
		
		boolean isConcrete=true;
		int j=0;
		StringBuilder newValue=new StringBuilder();
		for(int i=0; i<value.length(); i++){
			char c=value.charAt(i);
			if((int)c<128 && (int)c>31){
				newValue.append(c);
			}
			else{
				newValue.append((char)((int)c%96 +32));
//				String tempValue=newValue.toString();
//				tempValue=tempValue.replaceAll("\\\\\'", "'");
//				tempValue=tempValue.replace("\\\"", "\"");
//				tempValue=tempValue.replaceAll("\\\\", "\\\\\\\\");
//				tempValue=tempValue.replace("\"", "\\\"");
//				
//				list.add("(declare-variable c"+j+"_1_"+id+" String)\n");
//				list.add("(assert (= c"+j+"_1_"+id+" \""+tempValue+"\"))\n");
//				list.add("(declare-variable c"+j+"_2_"+id+" String)\n");
//				list.add("(assert (< (Length c"+j+"_2_"+id+") 2))\n");
//				j++;
//				newValue=new StringBuilder();
//				isConcrete=false;
			}
		}
//		if(!isConcrete){
//			for(int i=0; i<j; i++){
//				list.add("(declare-variable c"+i+"_"+id+" String)\n");
//				if(i==0)
//					list.add("(assert (= c"+i+"_"+id+" (Concat c"+i+"_1_"+id+" c"+i+"_2_"+id+")))\n");
//				else
//					list.add("(assert (= c"+i+"_"+id+" (Concat c"+(i-1)+"_"+id+" (Concat c"+i+"_1_"+id+" c"+i+"_2_"+id+"))))\n");
//			}
//			value=newValue.toString();
//			value=value.replaceAll("\\\\\'", "'");
//			value=value.replace("\\\"", "\"");
//			value=value.replaceAll("\\\\", "\\\\\\\\");
//			value=value.replace("\"", "\\\"");
//			list.add("(declare-variable c"+j+"_"+id+" String)\n");
//			list.add("(assert (= c"+j+"_"+id+" (Concat c"+(j-1)+"_"+id+" \""+value+"\")))\n");
//			value="c"+j+"_"+id;
//		}
//		else
		{
			value=newValue.toString();
			value=value.replaceAll("\\\\\'", "'");
			value=value.replace("\\\"", "\"");
			value=value.replaceAll("\\\\", "\\\\\\\\");
			value=value.replace("\"", "\\\"");
			value="\""+value+"\"";
		}
		return value;
	}
	protected static String changeActualExcapes(String value){
		if(value!=null){
//			value=value.replaceAll("\\n", "\\\\n");
//			value=value.replaceAll("\\t", "\\\\t");
//			value=value.replaceAll("\\r", "\\\\r");
//			value=value.replaceAll("\\f", "\\\\f");
		}
		return value;
	}
	
	@Override
	public void remove(int id) {
		store.remove(id);
	}
	/** Checks if the branching point is sat/unsat/unknown/timeout.
	 * 
	 * @param method name of the method.
	 * @param actualValue Actual value captured in DSE.
	 * @param id id of the branching point.
	 * @param sourceMap A map of source values.
	 */
	protected void checkSatisfiability(String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap) {
		
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("Warning: actualValue not true or false");
			return;
		}
		String fName = method.split("!!")[0];
		
		if(!SatSolver.containsBoolFunction(fName)){
			System.err.println("bad call of solveBooleanConstraint");
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
			boolean isUnknown=false;
			taint=store.getTaint(id);
			if(taint&&(!fName.startsWith("equals")||actualValue.equals("true"))){
				int val=testConstraint(sourceMap.get("t"), argNum);
				if(val==1){
					taint=false;
					store.setTaint(sourceMap.get("t"), false);
					if(argNum>=0)
						store.setTaint(argNum, false);
				}
			}
			
			if(trueSide==null && falseSide==null){
				if(verbose)
				System.out.println(++boolId+"\t"+fName+"\t"+"UNKNOWN\t0"+"\t"+actualValue+"\t"+taint);
				completeResult="UNKNOWN";
				newTaint(sourceMap);
				return;
			}
			if(trueSide !=null){
				exec(id, listToString(trueSide).concat("(check-sat)\n"), true, false, !actualValue.equals("true"));
				if(execResult==null){
					if(verbose)
						System.out.println(++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint);
					completeResult="TIMEOUT";
					newTaint(sourceMap);
					return;
				}
				if(!execResult.startsWith("************************")){
					System.err.println(listToString(trueSide).concat("(get-model)\n"));
					System.err.println(execResult);
					System.err.println("Bad syntax point 3...exiting...");
//					for(int i=0; i<listToString(trueSide).length();i++){
//						System.err.print((int)listToString(trueSide).charAt(i)+" ");
//					}
					completeResult="UNKNOWN";
					newTaint(sourceMap);
					System.exit(1);
				}
//				if(actualValue.equals("true"))
//					testStringCode(id);
				if(execResult.contains(">> SAT")){
					trueSat=true;
				}
				else if(execResult.contains(">> UNSAT")){
					trueSat=false;
				}
				else if(execResult.contains(">> Unknown")){
					isUnknown=true;
				} 
			}
			else
				trueSat=true;
			if(falseSide!=null){
				exec(id, listToString(falseSide).concat("(check-sat)\n"), true, false, !actualValue.equals("false"));
				if(execResult==null){
					if(verbose)
						System.out.println(++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint);
					completeResult="TIMEOUT";
					newTaint(sourceMap);
					return;
				}
				if(!execResult.startsWith("************************")){
					System.err.println(listToString(falseSide).concat("(get-model)\n"));
					System.err.println("Bad syntax point 4...exiting...");
					System.err.println(execResult);
					completeResult="UNKNOWN";
					newTaint(sourceMap);
					System.exit(1);
				}
//				if(actualValue.equals("false"))
//					testStringCode(id);
				if(execResult.contains(">> SAT")){
					falseSat=true;
				}
				else if(execResult.contains(">> UNSAT")){
					falseSat=false;
				}
				else if(execResult.contains(">> Unknown")){
					isUnknown=true;
				}
			}
			else
				falseSat=true;
		if(trueSide!=null&&falseSide!=null){	
			exec(id, listToString(mergeLists(trueSide, falseSide)).concat("(check-sat)\n"), false, false, false);
			if(execResult==null){
				if(verbose)
					System.out.println(++boolId+"\t"+fName+"\t"+"TIMEOUT\t0"+"\t"+actualValue+"\t"+taint);
				completeResult="TIMEOUT";
				newTaint(sourceMap);
				return;
			}
			if(!execResult.startsWith("************************")){
				System.err.println(listToString(mergeLists(trueSide, falseSide)).concat("(get-model)\n"));
				System.err.println("Bad syntax point ...exiting...");
				completeResult="UNKNOWN";
				newTaint(sourceMap);
				System.exit(1);
			}
			if(execResult.contains(">> SAT")){
				isUnknown=true;
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
//				System.err.println(store.get(sourceMap.get("t")));
//				System.err.println(store.get(sourceMap.get("s1")));
			}
		}
		else if(isUnknown){
				completeResult="UNKNOWN";
				unknown.add(id);
				newTaint(sourceMap);
				store.setTaint(id, true);
				//checkSubset(id, sourceMap, trueSide, falseSide);
			}
		else{
			sat.add(id);
			if(!toTaints.contains(id))
				satTimeout.add(id);
			completeResult="SAT";
		}

		if(verbose){
			System.out.print(++boolId+"\t"+fName+"\t"+completeResult);
			//TODO: fix true/false times
			if(actualValue.equals("true"))
				System.out.print("\t"+0+"\t"+0);
			else
				System.out.print("\t"+0+"\t"+0);
	
			System.out.print("\t"+actualValue+"\t"+taint+"\n");
		}
	}

	/**
	 * Not used. Test if the result from one branch is a subset of another. Doesn't work!
	 */
	private void checkSubset(int id, HashMap<String, Integer> sourceMap,
			ArrayList<String> trueSide, ArrayList<String> falseSide) {
		
		ArrayList<String>tempFalse=new ArrayList<String>(falseSide);
		for(int i=0; i<tempFalse.size(); i++){
			tempFalse.set(i, tempFalse.get(i).replace(" s"+sourceMap.get("t"), " falseside"));
		}
		ArrayList<String>subsetCheck=mergeLists(trueSide, tempFalse);
		subsetCheck.add("(assert (not (Contains s"+sourceMap.get("t")+" falseside)))");
		
		exec(id, listToString(subsetCheck).concat("(check-sat)\n"), false, false, false);
		if(execResult==null){
			//subsets++;
			return;
		}
		if(!execResult.startsWith("************************")){
			System.err.println(listToString(mergeLists(trueSide, tempFalse)).concat("(get-model)\n"));
			System.err.println("Bad syntax point ...exiting...");
			System.exit(1);
		}
		if(execResult.contains(">> UNSAT")){
			subsets.add(id);
			return;
		}
		
		subsetCheck=mergeLists(trueSide, tempFalse);
		subsetCheck.add("(assert (not (Contains falseside s"+sourceMap.get("t")+")))");
		
		exec(id, listToString(subsetCheck).concat("(check-sat)\n"), false, false, false);
		if(execResult==null){
			//subsets++;
			return;
		}
		if(!execResult.startsWith("************************")){
			System.err.println(listToString(mergeLists(trueSide, tempFalse)).concat("(get-model)\n"));
			System.err.println("Bad syntax point ...exiting...");
			System.exit(1);
		}
		if(execResult.contains(">> UNSAT")){
			subsets.add(id);
			return;
		}
	}
	
	/**
	 * Depricated. Ensures the result is a character.
	 */
	private String grabChar(int id, ArrayList<String>target, int uniqueid){
//		String val1=addExcapes(actualVals.get(id), target, id);
//		val1=val1.substring(1, val1.length()-1);
//		String key="c"+uniqueid+"_"+id;
//		target.add("(declare-variable "+key+" String)\n");
//		try{
//			int tempVal=Integer.parseInt(val1);
//			if(tempVal<10 && tempVal >=0){
//				target.add("(assert (= "+key+" \""+val1.charAt(0)+"\"))\n");
//				//Shouldn't be needed anymore
//				//target.add("(assert (or (= "+key+" \""+val1.charAt(0)+"\") (= "+key+" \""+((char)(tempVal%96+32))+"\")))\n");
//			}
//			else
//				target.add("(assert (= "+key+" \""+((char)(tempVal%96+32))+"\"))\n");
//		}
//		catch(NumberFormatException e){
//			target.add("(assert (= "+key+" \""+val1.charAt(0)+"\"))\n");
//		}
//		return key;
		return addExcapes(actualVals.get(id), target, id);
	}
	
	/**
	 * Not used. Checks for unsound values.
	 * @param method
	 * @param actualValue
	 * @param id
	 * @param sourceMap
	 */
	protected void unsoundCheck(String method, String actualValue,
			int id, HashMap<String, Integer> sourceMap){
		
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("warning constraint detected without true/false value");
			return;
		}
			String fName = method.split("!!")[0];

			base =new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("t")));
			arg=null;
				
			 argNum=-1;
				if(sourceMap.get("s1")!=null){
					arg =new ArrayList<String>((ArrayList<String>) store.get(sourceMap.get("s1")));
					argNum=sourceMap.get("s1");
				}
				
				ArrayList<String>base2=new ArrayList<String>(base);
				for(int i=0; i<base2.size(); i++){
					base2.set(i, base2.get(i).replace(" s", " snew"));
				}
				base=mergeLists(base, base2);
				base.add("(declare-variable stemp String)\n");
//				base.add("(assert (= stemp (Replace \"a\" \"a\" s"+sourceMap.get("t")+")))\n");
//				base.add("(assert (= 0 (Indexof stemp s"+sourceMap.get("t")+")))\n");
//				base.add("(assert (= 0 (Indexof s"+sourceMap.get("t")+" stemp)))\n");

				base.add("(assert (= snew"+sourceMap.get("t")+" stemp))\n");

			if (fName.equals("contains")) {
				base=mergeLists(base, arg);
				//contained in
					
					base.add("(assert (or (Contains stemp s"+sourceMap.get("s1")+" )");
					base.add(" (not (Contains stemp s"+sourceMap.get("s1")+" ))))\n");
					
			} else if (fName.equals("endsWith")) {
				base=mergeLists(base, arg);

				base.add("(assert (or (EndsWith stemp s"+sourceMap.get("s1")+" )");
				base.add(" (not (EndsWith stemp s"+sourceMap.get("s1")+" ))))\n");

			}
			else if(fName.equals("startsWith")) {
				base=mergeLists(base, arg);
				
				String argument;
				if(sourceMap.size()==2){
					argument="stemp";
				}
				else{
					int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
					
					if(argOne==0){
						argument="stemp";
					}
					else{
						argument="ttemp"+id;
				
						base.add("(declare-variable "+argument+" String)\n");
						base.add("(declare-variable i"+id+" Int)\n");

						base.add("(assert (= i"+id+" (- (Length stemp) "+(argOne)+")))\n");
						base.add("(assert (= "+argument+" (Substring stemp "+argOne+" i"+id+")))\n");
					}
				}
				
				base.add("(assert (or (StartsWith "+argument+" s"+sourceMap.get("s1")+" )");
				base.add(" (not (StartsWith "+argument+" s"+sourceMap.get("s1")+" ))))\n");
					
			}
			else if(fName.equals("equals") || fName.equals("contentEquals")){
				base=mergeLists(base, arg);
//				base.add("(assert (= stemp s"+sourceMap.get("s1")+" ))\n");

					base.add("(assert (or (= stemp s"+sourceMap.get("s1")+" )");
					base.add(" (not (= stemp s"+sourceMap.get("s1")+" ))))\n");
			} 
			else if(fName.equals("isEmpty")){
					base.add("(assert (or (= (Length stemp) 0)");
					base.add(" (> (Length stemp) 0)))\n");
			}	
			base.add("(assert (= stemp s"+sourceMap.get("t")+"))\n");
			
//			base.add("(declare-variable i"+id+"_1 Int)\n");
			base.add("(assert (not (= (Substring stemp 0 (Length stemp)) s"+sourceMap.get("t")+")))\n");
			
//			base.add("(assert (not (= s"+sourceMap.get("t")+" stemp)))\n");
			exec(id, listToString(base).concat("(check-sat)\n"), false, false, false);
			if(execResult==null){
				return;
			}
			if(!execResult.startsWith("************************")){
				System.err.println(listToString(base).concat("(get-model)\n"));
				System.err.println(execResult);
				System.err.println("Bad syntax point 3...exiting...");
//				for(int i=0; i<listToString(trueSide).length();i++){
//					System.err.print((int)listToString(trueSide).charAt(i)+" ");
//				}
				System.exit(1);
			}
			if(execResult.contains(">> UNSAT")){
				System.err.println(base);
				System.err.println(execResult);
				numUnsound.add(id);
//				System.exit(1);
			}
		}

	/**
	 * Not used. Tests if input values lead down the correct path.
	 */
	private void testStringCode(int id){
		HashMap<String, String> terms=new HashMap<String, String>();
		String [] lines=execResult.split("\\\n");
		for(int i=0; i<lines.length; i++){
			if(lines[i].startsWith("s")){
				String [] splitLine=lines[i].split(" -> ");
				if(!lines[i].contains(" -> ")&&lines[i].contains(" ->")){
					System.err.println("Modified parsing");
					splitLine=new String[2];
					splitLine[0]=lines[i].substring(0,lines[i].length()-3);
					splitLine[1]="";
				}
				else if(splitLine[1].equals("\"\""))
					splitLine[1]="";
				terms.put(splitLine[0], splitLine[1]);
			}
		}	
		if(getStringCodeResult(terms, id))
			accuratePaths.add(id);
	}
}
