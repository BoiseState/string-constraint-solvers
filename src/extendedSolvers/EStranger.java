package extendedSolvers;

import edu.ucsb.cs.www.vlab.stranger.PerfInfo;
import edu.ucsb.cs.www.vlab.stranger.StrangerAutomaton;

/**
 * EStranger: An extension of the Stranger string constraint solver written in Java for use in processing string constraints.
 * @author Scott Kausler
 *
 */
public class EStranger extends ExtendedSolver<StrangerAutomaton> {
	
	public EStranger(){
		//Set up Stranger
		if (System.getProperty("jna.library.path") == null) {

			System.setProperty("jna.library.path",
							   "/usr/local/lib/libstranger.dylib");
		}

		StrangerAutomaton.initialize(true);
		PerfInfo perfInfo = new PerfInfo();
		StrangerAutomaton.perfInfo = perfInfo;
		StrangerAutomaton.traceID = 0;
		String fileName = "traceFile"; 
		StrangerAutomaton.openCtraceFile(fileName);
	}

	public void newSymbolicString(int id){
		symbolicStringMap.put(id, StrangerAutomaton.makeAnyString());
	}

	public void newConcreteString(int id, String string){
		StrangerAutomaton automaton;
		if(string == null)
			automaton = StrangerAutomaton.makePhi();
		else if(string.equals(""))
			automaton = StrangerAutomaton.makeAnyStringL1ToL2(0, 0);

		else
			automaton = StrangerAutomaton.makeString(string);
		symbolicStringMap.put(id, automaton);
	}
	
	@Override
	public boolean isValidState(int base, int arg){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		if(baseAutomaton.getNumTransitions()>5000)
			return false;
		return true;
	}

	public void propagateSymbolicString(int id, int base){
		symbolicStringMap.put(id, symbolicStringMap.get(base));
	}

	public void append(int id, int base, int arg, int start, int end){
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);
		argAutomaton=argAutomaton.substring(start, end);
		symbolicStringMap.put(getTempId(), argAutomaton);
		append(id, base, getTempId());
	}

	public void append(int id, int base, int arg){
		newSymbolicString(id);
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		StrangerAutomaton empty=StrangerAutomaton.makeEmptyString();
		if(baseAutomaton.checkEquivalence(empty)){
			symbolicStringMap.put(id, argAutomaton);
		}
		else if(argAutomaton.checkEquivalence(empty)){
			symbolicStringMap.put(id,baseAutomaton);
		}
		else{
			symbolicStringMap.put(id, baseAutomaton.concatenate(argAutomaton));
		}
	}

	public void substring(int id, int base, int start){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		symbolicStringMap.put(id, baseAutomaton.prefix(start));
	}

	public void substring(int id, int base, int start, int end){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		symbolicStringMap.put(id, baseAutomaton.substring(start,end));
	}

	public void setLength(int id, int base, int length){
		StrangerAutomaton automaton;
		if(length==0){
			automaton = StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		}
		else{
			StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
			baseAutomaton=baseAutomaton.concatenate(StrangerAutomaton.makeAnyString());
			automaton = StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
			for(int i=0; i<=length; i++)
				automaton=automaton.union(baseAutomaton.substring(0, i));

			symbolicStringMap.put(id, automaton);
		}
	}

	public void insert (int id, int base, int arg, int offset){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);
		StrangerAutomaton automaton;
		if(offset>0){
			StrangerAutomaton start=baseAutomaton.substring(0, offset);
			StrangerAutomaton end=baseAutomaton.prefix(offset);
			automaton=start.concatenate(argAutomaton).concatenate(end);
		}
		else{
			automaton=argAutomaton.concatenate(baseAutomaton);
		}
		symbolicStringMap.put(id, automaton);
	}

	public boolean isSingleton(int id){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(id);
		return baseAutomaton.isTrueSingleton()!=null;
	}

	public boolean isSound(int id, String actualValue){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(id);
		return baseAutomaton.checkIntersection(StrangerAutomaton.makeString(actualValue));
	}

	public boolean isSingleton(int id, String actualValue){
		return isSingleton(id);
	}

	public String getSatisfiableResult(int id){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(id);
		return baseAutomaton.generateSatisfyingExample();
	}

	public boolean isSatisfiable(int id){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(id);
		return !baseAutomaton.checkEmptiness();
	}

	public void insert (int id, int base, int arg, int offset, int start, int end){
		substring(getTempId(), arg, start, end);
		insert(id, base, getTempId(), offset);
	}
	
	public void setCharAt(int id, int base, int arg, int offset){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);
		StrangerAutomaton automaton;
		if(offset>=0){
			StrangerAutomaton start=baseAutomaton.substring(0, offset);
			StrangerAutomaton end=baseAutomaton.prefix(offset+1);
			automaton=start.concatenate(argAutomaton).concatenate(end);
		}
		else{
			automaton=argAutomaton.concatenate(baseAutomaton.prefix(1));
		}
		symbolicStringMap.put(id, automaton);
	}
	
	public void trim(int id, int base){
		StrangerAutomaton baseAutomation = symbolicStringMap.get(base);
		StrangerAutomaton automaton;
		String singleton=baseAutomation.isSingleton();
		int max=getMaxLength(baseAutomation);
		if(singleton!=null){
			singleton=singleton.trim();
			if(singleton.length()==0){
				automaton=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
			}
			else
				automaton=StrangerAutomaton.makeString(singleton);
		}
		else if(max>-1){
			automaton=StrangerAutomaton.makeAnyStringL1ToL2(0, max);
		}
		else if(baseAutomation.generateSatisfyingExample()!=null && baseAutomation.generateSatisfyingExample().startsWith("   ")){
			automaton = StrangerAutomaton.makeAnyString();
		}
		else if(StrangerAutomaton.makeAnyString().checkInclusion(baseAutomation)){
			automaton = StrangerAutomaton.makeAnyString();
		}
		else{
			StrangerAutomaton oldAuto;
			do{
				oldAuto=baseAutomation;
				automaton=baseAutomation.trim();
				automaton=automaton.trim('\t');
				automaton=automaton.trim('\n');
				automaton=automaton.trim('\r');
				automaton=automaton.trim('\f');

			}while(!automaton.checkEquivalence(oldAuto));
			automaton=automaton.union(StrangerAutomaton.makeAnyStringL1ToL2(0, 0));
		}
		symbolicStringMap.put(id, automaton);
	}
	
	public void delete(int id, int base, int start, int end){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		if(baseAutomaton.checkEquivalence(StrangerAutomaton.makeAnyString())){
			symbolicStringMap.put(id, baseAutomaton);
			return;
		}
		StrangerAutomaton automaton;
		StrangerAutomaton startAuto;
		if(start==0)
			startAuto=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		else
			startAuto=baseAutomaton.suffix(start);
		if(end<=start)
			automaton = baseAutomaton;
		else{
			StrangerAutomaton endAuto=baseAutomaton.prefix(end);
			automaton = startAuto.concatenate(endAuto);
		}
		symbolicStringMap.put(id, automaton);
	}
	
	public void deleteCharAt(int id, int base, int loc){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);

		StrangerAutomaton startAuto=baseAutomaton.suffix(loc);
		StrangerAutomaton endAuto=baseAutomaton.prefix(loc+1);
		symbolicStringMap.put(id, startAuto.concatenate(endAuto));
	}
	
	public void reverse(int id, int base){
		newSymbolicString(id);
	}
	
	public void toUpperCase(int id, int base){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		if(baseAutomaton.checkEquivalence(StrangerAutomaton.makeAnyString()))
			symbolicStringMap.put(id, baseAutomaton.clone());
		else{
			StrangerAutomaton automaton = baseAutomaton.toUpperCase();
			symbolicStringMap.put(id, automaton);
		}
	}
	
	public void toLowerCase(int id, int base){
		StrangerAutomaton automaton;
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		if(baseAutomaton.checkEquivalence(StrangerAutomaton.makeAnyString()))
			automaton = baseAutomaton.clone();
		else if(baseAutomaton.checkEquivalence(StrangerAutomaton.makeAnyStringL1ToL2(0, 0))){
			automaton = StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		}
		else{
			automaton = baseAutomaton.toLowerCase();
		}
		symbolicStringMap.put(id, automaton);
	}
	
	public void replace(int id, int base, int argOne, int argTwo){
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argOneAutomaton = symbolicStringMap.get(argOne);
		StrangerAutomaton argTwoAutomaton = symbolicStringMap.get(argTwo);

		symbolicStringMap.put(id, baseAutomaton.replace(argOneAutomaton, argTwoAutomaton));
	}
	
	private void assertCondition(boolean result, int base, int arg, StrangerAutomaton x){
		setLast(base, arg);
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		if(result){
			baseAutomaton=baseAutomaton.intersect(x);
		}
		else{
			StrangerAutomaton empty=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
			if(!(baseAutomaton.checkEquivalence(empty)&&!argAutomaton.checkEquivalence(empty))){

				StrangerAutomaton temp=baseAutomaton;
				if(argAutomaton.isSingleton()!=null){
					temp=baseAutomaton.minus(x);
				}
				if(baseAutomaton.isSingleton()!=null){
					argAutomaton=argAutomaton.intersect(baseAutomaton.complement());
				}
				baseAutomaton=temp;
			}
		}

		symbolicStringMap.put(base, baseAutomaton);
		symbolicStringMap.put(arg, argAutomaton);
	}
	
	public void contains(boolean result, int base, int arg){
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);
		StrangerAutomaton x=StrangerAutomaton.makeAnyString().concatenate(argAutomaton).concatenate(StrangerAutomaton.makeAnyString());
		assertCondition(result, base, arg, x);
	}
	
	public void endsWith(boolean result, int base, int arg){
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		StrangerAutomaton x=StrangerAutomaton.makeAnyString().concatenate(argAutomaton);
		assertCondition(result, base, arg, x);
	}
	
	public void startsWith(boolean result, int base, int arg){
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		StrangerAutomaton x=argAutomaton.concatenate(StrangerAutomaton.makeAnyString());
		assertCondition(result, base, arg, x);
	}
	
	public void equals(boolean result, int base, int arg){
		setLast(base, arg);
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		if(result){
			baseAutomaton=baseAutomaton.intersect(argAutomaton);
			argAutomaton=argAutomaton.intersect(baseAutomaton);
		}
		else{
			StrangerAutomaton temp=baseAutomaton;
			if(argAutomaton.isSingleton()!=null){
				temp=baseAutomaton.intersect(argAutomaton.complement());
			}
			if(baseAutomaton.isSingleton()!=null)
				argAutomaton=argAutomaton.intersect(baseAutomaton.complement());
			baseAutomaton=temp;
		}

		symbolicStringMap.put(base, baseAutomaton);
		symbolicStringMap.put(arg, argAutomaton);
	}
	
	public void equalsIgnoreCase(boolean result, int base, int arg){
		setLast(base, arg);
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);
		StrangerAutomaton argAutomaton = symbolicStringMap.get(arg);

		baseAutomaton=baseAutomaton.equalsIgnoreCase(argAutomaton, result);

		symbolicStringMap.put(base, baseAutomaton);
		symbolicStringMap.put(arg, argAutomaton);
	}
	
	public void isEmpty(boolean result, int base){
		setLast(base, -1);
		StrangerAutomaton baseAutomaton = symbolicStringMap.get(base);

		if(result){
			baseAutomaton=baseAutomaton.intersect(StrangerAutomaton.makeEmptyString());
		}
		else{
			baseAutomaton=baseAutomaton.intersect(StrangerAutomaton.makeEmptyString().complement());
		}
		symbolicStringMap.put(base, baseAutomaton);
	}
	
	/**
	 * Get the max length of a string in an automaton. Used in the trim operaiton.
	 * @param auto The automaton to be checked for length.
	 * @return
	 */
	private static int getMaxLength(StrangerAutomaton auto){
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
	public String replaceEscapes(String value){
		int newMap=255;

		value=value.replace((char)0, (char)newMap);
		value=value.replace((char)56319, (char)newMap);
		value=value.replace((char)65533, (char)newMap);

		return value;
	}

	@Override
	public void shutDown() {		
	}
}
