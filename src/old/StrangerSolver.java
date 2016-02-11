package old;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import edu.ucsb.cs.www.vlab.stranger.PerfInfo;
import edu.ucsb.cs.www.vlab.stranger.StrangerAutomaton;


public class StrangerSolver extends SatSolver {
	
	protected HashMap<Integer, Long> time;
	
	private StrangerAutomaton base;
	private StrangerAutomaton arg;
	private int argNum;
	
	private int boolId;
	private boolean verbose;
	
	private static boolean debug=false;
	
	public StrangerSolver(boolean verbose, String properties, String tempFile) {
		super("Stranger","", "", properties, tempFile);
		this.verbose=verbose;
		store=new StoreToFile();
		time=new HashMap<Integer, Long>();
		base=null;
		arg=null;
		argNum=-1;
		if(verbose){
			System.out.println("BoolId\tName\tSAT\tResult\ttainted?\n");
			fileWrite+="BoolId\tName\tSAT\tResult\ttainted?\n";
		}
		boolId=0;
				
		//Set up Stranger
		System.setProperty("jna.library.path","/usr/local/lib/libstrangerlib.dylib");
		StrangerAutomaton.initialize(true);
		PerfInfo perfInfo = new PerfInfo();
		StrangerAutomaton.perfInfo = perfInfo;
		StrangerAutomaton.traceID = 0;
		String fileName = "traceFile"; 
		StrangerAutomaton.openCtraceFile(fileName);
	}
	@Override
	public void addRoot(String value, String actualValue, int id) {
		addNewPastList(value, actualValue, id);

		if(debug){
			System.err.println("Root: "+value+" ID: "+id+" "+actualValue);
		}
		if(id==43268){
			System.err.println(value);
			System.err.println(actualValue);
		}
		if(actualValue !=null){
			actualValue=replaceExcapes(actualValue);
		}
		value=replaceExcapes(value);
		StrangerAutomaton auto=null;
		String automaton;
		long startTime = System.nanoTime();
		if(value.startsWith("\"")){
			automaton=actualValue;
			if(actualValue.equals("")){
				auto=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				automaton=null;
			}
		}
		else if (value.startsWith("r")||value.startsWith("$r")){
			auto=StrangerAutomaton.makeAnyString();
			automaton=null;
			symbolics.add(id);
		}
		else {
				//TODO deal with chars
			automaton=actualValue.toString();
		}
		if(automaton==null){
			if(auto==null){
				auto=StrangerAutomaton.makePhi();
			}
		}
		else{
			auto=StrangerAutomaton.makeString(automaton);
		}
		long endTime = System.nanoTime();
		time.put(id, endTime - startTime);
		store.put(id, auto);
		actualVals.put(id, actualValue);
}

	/**
	 * Incrementally updates the time required to solve the constraint.
	 * @param id id of this constraint.
	 * @param sourceMap Source values
	 * @param additionalTime Time to solve this constraint.
	 */
	protected void updateTime(int id, HashMap<String, Integer> sourceMap,long additionalTime){
		long previous=additionalTime;
		Iterator<Integer> it=sourceMap.values().iterator();
		while(it.hasNext()){
			int sourceId=it.next();
			if(time.containsKey(sourceId))
				previous+=time.get(sourceId);
		}
		tempTime=previous;
		time.put(id, previous);
	}
	/**
	 * Used in a predicate method to add time to sources.
	 * @param sourceMap Sources to add time to.
	 * @param additionalTime Addition time to add.
	 */
	protected void updateSourceTime(HashMap<String, Integer> sourceMap,long additionalTime){
		Iterator<Integer> it=sourceMap.values().iterator();
		while(it.hasNext()){
			int sourceId=it.next();
			long previous=0;
			if(time.containsKey(sourceId))
				previous=time.get(sourceId);
			previous=previous+additionalTime;
			time.put(sourceId, previous);
		}
	}

	@Override
	public void addOperation(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
		appendPastList(string, actualVal, id, sourceMap, false);
//		}
		if(debug){
			System.err.println("Op: "+string+" ID: "+id+" "+actualVal);
		}
		LinkedList<String> pastList=new LinkedList<String>();
		
//		Iterator<Entry<String, Integer>> it=sourceMap.entrySet().iterator();
//		while(it.hasNext()){
//			testConstraint(it.next().getValue(), -1);
//		}
		
		actualVal=replaceExcapes(actualVal);

		processTaint(string, id, sourceMap);

		numOperations++;
		actualVals.put(id, actualVal);
		StrangerAutomaton auto=(StrangerAutomaton)store.get(sourceMap.get("t"));
		String fName = string.split("!!")[0];
		if(auto==null)
			System.err.println("null found:"+string+" source:"+sourceMap.get("t"));
		if(auto.isBottom())
			System.err.println("Error: Bottom used as base");
		StrangerAutomaton a2=null;
		if(sourceMap.get("s1")!=null){
			a2=(StrangerAutomaton)store.get(sourceMap.get("s1"));
			if(a2.isBottom())
				System.err.println("Error: Bottom used as arg");
		}
		
//		//Things get way slow when too many states are involved.
		if(auto.getNumTransitions()>5000||(a2!=null &&a2.getNumTransitions()>5000)){
			toTaints.add(id);
			makeStringSymbolic(id);
			return;
		}
		
		long startTime=System.nanoTime();
		if((fName.equals("append"))||fName.equals("concat")){
//			if(id==probId){
//				System.err.println("Op: "+string+" ID: "+id+" "+actualVal);
//				System.err.println(sourceMap.get("t")+" "+sourceMap.get("s1"));
////				System.err.println("start:<"+actualVals.get(sourceMap.get("t"))+">:end");
////				System.err.println("start:<"+actualVals.get(sourceMap.get("s1"))+">:end");
//			}
			 if(sourceMap.get("s1")==null)
				 a2=StrangerAutomaton.makeAnyString();

			StrangerAutomaton temp=StrangerAutomaton.makeEmptyString();
			if(auto.checkEquivalence(temp)){
				store.put(id, a2);
			}
			else if(a2.checkEquivalence(temp)){
				store.put(id,auto);
			}
			else{

				if(sourceMap.size()>3){
					if(string.split("!!")[1].startsWith("[CI")){
						a2=StrangerAutomaton.makeAnyString();
					}
					else{
						int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
						int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
						a2=a2.substring(argOne, argTwo);
					}
				}
				else if(string.split("!!")[1].equals("C")){
					a2=grabChar(sourceMap.get("s1"));
				}
				else if(string.split("!!")[1].equals("Z")){
					try{
						int num=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
						if(num==1){
							a2=StrangerAutomaton.makeString("true");
						}
						else{
							a2=StrangerAutomaton.makeString("false");
						}
					}
					catch(NumberFormatException e){
						
					}
				}
				StrangerAutomaton newAuto;
				
//				if(auto.checkEquivalence(StrangerAutomaton.makeAnyString())){
//					System.err.println("auto is any");
//				}
//				if(a2.checkEquivalence(StrangerAutomaton.makeAnyString())){
//					System.err.println("a2 is any");
//				}
				//TODO: Investigate this more...It appears that sometimes this works while sometimes it doesn't.
//				if(auto==a2 && !auto.checkEquivalence(StrangerAutomaton.makeAnyString())){
//					makeStringSymbolic(id);
//					return;
//				}
//				else{
//					newAuto=auto.concatenate(a2);
//				}
//				if(id==probId){
//					System.err.println("new accepts:<"+newAuto.checkMembership((actualVal)));
//				}
//				System.err.println(pastLists.get(id));
				newAuto=auto.concatenate(a2);
				store.put(id, newAuto);
			}
		}
		else if(fName.equals("<init>")){
			if(sourceMap.get("t")!=null && sourceMap.get("s1")!=null &&actualVals.get(sourceMap.get("t")).equals("")){
				auto=((StrangerAutomaton) store.get(sourceMap.get("s1"))).clone();
				store.put(id, auto);
				numOperations--;
			}
			else
				makeStringSymbolic(id);
		}
		else if(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||(fName.equals("copyValueOf")&&sourceMap.size()==2)){
			if(!sourceMap.containsKey("t")){
				auto=((StrangerAutomaton)store.get(sourceMap.get("s1")));
			}
			numOperations--;
			store.put(id, auto);
		}
		else if(string.startsWith("substring")){	
			if(sourceMap.size()==2){
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				if(argOne!=0)
					auto=auto.prefix(argOne);
			}
			else{
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
				int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s2")));				
				auto=auto.substring(argOne, argTwo);
			}
			store.put(id, auto);
		}
		else if(fName.equals("setLength")){
			int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(argOne==0)
				auto=StrangerAutomaton.makeEmptyString();
			else{
				auto=auto.concatenate(StrangerAutomaton.makeAnyString());
				StrangerAutomaton newVal=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				for(int i=0; i<=argOne; i++)
					newVal=newVal.union(auto.substring(0, i));
				
				//for some reason other approach would not work
			}
			store.put(id, auto);
		}
		//TODO implement other insert
		else if(fName.equals("insert")){
			StrangerAutomaton newVal;
			if(string.split("!!")[1].equals("IC")){
				newVal=grabChar(sourceMap.get("s2"));
			}
			else if(string.split("!!")[1].startsWith("I[C")){
				newVal=StrangerAutomaton.makeAnyString();
			}
			else if(sourceMap.size()>3){
				newVal=(StrangerAutomaton)store.get(sourceMap.get("s2"));
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
				int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s4")));
				newVal=newVal.substring(argOne, argTwo);
			}
			else{
				newVal=(StrangerAutomaton)store.get(sourceMap.get("s2"));
			}
			
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(offset>0){
				StrangerAutomaton start=auto.substring(0, offset);
				StrangerAutomaton end=auto.prefix(offset);
				auto=start.concatenate(newVal).concatenate(end);
			}
			else{
				auto=newVal.concatenate(auto);
			}
			store.put(id, auto);
		}
		else if(fName.equals("setCharAt")){
			StrangerAutomaton newVal;
			newVal=grabChar(sourceMap.get("s2"));

			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(offset>=0){
				StrangerAutomaton start=auto.substring(0, offset);
				StrangerAutomaton end=auto.prefix(offset+1);
				auto=start.concatenate(newVal).concatenate(end);
			}
			else{
				auto=newVal.concatenate(auto.prefix(1));
			}
			store.put(id, auto);
		}
		//TODO: Check for 2 cases: Restricted any string and more then 2 leading white space chars
		
		//For some reason it fails when it is any string of any length. This hack fixes it (woo). Check should be done in strangerlib.
		else if(fName.equals("trim")){
//			makeStringSymbolic(id);
			String singleton=auto.isSingleton();
			int max=getMaxLength(auto);
			if(singleton!=null){
				singleton=singleton.trim();
				if(singleton.length()==0){
					auto=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				}
				else
					auto=StrangerAutomaton.makeString(singleton);
				store.put(id, auto);
			}
			else if(max>-1){
				auto=StrangerAutomaton.makeAnyStringL1ToL2(0, max);
				store.put(id, auto);
			}
			else if(auto.generateSatisfyingExample()!=null && auto.generateSatisfyingExample().startsWith("   ")){
				//TODO: find longest length and restrict this string to it
				store.put(id, StrangerAutomaton.makeAnyString());
			}
			else if(StrangerAutomaton.makeAnyString().checkInclusion(auto)){
				store.put(id, StrangerAutomaton.makeAnyString());
			}
			else{
				StrangerAutomaton oldAuto;
				do{
					oldAuto=auto;
					auto=auto.trim();
					auto=auto.trim('\t');
					auto=auto.trim('\n');
					auto=auto.trim('\r');
					auto=auto.trim('\f');

				}while(!auto.checkEquivalence(oldAuto));
				auto=auto.union(StrangerAutomaton.makeAnyStringL1ToL2(0, 0));
				store.put(id, auto);
			}
		}
		else if(fName.equals("delete")){
			int start=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			int end=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
			if(auto.checkEquivalence(StrangerAutomaton.makeAnyString())){
				store.put(id, auto);
				return;
			}
			StrangerAutomaton startAuto;
			if(start==0)
				startAuto=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
			else
				startAuto=auto.suffix(start);
			if(end<=start)
				store.put(id, auto);
			else{
				StrangerAutomaton endAuto=auto.prefix(end);
				store.put(id, startAuto.concatenate(endAuto));
			}
		}
		else if(fName.equals("deleteCharAt")){
			int loc=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			
			StrangerAutomaton startAuto=auto.suffix(loc);
			StrangerAutomaton endAuto=auto.prefix(loc+1);
			store.put(id, startAuto.concatenate(endAuto));
		}
		else if(fName.equals("reverse")){
			makeStringSymbolic(id);
		}
		else if(fName.equals("toUpperCase")&&sourceMap.size()==1){
			if(auto.checkEquivalence(StrangerAutomaton.makeAnyString()))
				store.put(id, auto.clone());
			else{
				auto=auto.toUpperCase();
				store.put(id, auto);
			}
		}
		else if(fName.equals("toLowerCase")&&sourceMap.size()==1){
			if(auto.checkEquivalence(StrangerAutomaton.makeAnyString()))
				store.put(id, StrangerAutomaton.makeAnyString());
			else if(auto.checkEquivalence(StrangerAutomaton.makeAnyStringL1ToL2(0, 0))){
				store.put(id, StrangerAutomaton.makeAnyStringL1ToL2(0, 0));
			}
			else{
				auto=auto.toLowerCase();
				store.put(id, auto);
			}
		}
		else if(fName.startsWith("replace")){
//			makeStringSymbolic(id);
//			//TODO: Allow better replace
//			if(auto.checkEquivalence(StrangerAutomaton.makeAnyStringL1ToL2(0, 0))){
//				store.put(id,auto.clone());	
//				return;
//			}
			if(fName.equals("replaceAll")||fName.equals("replaceFirst")||string.split("!!")[1].startsWith("II")){
				makeStringSymbolic(id);
				return;
			}
			//TODO: Make this version work
			if(sourceMap.size()!=3){
				makeStringSymbolic(id);
				return;
			}
			StrangerAutomaton argOne, argTwo;
			if(string.split("!!")[1].equals("CC")){
				argOne=grabChar(sourceMap.get("s1"));
				argTwo=grabChar(sourceMap.get("s2"));
			}
			else{
				argOne=(StrangerAutomaton)store.get(sourceMap.get("s1"));
				argTwo=(StrangerAutomaton)store.get(sourceMap.get("s2"));
			}
//			String argTwoString=argTwo.isSingleton();
//			if(argTwoString==null||argOne.isSingleton()==null){
//				makeStringSymbolic(id);
//			}
//			else{
//			System.err.println("Values: <"+auto.generateSatisfyingExample()+"><"+argOne.generateSatisfyingExample()+"><"+argTwo.generateSatisfyingExample()+">");
//			System.err.println("Actual: <"+actualVals.get(sourceMap.get("t"))+"><"+actualVals.get(sourceMap.get("s1"))+"><"+actualVals.get(sourceMap.get("s2"))+">");
//			System.err.println(sourceMap.get("t")+" "+sourceMap.get("s1")+" "+sourceMap.get("s2"));
//		System.err.println(id);
//		System.err.println(StrangerAutomaton.makeAnyString().checkEquivalence(auto));


//		System.err.println(string);
//		System.err.println(sourceMap.get("t")+":"+pastLists.get(sourceMap.get("t")));
		//argTwo=StrangerAutomaton.makeAnyString();
//			String s1=argOne.isSingleton();
//			String s2=argTwo.isSingleton();
//			if(s1!=null && s2 !=null && s2.contains(s1)){
//				argTwo=StrangerAutomaton.makeAnyString();
//			}
//			String s=auto.generateSatisfyingExample();
//			if((s!=null && s.length()>1) || isRestrictedLengthAnyString(auto)){
//				auto=StrangerAutomaton.makeAnyString();
//			}
//				System.err.println(pastLists.get(sourceMap.get("t")));
//				System.err.println(pastLists.get(sourceMap.get("s1")));
//				System.err.println(pastLists.get(sourceMap.get("s2")));
//System.err.println("<"+auto.generateSatisfyingExample()+">");
//System.err.println("<"+argOne.generateSatisfyingExample()+">"+(int)argOne.generateSatisfyingExample().charAt(0));
//System.err.println("<"+argTwo.generateSatisfyingExample()+">");
//System.err.println("singleton:"+auto.isSingleton()+":"+argOne.isSingleton()+":"+argTwo.isSingleton());


				auto=auto.replace(argOne, argTwo);
				store.put(id,auto);	
			
//			}
		}

		else{
			makeStringSymbolic( id);
		}
		long endTime=System.nanoTime();
		updateTime(id,sourceMap, endTime-startTime);
//		auto=(StrangerAutomaton)store.get(id);
//		if(auto!=null)
//		System.err.println(string+" "+id+" "+sourceMap.get("t")+" "+sourceMap.get("s1")+" "+store.getTaint(id)+" "+(auto.generateSatisfyingExample()));
//		if(testConstraint(id, -1)==-2){
//			System.err.println("unsat found!");
//			System.err.println(string+" "+id+" "+sourceMap.get("t")+" "+sourceMap.get("s1")+" "+store.getTaint(id)+" "+(auto.generateSatisfyingExample()));
//			System.exit(0);
//		}
	}

	/**
	 * Creates a new symbolic value. Happens when an operation that cannot be modeled is encountered.
	 * @param id
	 */
	private StrangerAutomaton makeStringSymbolic( int id){
		long startTime=System.nanoTime();
		StrangerAutomaton auto=StrangerAutomaton.makeAnyString();
		long endTime = System.nanoTime();
		time.put(id, endTime - startTime);
		store.put(id, auto);
		symbolicOps.add(id);
		return auto;
	}

	@Override
	public void addEnd(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		actualVals.put(id, actualValue);
		propoagateToTaints(id, sourceMap.values());
		actualValue=replaceExcapes(actualValue);
		if(debug){
			System.err.println("End: "+string+" ID: "+id+" "+actualValue);
			System.err.println(sourceMap.get("t")+" "+sourceMap.get("s1")+" "+sourceMap.get("s2"));
		}
		addTaints(id, sourceMap.values());
		String fName=string.split("!!")[0];
		long startTime=System.nanoTime();

		if(SatSolver.containsBoolFunction(fName)){	
			appendPastList(string, actualValue, id, sourceMap, true);
			constraintSatisfiability(string, actualValue, id, sourceMap);
			solveBooleanConstraint(fName, actualValue, id, sourceMap);
			long assertionStartTime=System.nanoTime();
			assertBooleanConstraint(fName, actualValue, id, sourceMap);
			long assertionEndTime=System.nanoTime();
			updateSourceTime(sourceMap, assertionEndTime-assertionStartTime);
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
				int sourceResult=testConstraint(sourceId, -1);
				if(sourceResult==-2){
					fileWrite+="incorrect end-"+id+"\nactualVal-"+actualValue+" "+"\n";
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
		long endTime=System.nanoTime();
		
		updateTime(id, sourceMap, endTime-startTime);
	}
	
	/**
	 * Depricated. Ensures the result is a character.
	 * @param id The id of the constraint.
	 * @param target
	 * @param uniqueid
	 * @return A string representing an ECLiPSe-str constant character.
	 */
	private StrangerAutomaton grabChar(int id){
//		String val1=actualVals.get(id);
//		StrangerAutomaton result;
//		try{
//			int tempVal=Integer.parseInt(val1);
//			if(!(tempVal<10 && tempVal >=0)){
//				result=StrangerAutomaton.makeChar((char)tempVal);
//			}
//			else{
//				result=StrangerAutomaton.makeChar(val1.charAt(0));
//				//result=StrangerAutomaton.makeChar((char)tempVal).union(StrangerAutomaton.makeString(tempVal+""));
//			}
//		}
//		catch(NumberFormatException e){
//			result=StrangerAutomaton.makeChar(val1.charAt(0));
//		}
//		return result;
		return StrangerAutomaton.makeString(actualVals.get(id));
	}

	@Override
	public void remove(int id) {
		StrangerAutomaton auto=(StrangerAutomaton)store.remove(id);
		if(auto!=null)
			auto.Free();
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

		 base =(StrangerAutomaton) store.get(sourceMap.get("t"));
		 arg =null;
		
		argNum=-1;
		if(sourceMap.get("s1")!=null){
			arg =(StrangerAutomaton) store.get(sourceMap.get("s1"));
			argNum=sourceMap.get("s1");
		}

		if (fName.equals("contains")) {
			StrangerAutomaton x=StrangerAutomaton.makeAnyString().concatenate(arg).concatenate(StrangerAutomaton.makeAnyString());

			if(result){
					base=base.intersect(x);
			}
			else{
				StrangerAutomaton empty=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				if(!(base.checkEquivalence(empty)&&!arg.checkEquivalence(empty))){
					

					StrangerAutomaton temp=base;
					if(arg.isSingleton()!=null){
						temp=base.minus(x);
					}
					if(base.isSingleton()!=null){
						arg=arg.intersect(base.complement());
					}
					base=temp;
				}
			}
		} else if (fName.equals("endsWith")) {	
			StrangerAutomaton x=StrangerAutomaton.makeAnyString().concatenate(arg);
			if(result){
					base=base.intersect(x);
			}
			else{
				StrangerAutomaton empty=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				if(!(base.checkEquivalence(empty)&&!arg.checkEquivalence(empty))){
					StrangerAutomaton temp=base;
					if(arg.isSingleton()!=null)
						temp=base.minus(x);	
					if(base.isSingleton()!=null){
						arg=arg.intersect(base.complement());
					}
					base=temp;
				}
			}
		}
		else if(fName.equals("startsWith")&&sourceMap.size()==2) {
//			if(sourceMap.size()>2){
//				int offset=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
//				base=base.prefix(offset);
//			}

			StrangerAutomaton x=arg.concatenate(StrangerAutomaton.makeAnyString());
			if(result){
					base=base.intersect(x);
			}
			else{
				StrangerAutomaton empty=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
				if(!(base.checkEquivalence(empty)&&!arg.checkEquivalence(empty))){
					StrangerAutomaton temp=base;
					if(arg.isSingleton()!=null){
						temp=base.minus(x);
					}
	
					if(base.isSingleton()!=null){
						arg=arg.intersect(base.complement());
					}
					base=temp;
				}
			}
		}
		
		else if(fName.equals("equals") || fName.equals("contentEquals")){
			if(result){
				base=base.intersect(arg);
				arg=arg.intersect(base);
			}
			else{
				StrangerAutomaton temp=base;
				if(arg.isSingleton()!=null){
					temp=base.intersect(arg.complement());
				}
				if(base.isSingleton()!=null)
					arg=arg.intersect(base.complement());
				base=temp;
			}
		} else if (fName.equals("equalsIgnoreCase")) {
				base=base.equalsIgnoreCase(arg, result);		
		}
		else if(fName.equals("isEmpty")){
			if(result){
				base=base.intersect(StrangerAutomaton.makeEmptyString());
			}
			else{
				base=base.intersect(StrangerAutomaton.makeEmptyString().complement());
			}
		}				
		else 
			//TODO: Something with this
			if(fName.equals("matches")){
		}
		else 
			//TODO: Something with this
			if(fName.equals("regionMatches")){
			if(sourceMap.size()==5){
				argNum=sourceMap.get("s2");
			}
			else{
				argNum=sourceMap.get("s3");
			}
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
		int sourceResult=testConstraint(sourceMap.get("t"), argInt);
		if(sourceResult==-2){
			System.err.println("incorrect: "+method+" "+actualVals.get(id));
			System.err.println(actualVals.get(sourceMap.get("t"))+" "+actualVals.get(argInt)+" "+"\n");
			incorrectConstraint.add(id);	
			System.exit(1);
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
	 * @return -2 if error. 0 if not singleton, 1 if singleton.
	 */
	private int testConstraint(int valId, int val2Id) {
		StrangerAutomaton base=(StrangerAutomaton) store.get(valId);
		StrangerAutomaton arg=null;
		if(val2Id>=0){
			arg=(StrangerAutomaton)(store.get(val2Id));
		}
		
		boolean isConcrete=false;
		if(base.isTrueSingleton()!=null){
			if(arg==null || arg.isTrueSingleton()!=null)
				isConcrete=true;
		}
//		if(val2Id<0)
//			System.out.println(actualVals.get(valId));
		long tempTime=System.nanoTime();
		if( base.checkIntersection(StrangerAutomaton.makeString(actualVals.get(valId)))){
			if(val2Id<=0){
				tempTime=System.nanoTime()-tempTime;
				if(time.containsKey(valId))
					tempTime+=time.get(valId);
				hotSpotTime.put(valId, tempTime);
			}
			if(val2Id<0 ||actualVals.get(val2Id)==null ||
					(arg!=null&&arg.checkIntersection(StrangerAutomaton.makeString(actualVals.get(val2Id))))){
				if(isConcrete)
					return 1;
				else
					return 0;
			}
		}
		System.err.println("Example: "+base.generateSatisfyingExample());
System.err.println(pastLists.get(valId));
		System.err.println("Error: Unsound result in testConstraint ID:"+valId+" ID2"+val2Id);
		System.err.println(base.generateSatisfyingExample()+" "+base.generateSatisfyingExample().length()+" "+actualVals.get(valId).length());
//		System.err.println("<"+base.isSingleton()+"> <"+actualVals.get(valId)+"> <"+base.generateSatisfyingExample()+">");
//		System.err.println("<"+arg.isSingleton()+"> <"+actualVals.get(val2Id)+"> <"+arg.generateSatisfyingExample()+">");
//		System.exit(1);
		return -2;
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
			store.put(argNum, arg);
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
		String fName = method.split("!!")[0];
		
		if(!SatSolver.containsBoolFunction(fName)){
			System.err.println("Warning: bad call of constraintSatisfiability");
			return;
		}
		
		long sourceTime=0;
		if(time.containsKey(id))
			sourceTime=time.get(id);
		long startTime=System.nanoTime();
				
			setConditionalLists(true, method, actualValue, id, sourceMap);
				base.checkEmptiness();
			long trueListTime=System.nanoTime()-startTime+sourceTime;

			StrangerAutomaton trueBase=base;
			//TODO: Use arg in calculation
			StrangerAutomaton trueArg=arg;

			startTime=System.nanoTime();
			setConditionalLists(false, method, actualValue, id, sourceMap);
				base.checkEmptiness();
			long falseListTime=System.nanoTime()-startTime+sourceTime;
			constraintTime.put(id, trueListTime+falseListTime);
			StrangerAutomaton falseBase=base;
			StrangerAutomaton falseArg=arg;
			
			//check for unsoundness
			if(!((StrangerAutomaton) store.get(sourceMap.get("t"))).checkInclusion(trueBase.union(falseBase))){
				numUnsound.add(id);
				System.err.println("Unsound:"+method);
				System.err.println(pastLists.get(id));
				System.exit(0);
			}
			//check for weird over approximations
			if(!trueBase.union(falseBase).checkInclusion(((StrangerAutomaton) store.get(sourceMap.get("t"))))){
				numOver.add(id);
				System.err.println("Over:"+method);
				System.err.println(pastLists.get(id));
				System.exit(0);
			}
			
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
		
		if(trueBase!=null &&falseBase!=null &&trueBase.checkEmptiness()&&falseBase.checkEmptiness()){
			trueSat=falseSat=false;
		}
		else if(trueBase!=null &&trueBase.checkEmptiness()){
			trueSat=false;
		}
		else if(falseBase!=null &&falseBase.checkEmptiness()){
			falseSat=false;
		}
		else if(trueBase!=null &&falseBase!=null &&!trueBase.intersect(falseBase).checkEmptiness()){
			isUnknown=true;
		}


		if(!trueSat&&!falseSat){
			completeResult="UNSAT";
			unsat.add(id);
			System.err.println("StrangerUNSAT "+method+" "+(trueBase==null)+" "+(falseBase==null)+" "+sourceMap.get("s1")+sourceMap.get("t"));
			System.exit(1);
		}
		else if(falseSat&&!trueSat){
			completeResult="TRUEUNSAT";
			trueUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("true")){
				System.err.println("\nInaccurate trueunsat"+id+": "+sourceMap.get("t")+":"+actualVals.get(sourceMap.get("t"))+" "+sourceMap.get("s1")+":"+actualVals.get(sourceMap.get("s1")));
				System.err.println(method);
			}
		}
		else if(trueSat&&!falseSat){
			completeResult="FALSEUNSAT";
			falseUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("false")){
				System.err.println("\nInaccurate falseunsat"+id+": "+sourceMap.get("t")+":<"+actualVals.get(sourceMap.get("t"))+"> "+sourceMap.get("s1")+":<"+actualVals.get(sourceMap.get("s1"))+">");
				System.err.println(method);
				((StrangerAutomaton)store.get(sourceMap.get("t"))).printAutomaton();
				((StrangerAutomaton)store.get(sourceMap.get("s1"))).printAutomaton();
				System.err.println(pastLists.get(sourceMap.get("t")));
				System.err.println(pastLists.get(sourceMap.get("s1")));
				falseBase.printAutomaton();
				trueBase.printAutomaton();
				System.exit(-1);
			}
		}
		else if(isUnknown){
			completeResult="UNKNOWN";
			unknown.add(id);
			newTaint(sourceMap);
			store.setTaint(id, true);
			if(trueBase.checkInclusion(falseBase)||falseBase.checkInclusion(trueBase))
				subsets.add(id);
		}
		else{
			sat.add(id);
			if(!toTaints.contains(id))
				satTimeout.add(id);
			completeResult="SAT";
		}

		if(verbose){
			System.out.print(++boolId+"\t"+fName+"\t"+completeResult);
			System.out.print("\t"+actualValue+"\t"+taint+"\n");
//			fileWrite+=++boolId+"\t"+fName+"\t"+completeResult;
//			fileWrite+="\t"+actualValue+"\t"+taint+"\n";
		}
	}
	
	/**
	 * Get the max length of a string in an automaton. Used in the trim operaiton.
	 * @param auto The automaton to be checked for length.
	 * @return
	 */
	public static int getMaxLength(StrangerAutomaton auto){
		int max=-1;
		if(auto.isLengthFinite()){
			int [] lengths=auto.getFiniteLengths();
			for(int i=0; i<lengths.length; i++){
				if(StrangerAutomaton.makeAnyStringL1ToL2(lengths[i], lengths[i]).checkInclusion(auto)){
					if(lengths[i]>max)
						max=lengths[i];
				}
			}
		}
		return max;
	}
	
	/**
	 * Maps characters that are not represented correctly in Stranger to a new value.
	 * @param value String to be replaced.
	 * @return Modified string.
	 */
	public static String replaceExcapes(String value){
//		value=SatSolver.replaceEscapes(value);
		int newMap=255;

		value=value.replace((char)0, (char)newMap);
		value=value.replace((char)56319, (char)newMap);
		value=value.replace((char)65533, (char)newMap);

		return value;
	}
	
	public void getStats() {
		String result="********************************\n";
		result+="Stranger Stats:\n";
		result+="Sat: "+sat+"\n";
		result+="Unsat: "+unsat+"\n";
		result+="True Unsat: "+trueUnsat+"\n";
		result+="False Unsat: "+falseUnsat+"\n";
		result+="Total Unsat: "+(falseUnsat.size()+trueUnsat.size())+"\n";
		result+="Unknown: "+unknown+"\n";
		result+="CorrectHot: "+correctHotspot+"\n";

		fileWrite+=result;
	}
	/**
	 * Used for debugging. Prints the characters in a string in integer form.
	 * @param s String to be printed.
	 */
	public static void printChars(String s){
		System.err.println("start:");
		for(int i=0; i<s.length(); i++){
			System.err.print((int)s.charAt(i)+" ");
		}
		System.err.println(":end");

	}
	
	/**
	 * Checks if the automaton at the point given represents any string.
	 * @param id
	 * @return
	 */
	public boolean isAnyString(int id){
		return ((StrangerAutomaton)store.get(id)).checkEquivalence(StrangerAutomaton.makeAnyString());
	}
	
	@Override
	public void finishUp() {
		// TODO Auto-generated method stub
		
	}
}
