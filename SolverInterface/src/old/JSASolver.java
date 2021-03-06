package old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.stringoperations.AssertContainedInOther;
import dk.brics.string.stringoperations.AssertContainsOther;
import dk.brics.string.stringoperations.AssertEmpty;
import dk.brics.string.stringoperations.AssertEndsWith;
import dk.brics.string.stringoperations.AssertEquals;
import dk.brics.string.stringoperations.AssertHasLength;
import dk.brics.string.stringoperations.AssertHasNotLength;
import dk.brics.string.stringoperations.AssertNotContainsOther;
import dk.brics.string.stringoperations.AssertNotEmpty;
import dk.brics.string.stringoperations.AssertNotEquals;
import dk.brics.string.stringoperations.AssertStartsWith;
import dk.brics.string.stringoperations.Basic;
import dk.brics.string.stringoperations.BinaryOperation;
import dk.brics.string.stringoperations.Delete;
import dk.brics.string.stringoperations.DeleteCharAt;
import dk.brics.string.stringoperations.Insert;
import dk.brics.string.stringoperations.Postfix;
import dk.brics.string.stringoperations.Prefix;
import dk.brics.string.stringoperations.Replace1;
import dk.brics.string.stringoperations.Replace2;
import dk.brics.string.stringoperations.Replace3;
import dk.brics.string.stringoperations.Replace4;
import dk.brics.string.stringoperations.Replace5;
import dk.brics.string.stringoperations.Replace6;
import dk.brics.string.stringoperations.Reverse;
import dk.brics.string.stringoperations.SetCharAt1;
import dk.brics.string.stringoperations.SetCharAt2;
import dk.brics.string.stringoperations.SetLength;
import dk.brics.string.stringoperations.Substring;
import dk.brics.string.stringoperations.ToLowerCase;
import dk.brics.string.stringoperations.ToUpperCase;
import dk.brics.string.stringoperations.Trim;
import dk.brics.string.stringoperations.UnaryOperation;

public class JSASolver extends SatSolver {
	private int boolId;
	
	private boolean verbose;
	
	protected HashMap<Integer, Long> time;
	private Automaton base;
	private Automaton arg;
	private int argNum;

	public JSASolver(boolean verbose, String properties, String tempFile) {
		super("JSA","", "",properties, tempFile);
		time=new HashMap<Integer, Long>();
		loadSomeFunctions();
		this.verbose=verbose;
		if(verbose)
			System.out.print("BoolId\tName\tSAT\tResult\tbaseState\tbaseTransitions\targStates\targTransitions\ttainted?\n");
		boolId=0;
	}
	
	public void getStats() {
		String result="********************************\n";
		result+="JSASolver Stats:\n";
		result+="Sat: "+sat+"\n";
		result+="Unsat: "+unsat+"\n";
		result+="True Unsat: "+trueUnsat+"\n";
		result+="False Unsat: "+falseUnsat+"\n";
		result+="Unknown: "+unknown+"\n";
		result+="Num Hotspots: "+hotSpots+"\n";
		result+="CorrectHot: "+correctHotspot+"\n";
		result+="Hotspot time: "+hotSpotTime+"\n";
		result+="Num Unsound: "+numUnsound+"\n";
		result+="Operations: "+numOperations+"\n";
		fileWrite+=result;
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

		 base =(Automaton) store.get(sourceMap.get("t"));
		 arg =null;
		
		argNum=-1;
		if(sourceMap.get("s1")!=null){
			arg =(Automaton) store.get(sourceMap.get("s1"));
			argNum=sourceMap.get("s1");
		}
		
		if (fName.equals("contains")) {
			
			if(result){
				AssertContainsOther c=new AssertContainsOther();
				base=doOp(base, arg, c);
				AssertContainedInOther o=new AssertContainedInOther();
				arg=doOp(arg, base, o);
			}
			else{
				Automaton temp=base;
				if(arg.getFiniteStrings(1)!=null){
					Automaton x=Automaton.makeAnyString().concatenate(arg).concatenate(Automaton.makeAnyString());
					temp=base.minus(x);
				}
				if(base.getFiniteStrings(1)!=null)
					arg=arg.intersection(base.complement());
				base=temp;
			}
		} else if (fName.equals("endsWith")) {
			if(result){
				AssertEndsWith e=new AssertEndsWith();
				base=doOp(base, arg, e);
				
				Postfix p=new Postfix();
				Automaton temp=doOp(base, p);
			
				AssertContainedInOther o=new AssertContainedInOther();
				arg=doOp(temp, arg, o);
			}
			else{
		        Set<String> strings = arg.getFiniteStrings(1);
		        Automaton temp=base;
		        if (strings != null)
		        	temp=base.minus(Automaton.makeAnyString().concatenate(arg));
				
				if(base.getFiniteStrings(1)!=null){
					Postfix p=new Postfix();
					base=doOp(base, p);
					
					arg=base.intersection(arg.complement());
				}
				base=temp;
			}
		} else if (fName.equals("equalsIgnoreCase")) {
			Automaton a=ignoreCase(arg);
			Automaton b=ignoreCase(base);
			//TODO: optimize for negation.
			if(result){
				AssertEquals e=new AssertEquals();
				base=doOp(base, a, e);
				arg=doOp(arg, b, e);
			}
			else{
				AssertNotEquals n=new AssertNotEquals();
				base=doOp(base, a, n);
				arg=doOp(arg, b, n);
			}
			
		} else if (fName.equals("equals")||fName.equals("contentEquals")) {
			if(result){
				AssertEquals e=new AssertEquals();
				base=doOp(base, arg, e);
				arg=base;
			}
			else{
				AssertNotEquals n=new AssertNotEquals();
				Automaton temp=doOp(base, arg, n);
				arg=doOp(arg, base, n);
				if(base.getFiniteStrings(1)!=null)
					arg=arg.intersection(base.complement());
				base=temp;
			}
		}
		else if(fName.equals("isEmpty")){
			if(result){
				AssertEmpty l=new AssertEmpty();
				base=doOp(base, l);
			}
			else{
				AssertNotEmpty n=new AssertNotEmpty();
				base=doOp(base,n);
			}
		}
		//TODO some sort of approximation of this
		else if(fName.equals("matches")){
		}
		else if(fName.equals("regionMatches")){
			if(sourceMap.size()==5){
				argNum=sourceMap.get("s2");
			}
			else{
				argNum=sourceMap.get("s3");
			}
			
			int off=0;
			Automaton argOne;
			if(sourceMap.get("s5")!=null){
				off++;
				arg = (Automaton) store.get(sourceMap.get("s3"));
				argOne=arg;
				if(actualVals.get(sourceMap.get("s1")).equals("true")){
					argOne=ignoreCase(arg);
				}
			}
			else{
				arg = (Automaton) store.get(sourceMap.get("s2"));
				argOne=arg;
			}
			int toffset=Integer.valueOf(actualVals.get(sourceMap.get("s"+(off+1))));
			int ooffset=Integer.valueOf(actualVals.get(sourceMap.get("s"+(off+3))));
			int len=Integer.valueOf(actualVals.get(sourceMap.get("s"+(off+4))));
			
			Postfix p=new Postfix();
			Automaton baseOne=doOp(base, p);
			argOne=doOp(argOne, p);

			if(result){
				AssertHasLength l=new AssertHasLength(0, len+toffset);
				baseOne=doOp(baseOne, l);
				
				l=new AssertHasLength(0, len+ooffset);
				argOne=doOp(argOne, l);
				
				AssertEquals e=new AssertEquals();
				Automaton temp=doOp(baseOne, argOne, e);
				
				AssertContainsOther c=new AssertContainsOther();
				base=doOp(base, arg, c);
				arg=doOp(temp, arg, c);
			}
			else{
				//TODO find an approximation of this
				//There are some problems with approximating this. Noteably, the substring we are using is definite.
//				AssertHasNotLength nl=new AssertHasNotLength(0, len+toffset);
//				Automaton tempBase=doOp(baseOne, nl);
//				
//				nl=new AssertHasNotLength(0, len+ooffset);
//				Automaton tempArg=doOp(argOne,nl);
//				
//				AssertNotEquals n=new AssertNotEquals();
//				auto=doOp(baseOne, argOne, n);
//				autoArg=doOp(argOne, baseOne, n);
//				
//				AssertEquals e=new AssertEquals();
//				Automaton temp=doOp(tempBase, tempArg, e);
//				auto=auto.union(temp);
//				autoArg=autoArg.union(temp);
			}
		}
		else if(fName.equals("startsWith")&&sourceMap.size()==2) {
//			if(sourceMap.size()>2){
//				int offset=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
//				PrecisePrefix p=new PrecisePrefix(offset);
//				base=doOp(base, p);
//			}

			if(result){
				AssertStartsWith e=new AssertStartsWith();
				base=doOp(base, arg, e);
				
				Prefix p=new Prefix();
				Automaton temp=doOp(base, p);
				
				AssertContainedInOther o=new AssertContainedInOther();
				arg=doOp(arg, temp, o);
			}
			else{
				
		        Set<String> strings = arg.getFiniteStrings(1);
		        Automaton temp=base;
		        if (strings != null)
		        	temp=base.minus(arg.concatenate(Automaton.makeAnyString()));
				
		        strings = base.getFiniteStrings(1);
		        if(strings!=null){
			        	arg=arg.intersection(base.complement());
				}
		        base=temp;
			}
		}
	}
	
	/**
	 * Used to counteract initial performance penalty from lazy loading.
	 */
	private void loadSomeFunctions(){
		Automaton a=Automaton.makeString("hello");
		Automaton b=Automaton.makeString("foo");
		
		BinaryOperation bop=new AssertEquals();
		bop.op(a, b);
		bop=new AssertNotEquals();
		bop.op(a, b);
		
		bop=new AssertContainsOther();
		bop.op(a, b);
		bop=new AssertNotContainsOther();
		bop.op(a, b);
		
		bop=new AssertEndsWith();
		bop.op(a, b);
		
		bop=new AssertStartsWith();
		bop.op(a, b);
		//not ends with
		a.intersection((Automaton.makeAnyString().concatenate(b.complement())));
		
		//equalsIgnoreCase
		ignoreCase(a);
		
		UnaryOperation uop=new AssertEmpty();
		uop.op(a);
		uop=new AssertNotEmpty();
		uop.op(a);
		
		uop=new PreciseSubstring(1,2);
		uop.op(a);
		uop=new Substring();
		uop.op(a);	
	}

	@Override
	public void addRoot(String value, String actualValue, int id) {
		addNewPastList(value, actualValue, id);

		Automaton auto=null;
		String automaton;
		long startTime = System.nanoTime();

		if(value.startsWith("\"")){
			automaton=actualValue;
		}
	
		else if (value.startsWith("r")||value.startsWith("$r")){
			auto=Automaton.makeAnyString();
			automaton=null;
			symbolics.add(id);
		}
		else {
				//TODO deal with chars
			automaton=actualValue.toString();
		}
			
		if(automaton==null){
			if(auto==null)
				auto=Automaton.makeEmpty();
		}
		else
			auto=Automaton.makeString(automaton);
		long endTime = System.nanoTime();
		time.put(id, endTime - startTime);
		store.put(id, auto);
		actualVals.put(id, actualValue);
}

	@Override
	public void addOperation(String string, String actualVal, int id,
			HashMap<String, Integer> sourceMap) {
		
		appendPastList(string, actualVal, id, sourceMap, false);
		propoagateToTaints(id, sourceMap.values());
		processTaint(string, id, sourceMap);

		String fName = string.split("!!")[0];
		numOperations++;
		actualVals.put(id, actualVal);
		Automaton auto=null;
		
		long startTime=System.nanoTime();
		if((fName.equals("append"))||fName.equals("concat")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			 Automaton a2;
			 if(sourceMap.get("s1")==null){
				 a2=Automaton.makeAnyString();
			 }else
				 a2=((Automaton) store.get(sourceMap.get("s1"))).clone();
			if(string.split("!!")[1].equals("C")){
				a2=grabChar(sourceMap.get("s1"));
			}
			else if(sourceMap.size()>3){
				if(string.split("!!")[1].startsWith("[CI")){
					a2=Automaton.makeAnyString();
				}
				else{
					int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
					int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
					PreciseSubstring s =new PreciseSubstring(argOne, argTwo);
					a2=doOp(a2, s);
				}
			}

			auto=auto.concatenate(a2);
			store.put(id, auto);
		}
		else if(fName.equals("<init>")){
			if(sourceMap.get("t")!=null && sourceMap.get("s1")!=null &&actualVals.get(sourceMap.get("t")).equals("")){
				auto=((Automaton) store.get(sourceMap.get("s1"))).clone();
				store.put(id, auto);
				numOperations--;
			}
			else
				makeStringSymbolic(id);
		}
		//TODO implement other copyValueOf
		else if(fName.equals("toString")|| fName.equals("valueOf")||fName.equals("intern")||fName.equals("trimToSize")||(fName.equals("copyValueOf")&&sourceMap.size()==2)){
			if(id==99){
				System.err.println(actualVal+" "+actualVals.get(sourceMap.get("s1"))+" "+actualVals.get(sourceMap.get("t")));
			}
			if(sourceMap.containsKey("t")){
				auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			}
			else
				auto=((Automaton) store.get(sourceMap.get("s1"))).clone();
			store.put(id, auto);
			numOperations--;
		}
		else if(fName.equals("substring")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(sourceMap.size()==2){
				if(argOne!=0){
//					Postfix p=new Postfix();
					PrecisePrefix p=new PrecisePrefix(argOne); 
					auto=doOp(auto, p);
				}
			}
			else{
				int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
				PreciseSubstring s=new PreciseSubstring(argOne, argTwo);
				auto=doOp(auto,s);
			}
			store.put(id, auto);
//			if(sourceMap.size()==2){
//				Postfix t=new Postfix();
//				auto=doOp(auto, t);
//			}
//			else{
//				if(actualVals.get(sourceMap.get("s1")).equals("0")){
//					Prefix t=new Prefix();
//					auto=doOp(auto, t);
//				}
//				else{
//					Substring t=new Substring();
//					auto=doOp(auto, t);
//				}
//				
//
//				int length=argTwo-argOne;
//				AssertHasLength l=new AssertHasLength(length, length);
//				auto=doOp(auto,l);
//			}
//			store.put(id, auto);
		}
		else if(fName.equals("setLength")){
			auto=(Automaton) store.get(sourceMap.get("t"));
			int length=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(length==0)
				auto=Automaton.makeEmptyString();
			else{
				auto=auto.concatenate(Automaton.makeAnyString());
				
				AssertHasLength s=new AssertHasLength(0, length);
				auto=doOp(auto,s);
			}

			store.put(id, auto);

//			SetLength a=new SetLength();
//			Automaton base=(Automaton) store.get(sourceMap.get("t"));
//			base=doOp(base, a);
//			store.put(id, base);
		}
		else if(fName.equals("setCharAt")){
			auto=(Automaton) store.get(sourceMap.get("t"));

			Automaton newVal;
			newVal=grabChar(sourceMap.get("s2"));

			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(offset>=0){
				
				PreciseSubstring s =new PreciseSubstring(0, offset);
				Automaton start=doOp(auto, s);
				PrecisePrefix p =new PrecisePrefix(offset+1);
				Automaton end=doOp(auto, p);
				auto=start.concatenate(newVal).concatenate(end);
			}
			else{
				PrecisePrefix p =new PrecisePrefix(1);
				auto=newVal.concatenate(doOp(auto, p));
			}
			store.put(id, auto);
		}
		//TODO: This may be incorrect
		else if(fName.equals("insert")){
			auto=(Automaton) store.get(sourceMap.get("t"));

			Automaton newVal;
			if(string.split("!!")[1].equals("IC")){
				newVal=grabChar(sourceMap.get("s2"));
			}
			else if(string.split("!!")[1].startsWith("I[C")){
				newVal=Automaton.makeAnyString();
			}
			else if(sourceMap.size()>3){
				newVal=(Automaton)store.get(sourceMap.get("s2"));
				int argOne=Integer.parseInt(actualVals.get(sourceMap.get("s3")));
				int argTwo=Integer.parseInt(actualVals.get(sourceMap.get("s4")));
				PreciseSubstring s =new PreciseSubstring(argOne, argTwo);
				newVal=doOp(newVal, s);
			}
			else{
				newVal=(Automaton)store.get(sourceMap.get("s2"));
			}
			
			int offset=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			if(offset>=0){
				PreciseSubstring s =new PreciseSubstring(0, offset);
				Automaton start=doOp(auto, s);
				PrecisePrefix p =new PrecisePrefix(offset);
				Automaton end=doOp(auto, p);
				auto=start.concatenate(newVal).concatenate(end);
			}
			else{
				auto=newVal.concatenate(auto);
			}
			store.put(id, auto);
		}
		else if(fName.equals("trim")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			
			AssertHasLength l=new AssertHasLength(1,1);
			Automaton temp=auto.intersection(l.op(auto));
			if(temp.equals(auto))
				auto=temp.union(Automaton.makeEmptyString());	
			else{	
				Trim t=new Trim();
				auto=doOp(auto, t);
			}
			store.put(id, auto);
		}
		else if(fName.equals("delete")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			int start=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			int end=Integer.parseInt(actualVals.get(sourceMap.get("s2")));
			
			if(auto.equals(Automaton.makeAnyString())){
				store.put(id, auto);
				return;
			}
			Automaton startAuto;
			if(start==0){
				startAuto=Automaton.makeEmptyString();
			}
			else{
				PreciseSuffix s=new PreciseSuffix(start);
				startAuto=doOp(auto, s);
			}
			if(end<=start){
				store.put(id, auto);
			}
			else{
				PrecisePrefix p =new PrecisePrefix(end);
				Automaton endAuto=doOp(auto, p);
				auto=startAuto.concatenate(endAuto);
				store.put(id, auto);
			}
		}
		else if(fName.equals("deleteCharAt")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			int loc=Integer.parseInt(actualVals.get(sourceMap.get("s1")));
			PreciseSuffix s=new PreciseSuffix(loc);
			Automaton startAuto=doOp(auto, s);
			PrecisePrefix p =new PrecisePrefix(loc+1);
			Automaton endAuto=doOp(auto, p);
			store.put(id, startAuto.concatenate(endAuto));
		}
		else if(fName.equals("reverse")){
			 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
			Reverse t=new Reverse();
			store.put(id, doOp(auto, t));
		}
		else if(fName.equals("toUpperCase")&&sourceMap.size()==1){
			if(sourceMap.size()>1){
				makeStringSymbolic(id);
			}
			else{
				 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
				ToUpperCase t=new ToUpperCase();
				store.put(id, doOp(auto, t));
			}
		}
		else if(fName.equals("toLowerCase")&&sourceMap.size()==1){
				 auto=((Automaton) store.get(sourceMap.get("t"))).clone();

				ToLowerCase t=new ToLowerCase();
				auto=doOp(auto, t);
				store.put(id, auto);
		}
		else if(fName.startsWith("replace")){
			if(sourceMap.size()==3){
				 auto=((Automaton) store.get(sourceMap.get("t"))).clone();

				Automaton a1=(Automaton) store.get(sourceMap.get("s1"));
				Automaton a2=(Automaton) store.get(sourceMap.get("s2"));
				Set<String>a1Strings=a1.getFiniteStrings(1);
				Set<String>a2Strings=a2.getFiniteStrings(1);

				if(fName.equals("replaceAll")||fName.equals("replaceFirst")){
					makeStringSymbolic(id);
				}
				else if(string.split("!!")[1].equals("CC")){
					String val1=actualVals.get(sourceMap.get("s1"));
					String val2=actualVals.get(sourceMap.get("s2"));
					
					char oldChar;
					char newChar;
					boolean isFirstUnknown=false;
					boolean isSecondUnknown=false;
					try{
						int tempVal=Integer.parseInt(val1);
						if(tempVal<10 && tempVal >=0){
							isFirstUnknown=true;
						}
						oldChar=((char)tempVal);
//						System.err.println("not number format:"+oldChar);
					}
					catch(NumberFormatException e){
						oldChar=val1.charAt(0);
//						System.err.println("warning: number format:"+oldChar);
					}
					try{
						int tempVal=Integer.parseInt(val2);
						if(tempVal<10 && tempVal >=0){
							isSecondUnknown=true;
						}
						newChar=((char)tempVal);
//						System.err.println("not number format:"+newChar);
					}
					catch(NumberFormatException e){
						newChar=val2.charAt(0);
//						System.err.println("warning: number format:"+newChar);
					}
					UnaryOperation r;
					if(isFirstUnknown && isSecondUnknown)
						r=new Replace4();
					else if(isFirstUnknown)
						r=new Replace3(newChar);
					else if(isSecondUnknown)
						r=new Replace2(oldChar);
					else
						r=new Replace1(oldChar, newChar);
					store.put(id, doOp(auto, r));
				}
				else if(a1Strings!=null&&a2Strings!=null){
					String s1=(String) a1Strings.toArray()[0];
					String s2=(String) a2Strings.toArray()[0];
					Replace6 r= new Replace6(s1, s2);
					store.put(id, doOp(auto, r));
				}
				else{
					makeStringSymbolic(id);
				}
			}
			else{
				 auto=((Automaton) store.get(sourceMap.get("t"))).clone();
				Automaton a2=((Automaton) store.get(sourceMap.get("s3"))).clone();
				Replace5 r=new Replace5();
				store.put(id, doOp(auto, a2, r));
			}
		}
		//format at least gets called
		else{
			makeStringSymbolic(id);
		}
//		auto=(Automaton)store.get(id);
//		if(auto!=null&&!auto.run(actualVal)){
//			System.err.println("Warning: Bad actual val");
//			System.err.println(id+":"+string);
//			System.err.println(auto.getFiniteStrings(1));
//			System.err.println(actualVal+" "+actualVals.get(sourceMap.get("t")));
//			System.err.println(pastLists.get(id));
//			System.err.println(sourceMap);
//		}
		long endTime=System.nanoTime();
		updateTime(id,sourceMap, endTime-startTime);
//		auto=(Automaton) store.get(id);
//		if(auto!=null)
//		System.err.println(string+" "+id+" "+sourceMap.get("t")+" "+sourceMap.get("s1")+" "+store.getTaint(id)+" "+(auto.getShortestExample(true)));
	}
	/**
	 * Performs a JSA BinaryOperation.
	 * @param a1 First Automaton in operation.
	 * @param a2 Second Automaton in operation
	 * @param b Operation itself.
	 * @return The resulting automaton.
	 */
	private Automaton doOp(Automaton a1, Automaton a2, BinaryOperation b){
		a1=b.op(a1, a2);
		return a1;
	}
	/**
	 * Performs a JSA UnaryOperation.
	 * @param auto First Automaton in operation.
	 * @param t Operation itself.
	 * @return The resulting automaton.
	 */
	private Automaton doOp(Automaton auto, UnaryOperation t) {
		auto=t.op(auto);
		return auto;
	}

	/**
	 * Creates a new symbolic value. Happens when an operation that cannot be modeled is encountered.
	 * @param id
	 */
	private void makeStringSymbolic(int id){
		Automaton auto=Automaton.makeAnyString();
		store.put(id, auto);
		symbolicOps.add(id);
	}

	@Override
	public void addEnd(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		propoagateToTaints(id, sourceMap.values());
		String fName=string.split("!!")[0];
		addTaints(id, sourceMap.values());
		long startTime=System.nanoTime();
		if(SatSolver.containsBoolFunction(fName)){
			appendPastList(string, actualValue, id, sourceMap, true);
//			System.err.println(string+" "+id+" "+sourceMap.get("t")+" "+sourceMap.get("s1")+" "+store.getTaint(id));
//			System.err.println(pastLists.get(id));

			constraintSatisfiability(string, actualValue, id, sourceMap);
			solveBooleanConstraint(string, actualValue, id, sourceMap);
			long assertionStartTime=System.nanoTime();
			assertBooleanConstraint(fName, actualValue, id, sourceMap);
			long assertionEndTime=System.nanoTime();
			updateSourceTime(sourceMap, assertionEndTime-assertionStartTime);
			numConstraints++;
			if(isTrackedTimeout(id))
				newToTaint(id, sourceMap);
		}
		else if(AnalysisInfo.getEndMethods(properties).contains(fName)){
//			System.out.println(actualValue);
			int tempId=sourceMap.get("t");
			Automaton auto=(Automaton) store.get(sourceMap.get("t"));
			if(auto==null){
				tempId=sourceMap.get("s1");
				auto=(Automaton) store.get(sourceMap.get("s1"));
			}
			if(pastLists.containsKey(tempId)&&pastLists.get(tempId).size()>1){
				hotSpots++;
				boolean test;
				long tempTime=System.nanoTime();
				test=auto.run(replaceExcapes(actualValue.toString()));
				tempTime=System.nanoTime()-tempTime;
				if(time.containsKey(id))
					tempTime+=time.get(id);
				hotSpotTime.put(id, tempTime);
				if(test){
					test=auto.subsetOf(Automaton.makeString(replaceExcapes(actualValue.toString())));
					if(test){
						correctHotspot.add(id);
					}
					else{
						unknownHotspot.add(id);
					}
				}
				else{
					System.err.println(pastLists.get(sourceMap.get("t")));
					fileWrite="Incorrect!!! " +actualValue+" "+id+": " +auto.run(actualValue.toString())+" "+auto.getShortestExample(true)+" "+auto.isEmpty()+" "+auto.isEmptyString()+" "+string+" "+"\n";
					System.err.println(fileWrite);
					System.exit(1);
					incorrectHotspot.add(id);
				}
			}
		}
		else if(containsIntFunction(string))
			store.setTaint(sourceMap.get("t"), true);
		long endTime=System.nanoTime();
		updateTime(id, sourceMap, endTime-startTime);
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
		String fName = method.split("!!")[0];
		
		if(!SatSolver.containsBoolFunction(fName))
			return;
		
//		if(!actualValue.equals("true")&& !actualValue.equals("false")){
//			System.err.println("Warning: actualValue not true or false");
//			return;
//		}
//
//			String result;
			boolean isConcrete=true;
			Automaton base=(Automaton) store.get(sourceMap.get("t"));

			String baseActual=actualVals.get(sourceMap.get("t"));

			Set<String> baseStrings=base.getFiniteStrings(1);
			if(!(baseStrings!=null)){
				isConcrete=false;
				base=base.intersection(Automaton.makeString(baseActual));
			}


//			//This line of code looks extremely unsafe...but there should be at least one string or else you would be calling a function on a null value.
//			String baseGuess=base.getFiniteStrings().iterator().next();
//			
				Automaton arg;
				String argActual;
				if(fName.equals("regionMatches")){
					if(sourceMap.size()==5){
						arg=(Automaton) store.get(sourceMap.get("s2"));
						argActual=actualVals.get(sourceMap.get("s2"));
					}
					else{
						arg=(Automaton) store.get(sourceMap.get("s3"));
						argActual=actualVals.get(sourceMap.get("s3"));
					}
				}else{
					argActual=actualVals.get(sourceMap.get("s1"));
					if(argActual!=null)
						arg=(Automaton) store.get(sourceMap.get("s1"));
					else
						arg=null;
				}
	//			String argGuess=null;
				if(arg!=null){					
					Set<String> argStrings=arg.getFiniteStrings(1);
					
					if(!(argStrings!=null)){
						isConcrete=false;
						arg=arg.intersection(Automaton.makeString(argActual));
	//				if(base.isEmpty()){
	//					fileWrite+="empty: "+actualValue+" "+base.isEmpty()+" "+arg.isEmpty()+" "+constraints.get(sourceMap.get("t")).getShortestExample(true)+constraints.get(sourceMap.get("s1")).getShortestExample(true)+"\n";
	//					incorrectBool++;
	//					return;
	//				}
					}
//					argGuess=arg.getShortestExample(true);
				}
//				result=this.getBooleanResult(fName, baseGuess, argGuess, sourceMap);

		if(base.getFiniteStrings(1)!=null && (arg==null || arg.getFiniteStrings(1) !=null)){
			if(isConcrete){
					correctConstraint.add(id);
			}
			else{
				unknownConstraint.add(id);
			}
		}
		else{
			fileWrite+="JSA incorrect: "+method+" "+actualValue;
			fileWrite+=id+" "+ "base: " +((Automaton)store.get(sourceMap.get("t"))).getShortestExample(true)+" ";
			if(arg!=null)
				fileWrite+="arg: "+ arg.getShortestExample(true);
			fileWrite+="\n"+actualVals.get(sourceMap.get("t"))+" "+actualVals.get(sourceMap.get("s1"))+" "+isConcrete+"\n";
			incorrectConstraint.add(id);
			System.err.println(fileWrite);
			System.exit(-1);
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
		if(base!=null)
			store.put(sourceMap.get("t"), base);
		if(arg!=null)
			store.put(argNum, arg);
		}
	
/**
 * An extended ignoreCase operation for equalsIgnoreCase.
 * @param arg The automaton where the operation should be applied.
 * @return The result of the ignoreCase operation.
 */
	protected Automaton ignoreCase(Automaton arg) {
	    Automaton b = arg.clone();
	    for (State s : b.getStates()) {
	        Set<Transition> transitions = s.getTransitions();
	        for (Transition t : new ArrayList<Transition>(transitions)) {
	            char min = t.getMin();
	            char max = t.getMax();
	            State dest = t.getDest();
	            if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {
	                for (int c = min; c <= max; c++) {
	                	if(Character.isUpperCase((char)c))
	                		transitions.add(new Transition(Character.toLowerCase((char) c), dest));
	                	if(Character.isLowerCase((char)c))
	                		transitions.add(new Transition(Character.toUpperCase((char) c), dest));
	                }
	            }
	        }
	    }
	    b.setDeterministic(false);
	    b.reduce();
	    b.minimize();
	    return b;
	}

	@Override
	public void remove(int id) {	
		store.remove(id);
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
	
	/** Checks if the branching point is sat/unsat/unknown/timeout.
	 * 
	 * @param method name of the method.
	 * @param actualValue Actual value captured in DSE.
	 * @param id id of the branching point.
	 * @param sourceMap A map of source values.
	 */
	protected void constraintSatisfiability(String string, String actualValue, int id,
			HashMap<String, Integer> sourceMap) {
		
		
		if(!actualValue.equals("true")&& !actualValue.equals("false")){
			System.err.println("Warning: actualValue not true or false");
			return;
		}
		String fName = string.split("!!")[0];
		if(!SatSolver.containsBoolFunction(fName)){
			System.err.println("Warning: bad call of constraintSatisfiability");
			return;
		}
		long sourceTime=0;
		if(time.containsKey(id))
			sourceTime=time.get(id);
		long startTime=System.nanoTime();
		setConditionalLists(true, string, actualValue, id, sourceMap);
			base.isEmpty();
		long trueListTime=System.nanoTime()-startTime+sourceTime;
		Automaton trueBase=base;
		//TODO: Use arg in calculation
		startTime=System.nanoTime();
		setConditionalLists(false, string, actualValue, id, sourceMap);
			base.isEmpty();
		long falseListTime=System.nanoTime()-startTime+sourceTime;
		constraintTime.put(id, trueListTime+falseListTime);
		Automaton falseBase=base;
		
		//check for unsoundness
		if(!((Automaton) store.get(sourceMap.get("t"))).subsetOf(trueBase.union(falseBase))){
			numUnsound.add(id);
			System.err.println("Unsound:"+string);
			System.err.println(pastLists.get(id));
			System.exit(0);
		}
		//check for weird over approximations
		if(!trueBase.union(falseBase).subsetOf(((Automaton) store.get(sourceMap.get("t"))))){
			numOver.add(id);
			System.err.println("Over:"+string+" "+actualValue);
			System.err.println(pastLists.get(id));
			System.exit(0);
		}
		
		taint=store.getTaint(id);
		if(taint&&!fName.equals("regionMatches")&&(!fName.startsWith("equals")||actualValue.equals("true"))){
			if(base.getFiniteStrings(1)!=null && (arg==null||(arg.getFiniteStrings(1)!=null))){
				taint=false;
				store.setTaint(sourceMap.get("t"), false);
				if(argNum>=0)
					store.setTaint(argNum, false);
			}		
		}
		if(trueBase!=null&&falseBase!=null&&trueBase.isEmpty()&&falseBase.isEmpty()){
			completeResult="UNSAT";
			unsat.add(id);
			System.err.println("JSAUNSAT");
			System.exit(1);
		}
		else if(trueBase!=null&&trueBase.isEmpty()){
			completeResult="TRUEUNSAT";
			trueUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("true")){

				System.err.println("\nInaccurate trueunsat " +string+" "+id+": "+sourceMap.get("t")+":"+actualVals.get(sourceMap.get("t"))+" "+sourceMap.get("s1")+":"+actualVals.get(sourceMap.get("s1")));
				System.err.println(((Automaton)store.get(sourceMap.get("t"))).run(actualVals.get(sourceMap.get("t")))+"\t"+((Automaton)store.get(sourceMap.get("s1"))).run(actualVals.get(sourceMap.get("s1"))));
				System.exit(1);
				System.err.println(pastLists.get(sourceMap.get("t")));
				System.err.println(pastLists.get(sourceMap.get("s1")));
			}
		}
		else if(falseBase!=null&&falseBase.isEmpty()){
			completeResult="FALSEUNSAT";
			falseUnsat.add(id);
			if(!toTaints.contains(id))
				unsatTimeout.add(id);
			if(actualValue.equals("false")){
				System.err.println("\nInaccurate falseunsat"+id+": "+sourceMap.get("t")+":"+actualVals.get(sourceMap.get("t"))+" "+sourceMap.get("s1")+":"+actualVals.get(sourceMap.get("s1")));
				System.err.println(((Automaton)store.get(sourceMap.get("t"))).getShortestExample(true)+ " "+((Automaton)store.get(sourceMap.get("s1"))).getShortestExample(true));
				System.exit(1);
			}
		}
		else if(trueBase==null||falseBase==null||!trueBase.intersection(falseBase).isEmpty()){
			completeResult="UNKNOWN";
			unknown.add(id);
			newTaint(sourceMap);
			store.setTaint(id, true);
			if(trueBase!=null&&falseBase!=null&&(trueBase.subsetOf(falseBase)||falseBase.subsetOf(trueBase)))
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
	
			System.out.print("\t"+actualValue);
			System.out.print("\t"+base.getNumberOfStates()+"\t"+base.getNumberOfTransitions());
			if(arg!=null)
				System.out.print("\t"+arg.getNumberOfStates()+"\t"+arg.getNumberOfTransitions()+"\t"+taint+"\n");
			else
				System.out.print("\t"+0+"\t"+0+"\t"+store.getTaint(id)+"\n");
		}
	}
	/**
	 * Depricated. Ensures the result is a character.
	 * @param id The id of the constraint.
	 * @param target
	 * @param uniqueid
	 * @return A string representing an ECLiPSe-str constant character.
	 */
	private Automaton grabChar(int id){
//		String val1=actualVals.get(id);
//		Automaton result;
//		try{
//			int tempVal=Integer.parseInt(val1);
//			if(!(tempVal<10 && tempVal >=0)){
//				result=Automaton.makeChar((char)tempVal);
//			}
//			else{
//				result=Automaton.makeChar(val1.charAt(0));
//				//result=Automaton.makeChar((char)tempVal).union(Automaton.makeString(tempVal+""));
//			}
//		}
//		catch(NumberFormatException e){
//			result=Automaton.makeChar(val1.charAt(0));
//		}
//		return result;
		return Automaton.makeString(actualVals.get(id));
	}

	@Override
	public void finishUp() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Checks if the automaton at the point given represents any string.
	 * @param id
	 * @return
	 */
	public boolean isAnyString(int id){
		return ((Automaton)store.get(id)).isTotal();
	}
}
