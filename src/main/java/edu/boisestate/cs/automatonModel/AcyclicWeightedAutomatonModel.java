package edu.boisestate.cs.automatonModel;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.acyclic.WeightedState;
import edu.boisestate.cs.automaton.acyclic.WeightedTransition;
import edu.boisestate.cs.util.DotToGraph;
import edu.boisestate.cs.automaton.acyclic.AcyclicWeightedAutomaton;
import edu.boisestate.cs.automaton.acyclic.BasicAcyclicWeightedAutomaton;
import edu.boisestate.cs.automaton.acyclic.BasicAcyclicWeightedOperations;

public class AcyclicWeightedAutomatonModel extends AutomatonModel<AcyclicWeightedAutomatonModel>{

	private AcyclicWeightedAutomaton automaton;

	protected AcyclicWeightedAutomatonModel(AcyclicWeightedAutomaton automaton, 
			Alphabet alphabet, int initialBoundLength) {
		super(alphabet, initialBoundLength);
		this.automaton = automaton;
	}

	protected AcyclicWeightedAutomatonModel(AcyclicWeightedAutomaton automaton, 
			Alphabet alphabet) {
		super(alphabet, 0);
		this.automaton = automaton;
	}

	@Override
	public String getAcceptedStringExample() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getFiniteStrings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return automaton.isEmpty();
	}

	@Override
	public boolean isSingleton() {
		boolean ret = automaton.getStringCount().intValue() <= 1 ? true : false;
		return ret;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertContainedInOther(AcyclicWeightedAutomatonModel containingModel) {
		//System.out.println("assertContainedInOther " + isEmpty() + " " + containingModel.isEmpty());
		//this has substring of other
		AcyclicWeightedAutomaton other = containingModel.automaton;
		//eas: as of 12-26-18, return unchanged
		//DotToGraph.outputDotFile(automaton.toDot(), "containedInOtherRet");
		//eas: as of 2-9-18 arg is empty return empty otherwise make a copy
		AcyclicWeightedAutomaton ret;
		if(other.isEmpty()){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else {
			ret = automaton.clone();
		}
		ret.determinize();
		ret.normalize();
		ret.minimize();
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}



	@Override
	public AcyclicWeightedAutomatonModel assertContainsOther(AcyclicWeightedAutomatonModel containedModel) {
		//System.out.println("assertContainsOther " + isEmpty() + " " + containedModel.isEmpty());

		//a bit of optimization, but still work
		AcyclicWeightedAutomaton ret;
		if(containedModel.isEmpty()){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else {
			//a common approach is to surround containedModel with all strings and then 
			//take an intersection of this with that model
			//here we limit to weighted automata with finite language, thus cannot append any string
			//instead we surround with weighted automata with strings up to that max length
			//of this automaton
			//step 1 find max (or we can use number of states + 1 value, which might create larger automata)
			int maxLength = this.automaton.getMaxLenght();
			//			System.out.println("This count 1: " + automaton.getStringCount());
			//			if(automaton.getStringCount().intValue() == 169){
			//				DotToGraph.outputDotFile(automaton.toDot(), "before169");
			//			}
			automaton.determinize();
			automaton.normalize();
			automaton.minimize();
			//			if(automaton.getStringCount().intValue() == 566){
			//				DotToGraph.outputDotFile(automaton.toDot(), "after566");
			//			}
			//automaton.normalize();
			//System.out.println("Here 1 ");
			//DotToGraph.outputDotFile(automaton.toDot(), "this");
			//DotToGraph.outputDotFile(automaton.toDot(), "containsInBase");

			//System.out.println("Max L " + maxLength);
			AcyclicWeightedAutomaton prefix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
			prefix.determinize();
			//prefix.normalize();
			//System.out.println("Prefix\n" + prefix);
			//DotToGraph.outputDotFile(prefix.toDot(), "prefix");
			AcyclicWeightedAutomaton suffix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
			suffix.determinize();
			//suffix.normalize();
			//System.out.println("Suffix\n" + suffix);
			//DotToGraph.outputDotFile(suffix.toDot(), "suffix");
			AcyclicWeightedAutomaton contained = containedModel.automaton;
			//System.out.println("Other:\n" + contained + contained.getMaxLenght() + " \n" + automaton.getStringCount().intValue());
			contained.determinize();
			//System.out.println("Done determinze 1");
			//contained.normalize();
			//DotToGraph.outputDotFile(contained.toDot(), "other");
			AcyclicWeightedAutomaton x = prefix.concatenate(contained);
			//System.out.println("Done concat 1");
			x.determinize();
			//System.out.println("Done determinze 2");
			x.normalize();
			x.minimize();
			//System.out.println("X1: " + x);
			//DotToGraph.outputDotFile(x.toDot(), "X1");
			x = x.concatenate(suffix);
			//System.out.println("X2: " + x);
			//DotToGraph.outputDotFile(x.toDot(), "X2");
			x.determinize();
			x.normalize();
			x.minimize();
			//System.out.println("X2D: " + x);
			//DotToGraph.outputDotFile(x.toDot(), "X2D");
			//DotToGraph.outputDotFile(x.toDot(), "X2DN");
			//for a just a single string we can remove the weights
			x = x.flatten();
			//			if(automaton.getStringCount().intValue() == 169){
			//				DotToGraph.outputDotFile(x.toDot(), "containsInArg");
			//			}
			//System.out.println("Here 2 ");
			ret  = automaton.intersection(x);
			//System.out.println("Here 3 ");
			//System.out.println("Ret: " + ret);

			//System.out.println(ret.toDot());
			//DotToGraph.outputDotFile(ret.toDot(), "RET");
			//System.out.println("Here 4 ");
			ret.determinize();
			//System.out.println("Here 5 ");
			ret.normalize();
			ret.minimize();
			//			System.out.println("Other:\n" + contained + contained.getMaxLenght() + " \n" + automaton.getStringCount().intValue());
			//			if(automaton.getStringCount().intValue() == 566 && contained.getMaxLenght() == 0){
			//			 DotToGraph.outputDotFile(ret.toDot(), "containsOtherRet");
			//			 System.out.println("Ret: " + ret);
			//			 System.exit(2);
			//			}
		}

		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEmpty() {
		//do the intersection with an empty string (cannot just create an empty one, otherwise
		//the empty string count could be of)
		//System.out.println("Assert Emtpy " + automaton.getStringCount());
		//AcyclicWeightedAutomaton ret = this.automaton.intersection(BasicAcyclicWeightedAutomaton.makeEmptyString());
		automaton.determinize();
		automaton.normalize();
		automaton.minimize();
		AcyclicWeightedAutomaton ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		if(automaton.getInitialState().isAccept()){
				ret.getInitialState().setAccept(true);
				ret.getInitialState().setWeight(automaton.getInitialState().getWeight());
		}
			
		//otherwise it is just empty
		return new AcyclicWeightedAutomatonModel(ret, alphabet, 0);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEndsOther(AcyclicWeightedAutomatonModel baseModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEndsWith(AcyclicWeightedAutomatonModel endingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEquals(AcyclicWeightedAutomatonModel equalModel) {
		//return the intersection of equlaModel and this
		AcyclicWeightedAutomaton ret = this.automaton.intersection(equalModel.automaton);

		//		if(ret.getStringCount().intValue() == 0){
		//			System.out.println("target1 " + automaton);
		//			System.out.println("arg1 " + equalModel.automaton);
		//			System.out.println("ret1 " + ret);
		//			//System.exit(2);
		//		}
		ret.determinize();
		ret.normalize();
		ret.minimize();
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEqualsIgnoreCase(AcyclicWeightedAutomatonModel equalModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertHasLength(int min, int max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainsOther(AcyclicWeightedAutomatonModel notContainingModel) {
		//System.out.println("assertNotContainsOther " + isEmpty() + " " + notContainingModel.isEmpty());
		AcyclicWeightedAutomaton ret;
		if(notContainingModel.isEmpty()){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else {
			//System.out.println("notContainingIsEmpyt " + notContainingModel.isEmpty());
			//create the set minus of this and notContainingModel
			//1. complete nonContainingModel
			//System.out.println("Flattaning");
			AcyclicWeightedAutomaton notContaining = notContainingModel.automaton.flatten();
			//System.out.println("notContaining " + notContaining.getStates().size());
			//up to the maxLenght of notContaining should work since we will surround it
			//with  .*
			//System.out.println("ABC " + alphabet.getCharSet() + " length " + notContaining.getMaxLenght());
			//System.out.println("Completing");
			notContaining = notContaining.complete(notContaining.getMaxLenght(), alphabet.getCharSet());
			
			//System.out.println("notContaining " + notContaining.getStates().size());
			//notContaining.determinize();//cannot do it otherwise removes unreachable states that we need
			//DotToGraph.outputDotFile(notContaining.toDot(), "notContainsOtherArg1");

			int maxLength = automaton.getMaxLenght();
			//DotToGraph.outputDotFile(automaton.toDot(), "notContainsOtherBase");
			//System.out.println("Building prefix and suffix");
			//2. concatenate it with .* on both sides
			AcyclicWeightedAutomaton prefix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
			//System.out.println("prefix " + prefix.getStates().size());
			//prefix.determinize();
			AcyclicWeightedAutomaton suffix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
			//System.out.println("suffix " + prefix.getStates().size());
			//suffix.determinize();
			notContaining = prefix.concatenate(notContaining);
			notContaining.determinize();
			notContaining.normalize();
			notContaining.minimize();
			//System.out.println("suffix+arg " + notContaining.getStates().size());
			notContaining = notContaining.concatenate(suffix);
			//System.out.println("notContaingAll " + notContaining.getStates().size());
			notContaining.determinize();
			notContaining.normalize();
			notContaining.minimize();
			//DotToGraph.outputDotFile(notContaining.toDot(), "notContainsInArg2");
			//it is ok when non-containing is not empty, i.e., accepts at least one string
			//but when it is empty, it should return the empty machine, that is no executions are possible there
			//System.out.println("Performing minus operation " + automaton.getStates().size() + " notContaining " + notContaining.getStates().size());
			ret = automaton.minus(notContaining);
			//DotToGraph.outputDotFile(ret.toDot(), "notContainsOtherRet");
			//System.out.println("Determinize");
			ret.determinize();
			//System.out.println("Normalize");
			ret.normalize();
			ret.minimize();
		}
		//System.out.println(ret);
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainedInOther(AcyclicWeightedAutomatonModel notContainedModel) {
		//System.out.println("assertNotContainedInOther " + isEmpty() + " " + notContainedModel.isEmpty());
		AcyclicWeightedAutomaton ret;
		if(notContainedModel.automaton.isEmpty()){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else {
			//clone
			ret = automaton.clone();
		}
		//System.out.println(ret);
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEmpty() {
		//get the empty string and complete it first since we will do the complement of it
//		AcyclicWeightedAutomaton arg = BasicAcyclicWeightedAutomaton.makeEmptyString();
//		//		System.out.println("arg before");
//		//		System.out.println(arg);
//		arg = arg.complete(automaton.getMaxLenght(), alphabet.getCharSet());
//		//		System.out.println("arg complete");
//		//		System.out.println(arg);
//		AcyclicWeightedAutomaton ret = this.automaton.minus(arg);
		automaton.determinize();
		automaton.normalize();
		automaton.minimize();
		AcyclicWeightedAutomaton ret = automaton.clone();
		//set it initial state to non-accepting, thus removing all epsilon transitins
		ret.getInitialState().setAccept(false);
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEndsOther(AcyclicWeightedAutomatonModel notEndingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEndsWith(AcyclicWeightedAutomatonModel notEndingModel) {

		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEquals(AcyclicWeightedAutomatonModel notEqualModel) {
		//since we will have to do set minus, which requires the complement we should have a complete automaton
		//System.out.println("assertNotEquals ");
		//System.out.println(automaton.getMaxLenght() + " " + alphabet.getCharSet() );
		//System.out.println("notEqual \n" + notEqualModel.automaton + "\n" + notEqualModel.automaton.getStringCount());

		//System.out.println("notEqual \n" + automaton + "\n" + automaton.getStringCount());
		//can only perform minus operation if the argument is a singleton, otherwise 
		//we over-approximate since it could be anything
		AcyclicWeightedAutomaton ret;
		if(!notEqualModel.automaton.isEmpty()){
			AcyclicWeightedAutomaton notEqual = notEqualModel.automaton.complete(automaton.getMaxLenght(), alphabet.getCharSet());
			//System.out.println("notEqual \n" + notEqual);
			ret = automaton.minus(notEqual);
		} else {

			ret = automaton.clone();
		}
		ret.determinize();
		ret.normalize();
		ret.minimize();
		//System.out.println("ret \n" + ret);
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEqualsIgnoreCase(AcyclicWeightedAutomatonModel notEqualModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotStartsOther(AcyclicWeightedAutomatonModel notStartingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotStartsWith(AcyclicWeightedAutomatonModel notStartsModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertStartsOther(AcyclicWeightedAutomatonModel startingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertStartsWith(AcyclicWeightedAutomatonModel startingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel concatenate(AcyclicWeightedAutomatonModel arg) {
//		System.out.println("This " + automaton.getStringCount());
//		DotToGraph.outputDotFile(automaton.toDot(), "concatTar");
//		System.out.println("In " + arg.modelCount());
//		DotToGraph.outputDotFile(arg.automaton.toDot(), "concatArg");
		AcyclicWeightedAutomaton res = this.automaton.concatenate(arg.automaton);
//		System.out.println("Concat1 " + res.getStringCount());
//		DotToGraph.outputDotFile(res.toDot(), "concatRes");
		//res.minimize();
		res.determinize();
		res.normalize();
		res.minimize();
//		System.out.println("Concat2 " + res.getStringCount());
		//System.exit(2);
		return new AcyclicWeightedAutomatonModel(res, this.alphabet);
	}

	@Override
	public boolean containsString(String actualValue) {

		return automaton.run(actualValue);
	}

	@Override
	public AcyclicWeightedAutomatonModel delete(int start, int end) {
		/*
		 * Removes the characters in a substring of this sequence. 
		 * The substring begins at the specified start and extends 
		 * to the character at index end - 1 or to the end of the 
		 * sequence if no such character exists. 
		 * If start is equal to end, no changes are made.
		 * 
		 * StringIndexOutOfBoundsException - if start is negative, 
		 * greater than length(), or greater than end.
		 */
		AcyclicWeightedAutomaton res = null;

		automaton.determinize();
		automaton.normalize();
		//System.out.println("deleting " + start + " " + end);
		//BasicAcyclicWeightedOperations.removeUnreachableStates(automaton);
		//System.out.println(automaton.getStringCount() + " " + automaton.getMaxLenght());
		
		if(start < 0 || start > end || automaton.isEmpty() || start > automaton.getMaxLenght()){
			res = BasicAcyclicWeightedAutomaton.makeEmpty();
		} 
		else  if( start == end){
			//return automaton with string with
			//greater or equal length than start
			res = automaton.clone();
			//DotToGraph.outputDotFile(res.toDot(), "origRes");
			//System.out.println("RES " + res);
			WeightedState nextState = res.getInitialState();
			if(start > 0){
				//make nextState nonfinal since the empty string would throw an
				//exception
				nextState.setAccept(false);
			}
			//start - 2, the actual indices of the strings which toStates
			//should be set to non-final
			removeDFS(nextState, start - 2, 0);
			
				//System.out.println(res.getStringCount());
//				res.determinize();
//				res.normalize();
			BasicAcyclicWeightedOperations.removeUnreachableStates(res);
				//DotToGraph.outputDotFile(res.toDot(), "delRes");
				//System.out.println("DEL " + res);
		

		} else {
			//we will do dfs algorithm since we also need to count
			//the weight of each "removed" substring
			res = automaton.clone();
			if(start > 0){ // an empty string will throw an exception
				res.getInitialState().setAccept(false);
			}
			//DotToGraph.outputDotFile(res.toDot(), "origRes");
			//System.out.println("RES " + res);
			WeightedState nextState = res.getInitialState();
			int index = 0;//starting index
			WeightedState connectFromState = null;//state to create epsilon transitions from
			//the weight of the given path, start as 1
			Fraction weight = new Fraction(1,1);
			deleteDFS(nextState, start, index, end, connectFromState, weight);
		}

		//		System.out.println("DELETE " + start + "\t" + end + " " + res.getStringCount());
		//		DotToGraph.outputDotFile(automaton.toDot(), "orig");
		//		DotToGraph.outputDotFile(res.toDot(), "deletedOrig");
		res.determinize();
		res.normalize();
		res.minimize();
//		System.out.println("resMC " + res.getStringCount());
//		if(res.getStringCount().intValue() == 336){
//			DotToGraph.outputDotFile(res.toDot(), "deleted");
//		}
		//stop for now
		//		if(start == 1 &&  end == 2 && res.getStringCount().intValue() < 12){
		//			System.exit(2);
		//		}
		return new AcyclicWeightedAutomatonModel(res, this.alphabet);
	}

	private void removeDFS(WeightedState currState, int depth, int index) {
		if(depth >= index){
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				//should be no conditioning on whether that state is accepting or not
				//make copies for all 
				//if(toState.isAccept()){
					//create an non-final version of it
					WeightedState copy = new WeightedState();
					//add the transitions to it
					for(WeightedTransition tTo : toState.getTransitions()){
						copy.addTransition(new WeightedTransition(copy, tTo.getSymb(),tTo.getToState(), tTo.getWeight()));
					}
					//update the transition
					t.setToState(copy);
					toState = copy;
				//} // end if toState accept
				//call removeDFS on it again
				removeDFS(toState, depth, index+1);
			}
		}

	}

	private void deleteDFS(WeightedState currState, int start, int index, int end, WeightedState connectFromState, Fraction weight){
		if(start > index){
			//create copies of the explored states
			//have not reached the point where we need to remove the states
			//thus create a non-final copy of a toState and explore it further, increment index
			//a non-final copy represent Exception thrown by Java if the start index is greater
			//then the length of the string
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			//further optimizaition create a copyState per set of 
			//transitions with the same toState
			Map<WeightedState, Set<WeightedTransition>> copySet = new HashMap<WeightedState, Set<WeightedTransition>>();
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				Set<WeightedTransition> trSet = null;
				if(copySet.containsKey(toState)){
					trSet = copySet.get(toState);
				} else {
					trSet = new HashSet<WeightedTransition>();
					copySet.put(toState, trSet);
				}
				//add the transition to it
				trSet.add(t);
				for(Entry<WeightedState, Set<WeightedTransition>> sTr : copySet.entrySet()){
					WeightedState newToState = new WeightedState();
					WeightedState oldToState = sTr.getKey();
					//if the next is the start then we have to make sure to preserve the acceptace
					if(index + 1 == start){
						if(oldToState.isAccept()){
							newToState.setAccept(true);
							newToState.setWeight(oldToState.getWeight());
						}
					}

					for(WeightedTransition tOld : oldToState.getTransitions()){
						newToState.addTransition(new WeightedTransition(newToState, tOld.getSymb(), tOld.getToState(), tOld.getWeight()));
					}
					//update the transitions for the currentState
					for(WeightedTransition tCurr : sTr.getValue()){
						//change toState toCurr
						tCurr.setToState(newToState);
					}

					//now call DFS on tempState, and increment the index count, i.e., the depth
					deleteDFS(newToState, start, index + 1, end, connectFromState, weight);
				}
			}
		} else if (start == index) {
			//found the state from which to start to remove transitions
			connectFromState = currState;
			
			//explore its transitions and propagate new weight and remove each transition to that toState
			HashSet<WeightedTransition> wtSet  = new HashSet<WeightedTransition>();
			wtSet.addAll(currState.getTransitions());
			for(WeightedTransition wt : wtSet){
				currState.removeTransition(wt);
				deleteDFS(wt.getToState(), start, index + 1, end, connectFromState, weight.multiply(wt.getWeight()));
			}

		} else if (start < index && index < end){
			//if currState is a final state then we need to set conncetFromState as a final state
			//and update its weights using addition
			if(currState.isAccept()){
				Fraction newStateWeight = currState.getWeight().multiply(weight);
				if(connectFromState.isAccept()){
					connectFromState.setWeight(connectFromState.getWeight().add(newStateWeight));
				} else {
					connectFromState.setAccept(true);
					connectFromState.setWeight(newStateWeight);
				}
			}
			//explore its transitions and propagate new weight
			for(WeightedTransition wt : currState.getTransitions()){
				deleteDFS(wt.getToState(), start, index + 1, end, connectFromState, weight.multiply(wt.getWeight()));
			}

		} else if (index == end){
			//at this point currState is where the second part starts, thus we need to connect it to fromState
			
			if(currState.isAccept()){
				Fraction newStateWeight = currState.getWeight().multiply(weight);
				if(connectFromState.isAccept()){
					connectFromState.setWeight(connectFromState.getWeight().add(newStateWeight));
				} else {
					connectFromState.setAccept(true);
					connectFromState.setWeight(newStateWeight);
				}
			}
//			if(start == 0 && end == 1){
//				System.out.println(connectFromState.getWeight());
//			}
			//reconnect the transitions from currState
			//connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), currState, weight);
			//cannot use epsilon transitions
			//copy the transitions from currState to connectFromState
			for(WeightedTransition wt : currState.getTransitions()){
				connectFromState.addTransition(new WeightedTransition(connectFromState, wt.getSymb(), wt.getToState(), weight.multiply(wt.getWeight())));
			}
			
//			if(start == 0 && end == 1){
//				System.out.println(connectFromState.getWeight());
//			}
		}
	}





	@Override
	public boolean equals(AcyclicWeightedAutomatonModel arg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AcyclicWeightedAutomatonModel intersect(AcyclicWeightedAutomatonModel arg) {
		AcyclicWeightedAutomaton res = this.automaton.intersection(arg.automaton);
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	@Override
	public AcyclicWeightedAutomatonModel insert(int offset, AcyclicWeightedAutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger modelCount() {
		return automaton.getStringCount();
	}

	@Override
	public AcyclicWeightedAutomatonModel replace(char find, char replace) {

		AcyclicWeightedAutomaton res = automaton.clone();
		res.determinize();
		//DotToGraph.outputDotFile(res.toDot(), "replaceBefore");
		//find all transitions with that char
		//remove from fromState and add it back with the updated 
		//char
		for(WeightedState s : res.getStates()){
			Set<WeightedTransition> transSet = new HashSet<WeightedTransition>();
			transSet.addAll(s.getTransitions());
			for(WeightedTransition wt : transSet){
				if(wt.getSymb() == find){
					wt.setSybmol(replace);
				}
			}
		}
		//DotToGraph.outputDotFile(res.toDot(), "replaceAfter");
		res.determinize();
		res.normalize();
		res.minimize();
		//DotToGraph.outputDotFile(res.toDot(), "replaceAfterDet");
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	@Override
	public AcyclicWeightedAutomatonModel replace(String find, String replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceChar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceFindKnown(char find) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceReplaceKnown(char replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel reverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel substring(int start, int end) {
		//		if(start == 0 && end == 0){
		//			System.out.println("In \n" + automaton);
		//			//System.exit(2);
		//		}
		//so for substring end cannot be greater than the length
		//otherwise it will throw an exception, while
		//in delete it will happened when the start is greater than the length

		//		 AcyclicWeightedAutomatonModel ret = this.delete(0, start);
		//			//check the indices, similar to delete
		//			int maxEnd = ret.automaton.getMaxLenght() + 1;
		//			ret = ret.delete(end - start, maxEnd);
		//		 return ret;


		int maxLength = automaton.getMaxLenght();
		AcyclicWeightedAutomaton res;
		if(end > maxLength || start < 0 || start > end){
			//create an empty one
			res = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else { 
			//could optimize extradDFS by following a set of transitions if the lead to the same state
			Set<WeightedState> startStates = extractDFS(automaton.getInitialState(), 0, start, end, new Fraction(1));
			res = BasicAcyclicWeightedAutomaton.makeEmpty();
			WeightedState startState = res.getInitialState();
			//now do epsilon connections
			//System.out.println("startStates " + startStates);
			for(WeightedState s : startStates){
				//do additive epsilon transitions
				//the weights should be added
				if(s.isAccept()){
					if(startState.isAccept()){
						startState.setWeight(startState.getWeight().add(s.getWeight()));
					} else {
						startState.setAccept(true);
						startState.setWeight(s.getWeight());
					}
				} 
				for(WeightedTransition wt : s.getTransitions()){
					//copy the transitions
					startState.addTransition(new WeightedTransition(startState, wt.getSymb(), wt.getToState(), wt.getWeight()));
				}
				//startState.addEpsilonTransition(res.getIncoming(startState), s);
			}
			res.determinize();
			res.normalize();
			res.minimize();
			//			if(start == 0 && end == 0){
			//				System.out.println("SS\n " + startStates);
			//				System.out.println("res\n " + res);
			//				//System.exit(2);
			//			}
		}
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	private Set<WeightedState> extractDFS(WeightedState from, int indx, int start, int end, Fraction weight) {
		Set<WeightedState> ret = new HashSet<WeightedState>();
		if(indx < start){
			//explore its children and keep the count of the weight, which should be multiplied
			for(WeightedTransition wt : from.getTransitions()){
				//can optimize here by figuring out when children go to the same state
				ret.addAll(extractDFS(wt.getToState(), indx+1, start, end, weight.multiply(wt.getWeight())));
			}

		} else if (indx == start){
			//then make from state a start state, i.e., return it
			//create a copy of from
			WeightedState newStart = new WeightedState();
			//not sure it should be accept because 

			ret.add(newStart);

			if(start != end){
				//explore its children, but do not add anything from it
				for(WeightedTransition wt : from.getTransitions()){

					//add the transitions to the new state too
					//we need to create a copy of toState right here, otherwise
					//the transition will still go to the old state
					WeightedState newTo = new WeightedState();
					WeightedState oldTo = wt.getToState();
					//System.out.println(wt.getSymb() + " -> " + oldTo);
					newStart.addTransition(new WeightedTransition(newStart, wt.getSymb(), newTo, wt.getWeight().multiply(weight)));
					//copy the transitions if we will explore further only
					if(indx + 1 < end){
						for(WeightedTransition wtOldTo : oldTo.getTransitions()){
							newTo.addTransition(new WeightedTransition(newTo, wtOldTo.getSymb(), wtOldTo.getToState(), wtOldTo.getWeight()));
						}
						extractDFS(newTo, indx+1, start, end, new Fraction(1)); // we stop considering the weight from now on
					} else if (indx + 1 == end){
						//finishing up here 
						int tail = automaton.getStringCountFromState(oldTo).intValue();
						//System.out.println("tail is " + tail);
						if(tail > 0){
							//make state final and update its weight
							//update the weight of the state
							newTo.setAccept(true);
							Fraction newWeight = new Fraction(tail);
							//if oldTo is accept then the algorithm will count 
							//it too, so no need to add its weight bac kagain.
							newTo.setWeight(newWeight);
						}
						//System.out.println("newTo " + newTo);

					} else {
						//cannot be reached
						System.out.println("Ivalid code location 1");
						System.exit(2);
					}
				}
			} else {
				//setting accept based on the count and the accept state
				int tail = automaton.getStringCountFromState(from).intValue();
				//System.out.println("tail " + tail);
				if(tail > 0){
					newStart.setAccept(true);
					Fraction newWeight = new Fraction(0);
					if(!from.isAccept() && tail > 0){
						//all the string that pass through it
						newWeight = new Fraction(tail).multiply(weight);
					} else if(from.isAccept()){ //if it is accept the tail will never be 0, it will count the empty string of from state
						//remove the empty string of from state
						Fraction tailNoEps = new Fraction(tail).subtract(from.getWeight());
						//System.out.println("tailNoEps " + tailNoEps);
						//all the string that pass through it
						Fraction allTailNoEps = tailNoEps.multiply(weight);
						//all the strings that end there
						Fraction fromAccept = from.getWeight().multiply(weight);
						newWeight = allTailNoEps.add(fromAccept);
					}

					//set the new weight
					newStart.setWeight(newWeight);
				}
			}
		} else if (indx <= end ){
			//in between start and end
			//create a copy of toState and explore further
			//explore its children, but do not add anything from it
			for(WeightedTransition wt : from.getTransitions()){
				//add the transitions to the new state too
				//we need to create a copy of toState right here, otherwise
				//the transition will still go to the old state
				WeightedState newTo = new WeightedState();
				WeightedState oldTo = wt.getToState();
				//update the transition
				//update toState for this transition
				wt.setToState(newTo);
				//two cases: to stop reached the end
				if(indx + 1 == end){
					//finishing up here 
					int tail = automaton.getStringCountFromState(oldTo).intValue();
					if(tail > 0){
						//make state final and update its weight
						//update the weight of the state
						newTo.setAccept(true);
						Fraction newWeight = new Fraction(tail);
						newTo.setWeight(newWeight);
					}
				} else {
					//if it is an intermediate state and oldTo happened to
					//be a final one  then the actual string would throw and exception
					//and thus will be removed
					//copy the transitions
					for(WeightedTransition wtOldTo : oldTo.getTransitions()){
						newTo.addTransition(new WeightedTransition(newTo, wtOldTo.getSymb(), wtOldTo.getToState(), wt.getWeight()));
					}
					extractDFS(newTo, indx+1, start, end, new Fraction(1)); // check here too
				}
			}
		}
		return ret;
	}

	@Override
	public AcyclicWeightedAutomatonModel setCharAt(int offset, AcyclicWeightedAutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel setLength(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel suffix(int start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel toLowercase() {
		AcyclicWeightedAutomaton res = automaton.clone();
		for(WeightedState s : res.getStates()){
			Set<WeightedTransition> transSet = new HashSet<WeightedTransition>();
			transSet.addAll(s.getTransitions());
			for(WeightedTransition wt : transSet){
				String symb = String.valueOf(wt.getSymb());
				String lowerCase = symb.toLowerCase();
				if(!lowerCase.equals(symb)){
					//if symb is not a lower case symbol then convert it
					wt.setSybmol(lowerCase.charAt(0));
				}
			}
		}
		res.determinize();
		res.normalize();
		res.minimize();
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	@Override
	public AcyclicWeightedAutomatonModel toUppercase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel trim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel clone() {
		AcyclicWeightedAutomaton res = automaton.clone();
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	@Override
	public String getAutomaton() {
		return automaton.toString();
	}


}
